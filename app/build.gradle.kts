import java.util.Properties


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // KSP: genera el código de Room (DAO, base de datos) al compilar.
    alias(libs.plugins.ksp)
}

// ---------------------------------------------------------------------------------------
// Firma del APK release (opcional). Los datos de la llave NUNCA se guardan en el repositorio.
// Se leen, en este orden, de:
//   1. keystore.properties (en la raíz del proyecto; está en .gitignore; copia keystore.properties.example)
//   2. gradle.properties del usuario (~/.gradle/gradle.properties) o variables de entorno
// Si falta alguno de los cuatro datos, el build release se genera igual, sin firmar (y el APK
// sin firmar no se puede instalar). Con el asistente de Android Studio (Build > Generate Signed
// App Bundle / APK) no hace falta nada de esto.
//   storeFile      ruta ABSOLUTA del archivo .jks     (o LIBROMPAZ_STORE_FILE)
//   storePassword  clave del almacén                  (o LIBROMPAZ_STORE_PASSWORD)
//   keyAlias       alias de la llave                  (o LIBROMPAZ_KEY_ALIAS)
//   keyPassword    clave de la llave                  (o LIBROMPAZ_KEY_PASSWORD)
// ---------------------------------------------------------------------------------------
val propiedadesDeFirma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}
val datoDeFirma = { nombre: String, alternativa: String ->
    propiedadesDeFirma.getProperty(nombre)
        ?: providers.gradleProperty(alternativa).orNull
        ?: System.getenv(alternativa)
}
val rutaAlmacen: String? = datoDeFirma("storeFile", "LIBROMPAZ_STORE_FILE")
val claveAlmacen: String? = datoDeFirma("storePassword", "LIBROMPAZ_STORE_PASSWORD")
val aliasLlave: String? = datoDeFirma("keyAlias", "LIBROMPAZ_KEY_ALIAS")
val claveLlave: String? = datoDeFirma("keyPassword", "LIBROMPAZ_KEY_PASSWORD")
val firmaCompleta = listOf(rutaAlmacen, claveAlmacen, aliasLlave, claveLlave).all { !it.isNullOrBlank() }

// ---------------------------------------------------------------------------------------
// Experiencia 3 (API). Los dos valores se definen en gradle.properties y llegan a la app por BuildConfig:
//   USAR_API      SOLO define el valor INICIAL del interruptor "Modo local (sin servidor)" de la app:
//                 false = parte en modo local (todo en Room, sin red; es como se entrega)
//                 true  = parte en modo servidor (lo que se completa en los TODO(E3-n))
//                 Después, el modo se cambia en tiempo de ejecución desde "Servidor" en el login y esa
//                 elección queda guardada en el dispositivo y manda sobre USAR_API.
//   API_BASE_URL  dirección inicial del servidor. Es solo el valor de partida: en modo servidor se cambia desde
//                 "Servidor" en el login, sin recompilar. Solo se aceptan servidores de la red local.
// ---------------------------------------------------------------------------------------
val usarApi: Boolean = providers.gradleProperty("USAR_API").orElse("false").get().trim().equals("true", ignoreCase = true)
val apiBaseUrl: String = providers.gradleProperty("API_BASE_URL").orElse("http://10.0.2.2:3000/").get()

android {
    namespace = "com.librompaz.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.librompaz.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("boolean", "USAR_API", "$usarApi")
    }

    signingConfigs {
        if (firmaCompleta) {
            create("release") {
                storeFile = file(rutaAlmacen!!)
                storePassword = claveAlmacen
                keyAlias = aliasLlave
                keyPassword = claveLlave
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (firmaCompleta) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // --- Navegación entre pantallas ---
    // navigation-compose 2.7.7 ya incluye el runtime de navegación. NO agregar
    // "navigation-runtime-android": esa coordenada no existe en la versión 2.7.7.
    implementation(libs.androidx.navigation.compose)

    // --- Íconos (flecha atrás, mostrar/ocultar clave, errores, etc.) ---
    implementation(libs.androidx.material.icons.extended)

    // --- Persistencia local: Room (con KSP) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // --- Consumo de la API local: Retrofit + Gson sobre OkHttp ---
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    // El registro de peticiones solo existe en el build debug (ver src/debug y src/release).
    debugImplementation(libs.okhttp.logging)

    // --- Dependencias estándar del proyecto (template de Android Studio) ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // --- Pruebas ---
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
