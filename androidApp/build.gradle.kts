import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment.ktx) // 🛡️ FIX: Requerido para ActivityResult APIs en modo Release
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.compose.material3)
    
    // 💰 ADMOB SDK: Para monetización por publicidad
    implementation(libs.play.services.ads)

    // 🔔 FCM: Para notificaciones push que despiertan la app
    implementation(libs.firebase.messaging.ktx)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.sagon.on"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.sagon.on"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 78
        versionName = "78.Full"

        val localProps = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            localPropsFile.inputStream().use { localProps.load(it) }
        }
        val geminiKey1 = localProps.getProperty("GEMINI_API_KEY_1") ?: localProps.getProperty("GEMINI_API_KEY") ?: ""
        val geminiKey2 = localProps.getProperty("GEMINI_API_KEY_2") ?: ""
        val geminiKey3 = localProps.getProperty("GEMINI_API_KEY_3") ?: ""
        val combinedKeys = listOf(geminiKey1, geminiKey2, geminiKey3).filter { it.isNotBlank() }.joinToString(",")
        buildConfigField("String", "GEMINI_API_KEYS", "\"$combinedKeys\"")
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            // 🛡️ MOTOR DE FIRMA DINÁMICO: Carga desde keystore.properties o variables de entorno
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val props = Properties()
                keystorePropertiesFile.inputStream().use { stream ->
                    props.load(stream)
                }
                
                val sFile = props.getProperty("storeFile")
                if (sFile != null) {
                    storeFile = rootProject.file(sFile)
                }
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            } else {
                storeFile = file("release.keystore")
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("KEY_ALIAS") ?: ""
                keyPassword = System.getenv("KEY_PASSWORD") ?: ""
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val releaseKeystoreFile = file("release.keystore")
            if (keystorePropertiesFile.exists() || releaseKeystoreFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

val copyWebAssets = tasks.register<Copy>("copyWebAssets") {
    dependsOn(":webApp:jsBrowserDevelopmentExecutableDistribution")
    from(project(":webApp").layout.buildDirectory.dir("dist/js/developmentExecutable"))
    from(rootDir.resolve("index.html"))
    from(rootDir.resolve("logo.png"))
    from(rootDir.resolve("manifest.json"))
    from(rootDir.resolve("sw.js"))
    into(layout.projectDirectory.dir("src/main/assets"))
}

tasks.matching { it.name.startsWith("pre") && it.name.endsWith("Build") }.configureEach {
    dependsOn(copyWebAssets)
}