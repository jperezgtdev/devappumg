# Guía Definitiva: Login Profesional, Persistencia y API REST en Android

Esta guía proporciona un paso a paso detallado para construir una aplicación Android robusta utilizando **Java** y **XML**. Aprenderás diseño de interfaces (UI), experiencia de usuario (UX), persistencia local y consumo de servicios externos.

---

## 1. Arquitectura y Configuración

### 1.1 Dependencias Críticas (Gradle)
Asegúrate de incluir las librerías para comunicación de red en `gradle/libs.versions.toml`:
```toml
[versions]
retrofit = "2.9.0"
okhttp = "4.9.3"

[libraries]
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
logging-interceptor = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
```

### 1.2 Control de Versión Dinámico
En tu `app/build.gradle`, puedes hacer que la versión de la app se inyecte automáticamente en el XML:

```gradle
android {
    defaultConfig {
        versionName "1.0.0"
        // Crea un recurso de string accesible como @string/app_version
        resValue "string", "app_version", "\"Versión ${versionName}\""
    }
    buildFeatures {
        resValues true // Obligatorio en versiones modernas de AGP
    }
}
```

### 1.3 Permisos y Seguridad (Manifest)
No olvides el permiso de Internet y configurar la **SplashActivity** como la actividad de inicio (`LAUNCHER`):

```xml
<application ...>
    <activity android:name=".activities.SplashActivity" android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
    ...
</application>
```

---

## 2. Diseño de Interfaces (Layouts y Estilos)

### 2.1 Pantalla de Carga (`activity_splash.xml`)
Implementa un diseño que use el logo de la aplicación centrado y un fondo que combine con el diseño general.

### 2.2 Uso de Drawables Personalizados
Para lograr un diseño moderno (como ondas y botones redondeados), usamos XML en la carpeta `res/drawable`:
- **bg_input.xml**: Define bordes redondeados y colores de fondo para los campos.
- **bg_wave.xml**: Un `vector` que crea la forma de onda en el encabezado.
- **bg_button.xml**: Define el aspecto del botón (redondeado y con color primario).

### 2.2 Pantalla de Login (`activity_login.xml`)
Estructura sugerida:
1.  **ScrollView**: Permite que el contenido se desplace en pantallas pequeñas.
2.  **ConstraintLayout**: Para posicionar elementos con precisión.
3.  **Encabezado Azul**: Un `FrameLayout` con fondo personalizado y círculos decorativos.
4.  **Campos de Texto**: Agrupados en `LinearLayout` para incluir iconos a la izquierda.
5.  **ProgressBar**: Un elemento circular posicionado en el centro de la pantalla con `android:visibility="gone"`.

---

## 3. Lógica de Usuario y UX

### 3.1 Mostrar y Ocultar Contraseña
Mejora la UX alternando el icono del ojo y el tipo de entrada del teclado:
```java
private void togglePasswordVisibility() {
    if (isPasswordVisible) {
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        ivPassword.setImageResource(R.drawable.ic_visibility_off);
    } else {
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        ivPassword.setImageResource(R.drawable.ic_visibility);
    }
    isPasswordVisible = !isPasswordVisible;
    etPassword.setSelection(etPassword.getText().length()); // Mantiene el cursor al final
}
```

### 3.2 Persistencia con SharedPreferences
Guardamos datos básicos para "Recordar Usuario" y el Token de sesión:
```java
private void saveUserSession(String correo, String nombre, String token) {
    SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString("auth_token", token);
    editor.putBoolean("is_logged_in", true);
    editor.apply();
}
```

### 3.3 Auto-Login (Salto de Login)
Verifica la sesión al arrancar la app en el `onCreate`:
```java
if (preferences.getBoolean("is_logged_in", false)) {
    startActivity(new Intent(this, MainActivity.class));
    finish();
}
```

---

## 4. Comunicación con Microservicios (Retrofit)

### 4.1 Cliente Global (`RetrofitClient`)
Configura la URL base del microservicio y los interceptores para depurar las peticiones:
```java
private static final String BASE_URL = "https://api-sgau-backend-746899482768.us-central1.run.app/";
```

### 4.2 Consumo con JWT (Token)
Para peticiones protegidas, debemos enviar el token en el header `Authorization`:
```java
@GET("api/usuarios/{id}")
Call<UserResponse> getUserById(@Header("Authorization") String token, @Path("id") int id);
```

Y al llamarlo:
`apiService.getUserById("Bearer " + storedToken, 1);`

---

## 5. Errores Comunes y Soluciones

1.  **Error 400 (Bad Request)**: Revisa que tu clase `LoginRequest` tenga los nombres de campos exactos (`@SerializedName("usuario")`) que el servidor espera.
2.  **Pantalla se pone negra al compartir**: El sistema operativo protege los campos de contraseña. Intenta quitar el foco del campo `EditText` antes de compartir pantalla.
3.  **Recursos no encontrados**: Si agregas un `resValue` en Gradle, **debes sincronizar (Sync Project with Gradle Files)** antes de usarlo en el XML.
4.  **Contexto Nulo**: Al usar Retrofit dentro de un fragmento o clase anónima, asegúrate de usar `MainActivity.this` en lugar de solo `this` para los Toasts o Diálogos.
