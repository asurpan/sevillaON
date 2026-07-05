plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer()).apply {
                    open = true // 🔒 HARD-LOCK: Lanzar navegador automáticamente
                    port = 8080
                    // 🔒 HARD-LOCK: Servir desde la raíz para encontrar index.html principal
                    static = mutableListOf(project.projectDir.parentFile.absolutePath) 
                }
                // 🔒 HARD-LOCK: El nombre del archivo DEBE coincidir con index.html
                outputFileName = "webApp.js"
                // 🚀 SPEED-BOOST: Desactivar todo lo que ralentiza la carga local
                sourceMaps = false
            }
        }
        binaries.executable()
    }

    sourceSets {
        val webMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(projects.shared)
                implementation(libs.compose.ui)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.materialIconsExtended)
            }
        }
        jsMain.get().dependsOn(webMain)
    }
}

// 🔒 HARD-LOCK: ALIAS PARA EJECUCIÓN RÁPIDA
tasks.register("run") {
    group = "application"
    dependsOn("jsBrowserDevelopmentRun")
}
