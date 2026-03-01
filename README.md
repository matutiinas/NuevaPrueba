# SportSwipe (Kotlin Multiplatform MVP)

App Tinder-like orientada a deporte con arquitectura Clean + MVVM + SOLID.

## Módulos
- `:shared` (KMP): domain/data/presentation, SQLDelight, reglas de negocio y ViewModels.
- `:androidApp`: Jetpack Compose con 5 tabs (Descubrir, Me gusta, Chats, Explorar, Perfil).
- `iosApp`: base SwiftUI compilable para integrar framework `shared`.

## Funcionalidades MVP
- 50 perfiles fake con dataset local (sin backend).
- Swipe like/nope + anuncios insertados en Discover (`adEveryN`).
- Match simulado por tabla/probabilidad simple.
- Lista de matches y chat local persistente.
- Explore con top-10 por compatibilidad y grupos.
- Perfil editable + ajustes premium (toggle).
- Límite de matches FREE por `freeMatchLimit`.

## Ejecutar Android
1. Abrir proyecto en Android Studio.
2. Sincronizar Gradle.
3. Ejecutar configuración `androidApp`.

## Ejecutar iOS
1. Generar framework shared (desde Android Studio o Gradle).
2. Abrir proyecto iOS en Xcode (crear target SwiftUI apuntando a carpeta `iosApp/iosApp`).
3. Enlazar framework `shared` y ejecutar en simulador.

## Configuración rápida
- Toggle premium: en tab Perfil (`ProfileViewModel.togglePremium`).
- Frecuencia de anuncios: `settings.adEveryN` (tabla `settings`).
- Límite matches free: `settings.freeMatchLimit`.

## Persistencia
SQLDelight tablas:
- `profiles`, `myProfile`, `likesGiven`, `likesReceived`, `matches`, `messages`, `settings`.

Seeding automático en primer arranque (`FakeDataSeeder`, meta key `seeded`).

## Tests
- Tests de dominio en `shared/src/commonTest` para:
  - generación de workout plan
  - límite de matches en FREE

## Troubleshooting de merges (importante)
Si Android Studio muestra errores tipo `Unresolved reference 'codex'` o similares en `build.gradle.kts`, casi siempre quedó texto basura de resolución de conflictos (por ejemplo `codex/...`, `main` o marcadores `<<<<<<<`).

Este repo incluye el workflow `.github/workflows/gradle-merge-sanity.yml` para detectar esos casos en PR antes de mergear.

### Recuperación rápida si Gradle se rompe por conflictos
Si ves errores como `Unresolved reference 'codex'` en `build.gradle.kts`:

1. Ejecuta:
   ```bash
   ./scripts/fix-conflicted-gradle.sh
