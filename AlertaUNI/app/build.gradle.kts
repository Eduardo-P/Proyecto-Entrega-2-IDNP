import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    alias(libs.plugins.dagger.hilt)
    id("org.jetbrains.kotlin.plugin.serialization")
}

// Credenciales y opciones locales: se leen de local.properties (no se sube a git).
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun localProp(key: String, default: String = ""): String =
    localProps.getProperty(key) ?: default

android {
    namespace = "com.erns.alertauni"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.erns.alertauni"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${localProp("supabase.url", "https://myurl.supabase.co")}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${localProp("supabase.key", "supabase_key")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProp("google.webClientId", "12345678-abc.apps.googleusercontent.com")}\"")
    }

    buildTypes {
        debug {
            // SIMULADO: modo demo sin backend (solo debug). Se activa con alertauni.demo=true en local.properties.
            buildConfigField("boolean", "DEMO_MODE", localProp("alertauni.demo", "false"))
        }
        release {
            buildConfigField("boolean", "DEMO_MODE", "false")
            isMinifyEnabled = false
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
    sourceSets {
        // Robolectric lee los esquemas de Room desde los assets de la variante debug
        // (solo afecta al APK de depuración; son unos pocos KB).
        getByName("debug").assets.srcDirs("$projectDir/schemas")
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java", "src/main/kotlin")
        }
    }
}

dependencies {
    implementation("androidx.security:security-crypto:1.1.0")
    implementation("io.github.jan-tennert.supabase:functions-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.5.0")
    
    implementation("io.ktor:ktor-client-android:2.3.11")
    implementation("io.ktor:ktor-client-okhttp:2.3.11")
    implementation("io.ktor:ktor-client-cio:2.3.11")

    implementation(libs.dagger.hilt.android)
    implementation(libs.firebase.dataconnect)
    implementation(libs.generativeai)
    kapt(libs.dagger.hilt.compiler)
    kapt(libs.kotlinx.metadata.jvm)

    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Íconos Material del diseño (grupo, candado, bandera, reloj, archivo, chat)
    implementation("androidx.compose.material:material-icons-extended")

    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-auth")

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.firebase.firestore)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.runtime.livedata)

    // Google Sign-In and Credentials
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.android.gms:play-services-base:18.5.0")
    implementation(libs.androidx.credentials.core)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.identity.googleid)
    implementation(libs.google.play.services.auth) //agregado para manejar la autenticación de Google

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    testImplementation(libs.junit)
    // Pruebas locales (JVM) de Room y ViewModels con Robolectric
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation(libs.androidx.junit)
    testImplementation("androidx.room:room-testing:2.7.0-alpha11")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation("com.google.zxing:core:3.5.2")
}

// Exporta el esquema de Room para versionarlo y probar migraciones
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

configurations.all {
    exclude(group = "com.intellij", module = "annotations")
}