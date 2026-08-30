package gt.com.ro.devappumg.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import gt.com.ro.devappumg.R;
import gt.com.ro.devappumg.utils.UpdateManager;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Iniciamos la comprobación de actualización
        checkForUpdates();
    }

    private void checkForUpdates() {
        UpdateManager updateManager = new UpdateManager(this);
        updateManager.checkForUpdates(new UpdateManager.UpdateCheckCallback() {
            @Override
            public void onNoUpdate() {
                proceedToApp();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error comprobando actualizaciones", e);
                proceedToApp();
            }
        });
    }

    private void proceedToApp() {
        // Retraso de 1.5 segundos para mostrar el splash antes de decidir a dónde ir
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkSession();
            }
        }, 1500);
    }

    private void checkSession() {
        SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("is_logged_in", false);

        Intent intent;
        if (isLoggedIn) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        
        startActivity(intent);
        finish(); // Cerramos el Splash para que no se pueda volver atrás
    }
}