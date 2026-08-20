import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.3.4"
    id("com.google.dagger.hilt.android")
    id("androidx.room")
}
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

val tmdbApiKey: String = localProps.getProperty("TMDB_API_KEY")
    ?: throw GradleException(
        "TMDB_API_KEY not found! Add it to local.properties in the project root."
    )


android {
    namespace = "com.dwightsckrute.visto"
    compileSdk = 36
    android.buildFeatures.buildConfig = true
    defaultConfig {
        // A different application id lets Visto coexist with the
        // original app, so installing this fork never replaces its local data.
        applicationId = "com.dwightsckrute.visto"
        minSdk = 24
        targetSdk = 36
        versionCode = 14
        versionName = "1.9.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Un id distinto permite instalar una build de pruebas junto a la Visto real,
            // con su propio sandbox, sin arriesgar los datos ni la firma de la instalación.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            // R8 desactivado, y no por descuido.
            //
            // Encenderlo redujo el APK de 20 MB a 5,1 MB, y rompió la aplicación: al navegar
            // entre secciones petaba con ClassCastException dentro de las factorías generadas de
            // Hilt. R8 estaba reescribiendo tipos del grafo de inyección que solo se resuelven en
            // tiempo de ejecución. Comprobado en el dispositivo, no deducido.
            //
            // Volver a intentarlo requiere las reglas de Dagger/Hilt y probar cada pantalla
            // instalando la release, no basta con que compile. Hasta entonces esto se queda
            // apagado: una aplicación que arranca pesando más es mejor que una que se cierra.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }


    buildFeatures {
        compose = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }


}

room {
    schemaDirectory("$projectDir/schemas")
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    // Material 3 se fija por versión (no por el BOM) porque Visto depende de las APIs
    // Expressive, que solo existen en la línea 1.5.0-alpha.
    implementation(libs.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.materialKolor)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.animation.core)
    implementation(libs.core.splashscreen)
    implementation(libs.androidx.work.runtime)
    // Instala el perfil de arranque que Compose trae empaquetado, que sin esto no se aplica.
    implementation(libs.androidx.profileinstaller)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.coil.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp.logging)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
}
