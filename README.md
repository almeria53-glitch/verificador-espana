# VERIFICADOR para Android

Aplicación Android nativa inicial para consultar matrículas españolas.

## Funciones incluidas en esta primera versión
- Introducción manual de matrícula.
- Escáner con cámara y reconocimiento de texto mediante ML Kit.
- Validación básica del formato actual de matrícula española.
- Pantalla de resultado que distingue claramente una lectura de matrícula de una verificación oficial.

## Importante sobre los datos
Esta versión **todavía no determina** si un vehículo es TAXI, VTC o particular. No hay conexión incorporada con los registros oficiales. El resultado se marca como pendiente para evitar presentar una lectura OCR como una verificación real. La siguiente fase requiere estudiar y conseguir acceso autorizado a fuentes oficiales y definir qué datos de titulares/empresas pueden mostrarse legalmente.

## Compilar
El workflow de GitHub Actions compila el APK de depuración con Java 17, Gradle 8.9 y Android SDK 35.

También se puede compilar localmente con Gradle 8.9:
```bash
gradle assembleDebug
```

APK generado:
`app/build/outputs/apk/debug/app-debug.apk`

## Identificador
`es.verificador.app`
