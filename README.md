<div align="center">
  <img src="app/src/main/res/drawable/app_icon.png" width="128" alt="Icono de Visto">
  <h1>Visto</h1>
  <p>Tu biblioteca personal de películas y series.</p>
  <p><a href="https://github.com/dwightsckrute/Visto/releases/latest">Descargar la última versión</a></p>
</div>

Visto es una aplicación Android en español e inglés para guardar películas y series, organizar listas y registrar el progreso de lo que ves. Nace como una versión personal de [WatchMaster](https://github.com/PranshulGG/WatchMaster) y mantiene el proyecto original como `upstream`.

## Características

- Búsqueda de películas, series y personas mediante TMDB.
- Listas de pendientes, en curso y terminadas.
- Seguimiento de temporadas y episodios.
- Inicio personal con estadísticas, contenido en curso y actividad reciente.
- Valoraciones, notas, fechas y listas personalizadas.
- Interfaz Jetpack Compose con Material 3 Expressive.
- Selector de interfaz en español o inglés; las nuevas consultas de TMDB siguen el idioma elegido.
- Proveedores de streaming disponibles en España.
- Exportación e importación local en JSON.
- Copias de seguridad automáticas diarias o semanales en un archivo elegido por el usuario.

## Copias de seguridad automáticas

En **Ajustes > Datos > Copia de seguridad automática**, selecciona frecuencia diaria o semanal y elige un archivo. Visto obtiene permiso persistente para actualizar ese mismo archivo mediante WorkManager, incluso después de reiniciar el dispositivo.

El archivo utiliza el mismo formato JSON v4 que la exportación manual e incluye biblioteca, temporadas y listas. Guárdalo en una ubicación sincronizada o que copies fuera del teléfono: al desinstalar una aplicación, Android puede eliminar sus datos privados.

## Identidad independiente

Visto usa el identificador Android `com.dwightsckrute.visto`. Puede instalarse junto a WatchMaster y no sustituye ni accede directamente a la base de datos privada de la aplicación original.

No cambies este identificador después de empezar a utilizar Visto. Android interpretaría el nuevo identificador como otra aplicación sin acceso a sus datos.

## Migrar desde WatchMaster sin perder datos

1. No desinstales WatchMaster ni borres sus datos.
2. En la aplicación original abre **Settings > Data > Export app data**.
3. Activa **Include lists** y guarda el archivo JSON.
4. Instala una versión release de Visto firmada con tu clave permanente.
5. En Visto abre **Ajustes > Datos > Importar datos de la aplicación**.
6. Selecciona el JSON y comprueba películas, series, temporadas, estados, valoraciones, notas y listas.
7. Conserva el JSON y la aplicación original hasta verificar que todo se ha migrado correctamente.

La copia v4 contiene la lista de seguimiento, las temporadas y las listas personalizadas. Las preferencias visuales y algunos datos de caché se vuelven a crear al utilizar Visto.

> Si WatchMaster no muestra la opción de exportar, actualízalo por su canal habitual sin desinstalarlo. Un APK con una firma diferente no puede instalarse encima de la aplicación original.

## Requisitos

- Android Studio y Android SDK 36.
- JDK 17.
- Android 7.0 o posterior (`minSdk 24`).
- Una clave de API v3 de TMDB.

## Configuración de TMDB

1. Crea una cuenta o inicia sesión en [TMDB](https://www.themoviedb.org/).
2. Solicita una clave desde **Ajustes > API** para uso personal/no comercial.
3. Copia `local.properties.example` como `local.properties`.
4. Configura el SDK y tu clave:

```properties
sdk.dir=/ruta/a/Android/Sdk
TMDB_API_KEY=TU_CLAVE_V3_DE_TMDB
```

`local.properties` está excluido de Git para evitar publicar la clave.

## Compilación de desarrollo

```bash
./gradlew assembleDebug
```

El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`. Esta variante utiliza la clave de depuración del equipo y no debe ser la primera instalación definitiva si quieres conservar una ruta de actualización estable.

## Descargar e instalar actualizaciones

Los APK firmados se publican en [GitHub Releases](https://github.com/dwightsckrute/Visto/releases). Descarga `Visto-x.y.z.apk` desde el móvil, ábrelo y confirma **Actualizar**. Android conservará la biblioteca siempre que el APK esté firmado con la misma clave.

El workflow `.github/workflows/release.yml` ejecuta pruebas y lint, crea el APK release y lo publica automáticamente al subir una etiqueta `v*`:

```bash
git tag v1.1.0
git push origin v1.1.0
```

El repositorio necesita los secretos `TMDB_API_KEY`, `SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS` y `SIGNING_KEY_PASSWORD`. No se guardan en Git ni aparecen en los logs. La clave privada de firma nunca se incluye en el APK; como sucede en cualquier cliente móvil de TMDB, la clave de API sí forma parte de la aplicación compilada y no debe considerarse un secreto irrecuperable.

## Firma permanente y APK definitivo

Genera una sola clave y consérvala durante toda la vida de Visto:

```bash
keytool -genkeypair -v \
  -keystore visto-release.jks \
  -alias visto \
  -keyalg RSA -keysize 4096 -validity 10000

cp keystore.properties.example keystore.properties
```

Completa `keystore.properties` con la ubicación, el alias y las contraseñas de la clave. Después ejecuta:

```bash
./gradlew assembleRelease
```

El APK firmado se genera en `app/build/outputs/apk/release/app-release.apk`. Los archivos `*.jks`, `*.keystore` y `keystore.properties` están excluidos de Git.

Guarda copias seguras de `visto-release.jks` y `keystore.properties`. Si pierdes la clave, Android no permitirá instalar nuevas versiones como actualización de la aplicación existente.

## Mantener Visto actualizado

```bash
git remote add upstream https://github.com/PranshulGG/WatchMaster.git
git fetch upstream
git merge upstream/master
```

Antes de publicar una actualización, incrementa `versionCode` y `versionName`, prueba la importación de una copia reciente y firma el APK con la misma clave.

## Tecnología

- Kotlin
- Jetpack Compose
- Material 3 Expressive
- Room
- Retrofit y OkHttp
- Hilt
- TMDB API

## Origen, licencia y atribución

Visto es un fork de WatchMaster y conserva su licencia [GPL-3.0](LICENSE). Si distribuyes un APK modificado, debes ofrecer también el código fuente correspondiente y mantener los avisos de autoría y licencia.

Este producto utiliza la API de TMDB, pero TMDB no lo respalda ni certifica.
