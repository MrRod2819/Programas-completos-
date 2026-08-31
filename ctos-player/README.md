# ctOS Player

Reproductor de música local para Android con estética cyberpunk *DedSec Pop*, escrito 100 % en Kotlin con Jetpack Compose y AndroidX Media3.

## Requisitos

- JDK 17
- Android SDK 34 (`compileSdk`/`targetSdk` 34, `minSdk` 26)

## Compilar

```bash
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Arquitectura

| Paquete | Contenido |
| --- | --- |
| `core/theme` | Tokens de color, tipografía, tema dinámico y fondo cyberpunk |
| `data/model` | `Song`, `ThemePalette`, `RingStyle`, estado del reproductor y de la biblioteca |
| `data/repository` | Escáner de `MediaStore` y persistencia con DataStore |
| `service` | `PlaybackService` (Media3 `MediaSessionService`) y `PlaybackManager` (`MediaController`) |
| `ui/components` | Disco de vinilo en `Canvas`, mini reproductor, glow modifiers, shimmer CRT |
| `ui/screens` | Home, Now Playing, Cola, Ajustes, Búsqueda y Perfil |

## Funcionalidad

- Escaneo de audio local (mp3, flac, wav, m4a, ogg) ignorando pistas de menos de 30 s.
- Reproducción en segundo plano con notificación del sistema, foco de audio y pausa al desconectar auriculares.
- Permisos en tiempo de ejecución (`READ_MEDIA_AUDIO`, `POST_NOTIFICATIONS`) para Android 13+.
- Cinco paletas persistidas y cuatro estilos de anillo del disco.
- Animaciones de entrada escalonada y esqueletos con barrido tipo CRT durante el escaneo.
