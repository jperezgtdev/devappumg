# Guía Definitiva: Aplicación Android con Login, Retrofit y Actualización Automática

Esta guía detalla el proceso paso a paso para construir una aplicación profesional en **Java** y **XML**. Cubre desde el diseño visual hasta la integración con microservicios y sistemas de actualización obligatoria desde la nube.

---

## 1. Configuración de Arquitectura (Gradle)

### 1.1 Dependencias y Versiones
Configura tu proyecto para soportar comunicación de red y Firebase en `gradle/libs.versions.toml`:
```toml
[versions]
retrofit = "2.9.0"
okhttp = "4.9.3"
firebaseFirestore = "26.6.0"

[libraries]
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore", version.ref = "firebaseFirestore" }
```

### 1.2 Control de Versión Dinámico
Define la versión de tu app en `app/build.gradle`. Esto permite que el XML lea automáticamente la versión configurada:
```gradle
android {
    defaultConfig {
        versionCode 1
        versionName "1.0.0"
        // Inyecta el String en los recursos XML
        resValue "string", "app_version", "\"Versión ${versionName}\""
    }
    buildFeatures {
        resValues true // Necesario para habilitar resValue
    }
}
```

---

## 2. Flujo de Navegación y UX

### 2.1 Pantalla de Bienvenida (SplashActivity)
El Splash es la puerta de entrada. Su función es:
1. Mostrar la marca.
2. **Verificar actualizaciones** en segundo plano.
3. Decidir si enviar al usuario al Login o a la Pantalla Principal (Auto-login).

### 2.2 Patrones de Experiencia de Usuario (UX)
- **ProgressBar Circular**: Muéstralo siempre antes de iniciar una petición al servidor (`View.VISIBLE`) y ocúltalo al terminar (`View.GONE`).
- **Bloqueo de Botones**: Desactiva el botón de acción durante la carga para evitar peticiones duplicadas.
- **Seguridad Visual**: Implementa el "ojo" para mostrar/ocultar contraseñas cambiando el `inputType` y el icono dinámicamente.

---

## 3. Persistencia de Datos (SharedPreferences)

Utilizamos `SharedPreferences` para dos propósitos distintos:
1. **Recordar Usuario**: Guardar solo el correo/username si el usuario marca la casilla.
2. **Sesión Activa**: Guardar el Token **JWT** y un flag `is_logged_in`.

```java
// Ejemplo de guardado de sesión
SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
prefs.edit()
    .putString("auth_token", token)
    .putBoolean("is_logged_in", true)
    .apply();
```

---

## 4. Consumo de Microservicios (Retrofit)

### 4.1 Peticiones Protegidas con JWT
Para consumir endpoints seguros (como los de SGAU), debes incluir el Token en los encabezados:
```java
public interface ApiService {
    @GET("api/usuarios/{id}")
    Call<UserResponse> getUserById(
        @Header("Authorization") String token, // Formato: "Bearer {token}"
        @Path("id") int id
    );
}
```

### 4.2 Depuración Profesional
Configura un `HttpLoggingInterceptor` en tu `RetrofitClient`. Esto te permitirá ver el JSON exacto de entrada y salida en la pestaña **Logcat** de Android Studio.

---

## 5. Actualización Obligatoria con Firestore

Este sistema garantiza que todos los usuarios tengan la versión más reciente.

### 5.1 Estructura en la Nube
En Firebase Cloud Firestore, crea un documento en `versiones/actual` con:
- `versionCode` (Numérico)
- `apkUrl` (Texto con el link de descarga)

### 5.2 Lógica de Comparación
La app compara su código interno con el de Firestore:
```java
if (firebaseVersionCode > appInstalledVersionCode) {
    // 1. Mostrar Diálogo Obligatorio (setCancelable(false))
    // 2. Descargar APK asíncronamente
    // 3. Iniciar Instalador oficial
}
```

### 5.3 Seguridad en la Instalación (FileProvider)
Para abrir el instalador en Android moderno, debes configurar un `FileProvider` en el Manifest y definir las rutas permitidas en `res/xml/file_paths.xml`. Esto otorga permisos temporales de lectura al instalador sobre el archivo APK descargado.

---

## 6. Resolución de Errores Comunes

1. **Error 400**: El servidor rechazó los datos. Verifica que los nombres de los campos en tus clases Java coincidan con el JSON del API.
2. **Error 403/401**: El Token JWT ha expirado o es inválido.
3. **App no instala la actualización**: Verifica que el `authorities` del `FileProvider` en el Java coincida exactamente con el del `AndroidManifest.xml`.
4. **Permisos**: Asegúrate de tener `INTERNET` y `REQUEST_INSTALL_PACKAGES` declarados.
