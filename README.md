# Rehearsal Cloud App

Rehearsal Cloud es una aplicación móvil para Android desarrollada en Kotlin usando Jetpack Compose, Room, Retrofit y una arquitectura MVVM. Permite gestionar canciones, listas de canciones (setlists) y usuarios, integrando almacenamiento local y sincronización con un backend vía API REST.

## Estructura del Proyecto

- **app/**  
  Contiene todo el código fuente de la aplicación Android.
  - **src/main/java/com/app/rehearsalcloud/**
    - **model/**  
      Entidades de datos (por ejemplo, `User`, `Song`, `Setlist`, `AudioFile`) y la base de datos Room ([`AppDatabase`](app/src/main/java/com/app/rehearsalcloud/model/AppDatabase.kt)).
    - **repository/**  
      Lógica de acceso a datos y comunicación con la API REST (por ejemplo, [`SongRepository`](app/src/main/java/com/app/rehearsalcloud/repository/SongRepository.kt), [`SetlistRepository`](app/src/main/java/com/app/rehearsalcloud/repository/SetlistRepository.kt), [`AuthRepository`](app/src/main/java/com/app/rehearsalcloud/repository/AuthRepository.kt)).
    - **viewmodel/**  
      ViewModels para manejar el estado y la lógica de UI (por ejemplo, [`SongViewModel`](app/src/main/java/com/app/rehearsalcloud/viewmodel/SongViewModel.kt), [`SetlistViewModel`](app/src/main/java/com/app/rehearsalcloud/viewmodel/SetlistViewModel.kt), [`AuthViewModel`](app/src/main/java/com/app/rehearsalcloud/viewmodel/AuthViewModel.kt)).
    - **api/**  
      Configuración de Retrofit y servicios de API ([`RetrofitClient`](app/src/main/java/com/app/rehearsalcloud/api/RetrofitClient.kt)).
    - **ui/**  
      Pantallas y componentes de Jetpack Compose:
        - **song/**  
          Gestión de canciones ([`SongManagerView`](app/src/main/java/com/app/rehearsalcloud/ui/song/SongManagerView.kt)).
        - **setlist/**  
          Gestión de setlists ([`SetlistManagerView`](app/src/main/java/com/app/rehearsalcloud/ui/setlist/SetlistManagerView.kt), [`EditSetlistScreen`](app/src/main/java/com/app/rehearsalcloud/ui/setlist/EditSetlistScreen.kt)).
    - **MainActivity.kt**  
      Punto de entrada principal, navegación y composición de la UI.
    - **LoginActivity.kt** y **RegisterActivity.kt**  
      Pantallas de autenticación de usuario.

- **build.gradle.kts, settings.gradle.kts**  
  Configuración de Gradle y dependencias.

## Principales Funcionalidades

- **Autenticación de usuarios:**  
  Registro e inicio de sesión usando [`AuthViewModel`](app/src/main/java/com/app/rehearsalcloud/viewmodel/AuthViewModel.kt) y [`AuthRepository`](app/src/main/java/com/app/rehearsalcloud/repository/AuthRepository.kt).

- **Gestión de canciones:**  
  Crear, editar, eliminar y listar canciones, incluyendo la subida de archivos ZIP y portadas de canciones.  
  UI: [`SongManagerView`](app/src/main/java/com/app/rehearsalcloud/ui/song/SongManagerView.kt)  
  Lógica: [`SongViewModel`](app/src/main/java/com/app/rehearsalcloud/viewmodel/SongViewModel.kt), [`SongRepository`](app/src/main/java/com/app/rehearsalcloud/repository/SongRepository.kt)

- **Gestión de setlists:**  
  Crear, editar, eliminar y listar setlists, asociando canciones a cada setlist.  
  UI: [`SetlistManagerView`](app/src/main/java/com/app/rehearsalcloud/ui/setlist/SetlistManagerView.kt), [`EditSetlistScreen`](app/src/main/java/com/app/rehearsalcloud/ui/setlist/EditSetlistScreen.kt)  
  Lógica: [`SetlistViewModel`](app/src/main/java/com/app/rehearsalcloud/viewmodel/SetlistViewModel.kt), [`SetlistRepository`](app/src/main/java/com/app/rehearsalcloud/repository/SetlistRepository.kt)

- **Reproducción de audio:**  
  Permite reproducir archivos de audio asociados a las canciones.

- **Persistencia local y sincronización:**  
  Usa Room para almacenamiento local y Retrofit para sincronizar con el backend.

## Arquitectura

- **MVVM (Model-View-ViewModel):**  
  - **Model:** Entidades Room y DTOs.
  - **ViewModel:** Maneja el estado y lógica de UI.
  - **Repository:** Abstracción de acceso a datos (local y remoto).
  - **View (UI):** Composables de Jetpack Compose.

## Configuración y Ejecución

1. **Requisitos:**  
   - Android Studio Electric Eel o superior  
   - JDK 11  
   - Emulador o dispositivo físico con Android 8.0+

2. **Clonar el repositorio y abrir en Android Studio.**

3. **Configurar el backend:**  
   - Modifica `BASE_URL` en [`Constants.kt`](app/src/main/java/com/app/rehearsalcloud/common/Constants.kt) si tu API no está en `http://10.0.2.2:5198/api/` (Si tu api esta en una red local debes reemplazar la IP por la direccion IP del dispositivo que aloja el API).

4. **Ejecutar el proyecto:**  
   - Haz clic en "Run" en Android Studio o usa `./gradlew assembleDebug`.

## Dependencias principales

- Jetpack Compose
- Room
- Retrofit + Moshi
- Coil (carga de imágenes)
- Material3

## Notas

- El proyecto está preparado para pruebas instrumentadas y unitarias.
- La navegación se realiza con Navigation Compose.
- El almacenamiento de archivos (audio, imágenes) se realiza en el almacenamiento interno de la app.

---

Para detalles específicos de cada módulo, revisa los archivos fuente y comentarios en el código.
