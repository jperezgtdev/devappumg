package gt.com.ro.devappumg.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import gt.com.ro.devappumg.R;
import gt.com.ro.devappumg.api.RetrofitClient;
import gt.com.ro.devappumg.models.LoginRequest;
import gt.com.ro.devappumg.models.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario, etPassword;
    private ImageView ivPassword;
    private CheckBox cbRecordarme;
    private LinearLayout btnIngresar;
    private ProgressBar progressBar;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        ivPassword = findViewById(R.id.ivPassword);
        cbRecordarme = findViewById(R.id.cbRecordarme);
        btnIngresar = findViewById(R.id.btnIngresar);
        progressBar = findViewById(R.id.progressBar);

        // Cargar datos si se activó "Recordarme" anteriormente
        loadRememberedData();

        // Configurar mostrar/ocultar contraseña
        ivPassword.setOnClickListener(v -> togglePasswordVisibility());

        // Configurar botón ingresar
        btnIngresar.setOnClickListener(v -> performLogin());
    }

    private void loadRememberedData() {
        SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String savedUser = preferences.getString("remembered_user", "");
        boolean isRemembered = preferences.getBoolean("remember_me", false);

        if (isRemembered && !savedUser.isEmpty()) {
            etUsuario.setText(savedUser);
            cbRecordarme.setChecked(true);
        }
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivPassword.setImageResource(R.drawable.ic_visibility_off);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivPassword.setImageResource(R.drawable.ic_visibility);
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private void performLogin() {
        String usuario = etUsuario.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(usuario, password);
        
        progressBar.setVisibility(View.VISIBLE);
        btnIngresar.setEnabled(false);

        // El header X-Company-ID ahora se envía automáticamente desde RetrofitClient
        RetrofitClient.getApiService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnIngresar.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    
                    String token = loginResponse.getAccessToken();
                    
                    handleRememberMe(usuario);
                    // Guardamos el token y un nombre genérico, el nombre real se obtendrá en MainActivity
                    saveUserSession(usuario, "Usuario SGAU", token);
                    navigateToMain();
                } else {
                    Toast.makeText(LoginActivity.this, "Credenciales incorrectas o error en el servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnIngresar.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleRememberMe(String usuario) {
        SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (cbRecordarme.isChecked()) {
            editor.putString("remembered_user", usuario);
            editor.putBoolean("remember_me", true);
        } else {
            editor.clear();
        }
        editor.apply();
    }

    private void saveUserSession(String correo, String nombre, String token) {
        SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_email", correo);
        editor.putString("user_name", nombre);
        editor.putString("auth_token", token);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}