/**
 * PROTECTED CORE: CONFIGURACIÓN GLOBAL DEL PROYECTO
 * ESTADO: CONGELADO / NO MODIFICAR
 * - Gestión de plugins raíz.
 * - Estructura de dependencias heredadas.
 */

/**
 * PROTECTED CORE: CONFIGURACIÓN DE DEPENDENCIAS Y PLUGINS
 * ESTADO: CONGELADO / NO MODIFICAR
 * - Las versiones de Kotlin y Compose están sincronizadas con el motor de Audio Web.
 * - Cualquier cambio aquí puede romper la compatibilidad con el puente JS/Wasm.
 */

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

// 🔒 HARD-LOCK: LANZADOR AUTOMÁTICO DE SERVIDOR LOCAL (WEB)
// Al ejecutar esta tarea (triángulo en Gradle > run), se abrirá el navegador automáticamente.
tasks.register("run") {
    group = "application"
    description = "Inicia el servidor de desarrollo y abre la página automáticamente"
    dependsOn(":webApp:jsBrowserDevelopmentRun")
}
