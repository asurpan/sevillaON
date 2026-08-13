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
                    // 🚀 SPEED-BOOST: Servir solo archivos específicos en lugar de escanear toda la raíz
                    static = mutableListOf(
                        project.projectDir.parentFile.resolve("index.html").absolutePath,
                        project.projectDir.parentFile.resolve("logo.png").absolutePath,
                        project.projectDir.parentFile.resolve("manifest.json").absolutePath,
                        project.projectDir.parentFile.resolve("sw.js").absolutePath,
                        project.projectDir.parentFile.resolve("google9312739a0adb99dd.html").absolutePath
                    ) 
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
