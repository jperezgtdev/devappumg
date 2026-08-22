# Plan de Implementación: Login, SharedPreferences y Retrofit

Este plan detalla la transformación del proyecto para incluir una pantalla de Login con diseño personalizado, persistencia con `SharedPreferences` y consumo de API con Retrofit.

## Cambios Propuestos

### Configuración y Dependencias

#### [MODIFY] [libs.versions.toml](file:///C:/Users/netop/AndroidStudioProjects/devappumg/gradle/libs.versions.toml)
Agregar versiones y definiciones para Retrofit y Gson.

#### [MODIFY] [app/build.gradle](file:///C:/Users/netop/AndroidStudioProjects/devappumg/app/build.gradle)
Agregar las dependencias de Retrofit.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/netop/AndroidStudioProjects/devappumg/app/src/main/AndroidManifest.xml)
Agregar permiso de Internet y registrar `LoginActivity` y `MainActivity`.

---

### Recursos Visuales

#### [MODIFY] [colors.xml](file:///C:/Users/netop/AndroidStudioProjects/devappumg/app/src/main/res/values/colors.xml)
Integrar la paleta de colores del diseño SecureApp.

#### [MODIFY] [strings.xml](file:///C:/Users/netop/AndroidStudioProjects/devappumg/app/src/main/res/values/strings.xml)
Integrar los textos requeridos.

#### [NEW] Drawables del Login
Crear archivos XML para fondos, iconos y formas:
- `bg_login_header.xml`
- `bg_circle.xml`
- `bg_wave.xml`
- `bg_logo.xml`
- `bg_input.xml`
- `bg_button.xml`
- `bg_title_line.xml`
- `bg_separator.xml`
- `ic_shield_check.xml`
- `ic_person.xml`
- `ic_lock.xml`
- `ic_visibility_off.xml`
- `ic_arrow_right.xml`

---

### Interfaz de Usuario (XML)

#### [NEW] [activity_login.xml](file:///C:/Users/netop/AndroidStudioProjects/devappumg/app/src/main/res/layout/activity_login.xml)
Implementar el diseño detallado en el requerimiento.

#### [NEW] [activity_main.xml](file:///C:/Users/netop/AndroidStudioProjects/devappumg/app/src/main/res/layout/activity_main.xml)
Diseñar la pantalla principal que muestra datos del usuario y el botón para la API.

---

### Lógica de Negocio (Java)

#### [NEW] [LoginActivity.java](file:///C:/Users/netop/AndroidStudioProjects/devappumg/app/src/main/java/gt/com/ro/devappumg/LoginActivity.java)
- Manejo de UI.
- Toggle de visibilidad de contraseña.
- Validación de Login.
- Guardado en `SharedPreferences`.

#### [NEW] [MainActivity.java](file:///C:/Users/netop/AndroidStudioProjects/devappumg/app/src/main/java/gt/com/ro/devappumg/MainActivity.java)
- Recuperación de `SharedPreferences`.
- Configuración y ejecución de Retrofit.
- Visualización de resultados en `AlertDialog`.

#### [NEW] Modelos y API
- `UserResponse.java` (Modelo para la API).
- `ApiService.java` (Interface Retrofit).
- `RetrofitClient.java` (Singleton para Retrofit).

---

## Plan de Verificación

### Pruebas Manuales
1. Verificar que `LoginActivity` cargue con el diseño exacto.
2. Probar el botón de mostrar/ocultar contraseña.
3. Ingresar credenciales y verificar el paso a `MainActivity`.
4. Confirmar que `MainActivity` muestra el nombre/correo guardado.
5. Presionar el botón de API y verificar que el `AlertDialog` muestre datos reales de una API pública (ej. JSONPlaceholder).

### Validación de Restricciones
- Confirmar que se use Java y XML tradicional.
- Verificar que NO haya `RecyclerView` ni `ListView`.
- Asegurar que la contraseña no sea visible en `MainActivity`.
