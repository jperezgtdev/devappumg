package gt.com.ro.devappumg.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import gt.com.ro.devappumg.R;
import gt.com.ro.devappumg.api.RetrofitClient;
import gt.com.ro.devappumg.models.UserResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail;
    private Button btnFetchApi, btnLogout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnFetchApi = findViewById(R.id.btnFetchApi);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);

        loadUserData();

        btnFetchApi.setOnClickListener(v -> fetchUserDataFromApi());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadUserData() {
        SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String name = preferences.getString("user_name", "Usuario");
        String email = preferences.getString("user_email", "correo@ejemplo.com");

        tvUserName.setText("Nombre: " + name);
        tvUserEmail.setText("Correo: " + email);
    }

    private void fetchUserDataFromApi() {
        progressBar.setVisibility(View.VISIBLE);
        btnFetchApi.setEnabled(false);

        SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String token = preferences.getString("auth_token", "");
        String authHeader = "Bearer " + token;

        // Consultar el usuario con ID 1 según la colección
        RetrofitClient.getApiService().getUserById(authHeader, 1).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnFetchApi.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    showUserDialog(response.body());
                } else {
                    Toast.makeText(MainActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnFetchApi.setEnabled(true);
                Toast.makeText(MainActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserDialog(UserResponse user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Resultado API SGAU");
        builder.setMessage("ID: " + user.getId() + "\n" +
                "Username: " + user.getUsername() + "\n" +
                "Nombre: " + user.getNombre() + " " + user.getApellido() + "\n" +
                "Correo: " + user.getEmail() + "\n" +
                "Estado: " + (user.isActivo() ? "Activo" : "Inactivo"));
        builder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void logout() {
        SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        preferences.edit().clear().apply();
        
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}