<div align="center">
  <img src="app/src/main/res/drawable/app_icon.png" width="128" alt="Icono de Visto">
  <h1>Visto</h1>
  <p>Tu biblioteca personal de películas y series.</p>
</div>

Visto es una aplicación Android en español para guardar películas y series, organizar listas y registrar el progreso de lo que ves. Nace como una versión personal de [WatchMaster](https://github.com/PranshulGG/WatchMaster) y mantiene el proyecto original como `upstream`.

## Características

- Búsqueda de películas, series y personas mediante TMDB.
- Listas de pendientes, en curso y terminadas.
- Seguimiento de temporadas y episodios.
- Valoraciones, notas, fechas y listas personalizadas.
- Interfaz Jetpack Compose con Material 3 Expressive.
- Contenido de TMDB en español de España (`es-ES`).
- Proveedores de streaming disponibles en España.
- Exportación e importación local en JSON.

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
