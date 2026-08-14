# WatchMaster ES

Versión personal en español de [WatchMaster](https://github.com/PranshulGG/WatchMaster), una aplicación Android para organizar películas y series. El proyecto original se distribuye bajo licencia GPL-3.0.

## Qué cambia en este fork

- Interfaz y mensajes en español.
- Consultas a TMDB en español de España (`es-ES`).
- Proveedores de streaming disponibles en España.
- Fechas con formato español.
- Identificador Android independiente: `com.franciscoredondokuik.watchmaster`.
- Importación compatible con las copias JSON v4 de WatchMaster 1.8.0.

El identificador independiente permite instalar **WatchMaster ES y WatchMaster a la vez**. La instalación de este fork no sustituye ni borra la base de datos privada de la aplicación original.

## Migrar tus datos sin perderlos

1. No desinstales WatchMaster ni borres sus datos.
2. En la aplicación original abre **Settings > Data > Export app data**.
3. Activa **Include lists** y guarda el archivo JSON en una ubicación que puedas localizar.
4. Instala WatchMaster ES. Verás ambas aplicaciones en el móvil.
5. En WatchMaster ES abre **Ajustes > Datos > Importar datos de la aplicación** y selecciona el JSON.
6. Comprueba películas, series, temporadas, estados, valoraciones, notas y listas antes de plantearte eliminar la aplicación original.
7. Conserva el JSON como copia de seguridad.

La copia v4 contiene la lista de seguimiento, las temporadas y las listas personalizadas. Las preferencias visuales y algunos datos descargables de caché no se migran; se vuelven a crear al usar la nueva aplicación.

> Si tu versión original no ofrece la opción de exportar, actualízala por su canal habitual sin desinstalarla. No instales un APK firmado por otra persona encima de la aplicación original: Android lo rechazará por tener una firma diferente.

## Preparar el proyecto

Necesitas Android Studio con JDK 17 y una clave de API de [TMDB](https://www.themoviedb.org/settings/api).

1. Copia `local.properties.example` como `local.properties`.
2. Ajusta `sdk.dir` a la ruta de tu Android SDK.
3. Sustituye `TU_CLAVE_TMDB` por tu clave.
4. Compila con `./gradlew assembleDebug` o abre el proyecto en Android Studio.

El APK de desarrollo se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Firma estable para no perder datos en futuras actualizaciones

El APK de depuración solo debe usarse para probar. Antes de instalar tu versión definitiva, crea una clave de firma que conservarás para todas las versiones futuras:

```bash
keytool -genkeypair -v \
  -keystore watchmaster-es-release.jks \
  -alias watchmaster-es \
  -keyalg RSA -keysize 4096 -validity 10000
cp keystore.properties.example keystore.properties
```

Completa en `keystore.properties` las contraseñas que hayas elegido y compila con:

```bash
./gradlew assembleRelease
```

El APK definitivo estará en `app/build/outputs/apk/release/app-release.apk`. Guarda una copia segura de `watchmaster-es-release.jks`, del alias y de sus contraseñas. **Si pierdes esa clave, Android no permitirá actualizar WatchMaster ES con otra firma**; tendrías que exportar los datos, desinstalar e importar de nuevo. Los archivos de firma y sus contraseñas están excluidos de Git.

## Mantener el fork actualizado

El repositorio original debe conservarse como remoto `upstream`, mientras tu repositorio personal se usa como `origin`:

```bash
git remote add origin https://github.com/TU_USUARIO/WatchMaster.git
git fetch upstream
git merge upstream/master
```

Antes de publicar una versión nueva, incrementa `versionCode` y `versionName`, compila y prueba la importación usando una copia del JSON. No cambies el `applicationId` después de empezar a usar WatchMaster ES: Android lo trataría como una tercera aplicación sin acceso a sus datos.

## Licencia y atribución

Se conserva la licencia GPL-3.0 del proyecto original. Si distribuyes APKs de esta versión, publica también el código fuente correspondiente y conserva los avisos de autoría y licencia.
