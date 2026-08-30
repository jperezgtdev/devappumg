package gt.com.ro.devappumg.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateManager {

    private static final String TAG = "UpdateManager";
    private final Context context;
    private final FirebaseFirestore db;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface UpdateCheckCallback {
        void onNoUpdate();
        void onError(Exception e);
    }

    public UpdateManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void checkForUpdates(UpdateCheckCallback callback) {
        db.collection("versiones").document("actual")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        processUpdateInfo(documentSnapshot, callback);
                    } else {
                        Log.e(TAG, "Documento 'versiones/actual' no existe");
                        callback.onNoUpdate();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error consultando Firestore", e);
                    callback.onError(e);
                });
    }

    private void processUpdateInfo(DocumentSnapshot document, UpdateCheckCallback callback) {
        Long firebaseVersionCode = document.getLong("versionCode");
        String versionName = document.getString("versionName");
        String apkUrl = document.getString("apkUrl");

        if (firebaseVersionCode == null || apkUrl == null || apkUrl.isEmpty()) {
            Log.e(TAG, "Datos de actualización incompletos en Firestore");
            callback.onNoUpdate();
            return;
        }

        int currentVersionCode = getCurrentVersionCode();
        if (firebaseVersionCode > currentVersionCode) {
            showUpdateDialog(versionName, currentVersionCode, firebaseVersionCode.intValue(), apkUrl);
        } else {
            callback.onNoUpdate();
        }
    }

    private int getCurrentVersionCode() {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                return (int) pInfo.getLongVersionCode();
            } else {
                return pInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error obteniendo versionCode", e);
            return -1;
        }
    }

    private void showUpdateDialog(String newVersionName, int currentCode, int newCode, String apkUrl) {
        String currentVersionName = "";
        try {
            currentVersionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception ignored) {}

        new AlertDialog.Builder(context)
                .setTitle("Nueva versión disponible")
                .setMessage("Versión actual: " + currentVersionName + " (" + currentCode + ")\n" +
                        "Nueva versión: " + newVersionName + " (" + newCode + ")\n\n" +
                        "Hay una nueva versión disponible de la aplicación.")
                .setPositiveButton("Actualizar", (dialog, which) -> downloadAndInstallApk(apkUrl))
                .setNegativeButton("Luego", null)
                .setCancelable(false)
                .show();
    }

    private void downloadAndInstallApk(String apkUrl) {
        // Construir un AlertDialog dinámicamente con barra de progreso
        AlertDialog progressDialog = new AlertDialog.Builder(context)
                .setTitle("Descargando actualización...")
                .setMessage("Iniciando descarga...")
                .setCancelable(false)
                .create();

        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(false);
        progressBar.setMax(100);
        progressBar.setPadding(40, 20, 40, 20);

        TextView progressText = new TextView(context);
        progressText.setText("0%");
        progressText.setPadding(40, 0, 40, 20);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(progressBar);
        layout.addView(progressText);

        progressDialog.setView(layout);
        progressDialog.show();

        executorService.execute(() -> {
            File result = doDownload(apkUrl, (progress) -> {
                mainHandler.post(() -> {
                    progressBar.setProgress(progress);
                    progressText.setText(progress + "%");
                });
            });

            mainHandler.post(() -> {
                progressDialog.dismiss();
                if (result != null && result.exists()) {
                    installApk(context, result);
                } else {
                    Toast.makeText(context, "Error al descargar la actualización", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private interface ProgressListener {
        void onProgress(int progress);
    }

    private File doDownload(String urlPath, ProgressListener listener) {
        HttpURLConnection connection = null;
        InputStream input = null;
        FileOutputStream output = null;
        try {
            URL url = new URL(urlPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Server returned HTTP " + connection.getResponseCode());
                return null;
            }

            int fileLength = connection.getContentLength();
            input = new BufferedInputStream(url.openStream());
            
            File apkFile = new File(context.getExternalCacheDir(), "update.apk");
            output = new FileOutputStream(apkFile);

            byte[] data = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                if (fileLength > 0) {
                    listener.onProgress((int) (total * 100 / fileLength));
                }
                output.write(data, 0, count);
            }
            return apkFile;
        } catch (Exception e) {
            Log.e(TAG, "Error descargando APK", e);
            return null;
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (Exception ignored) {}
            if (connection != null) connection.disconnect();
        }
    }

    private static void installApk(Context context, File file) {
        try {
            Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error iniciando instalador", e);
            Toast.makeText(context, "No se pudo iniciar el instalador", Toast.LENGTH_LONG).show();
        }
    }
}