package com.sagon.on

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun playScanClick() {}

actual fun vibratePtt() {}

actual fun triggerUiSound(type: String) {}

actual fun getTimeMillis(): Long = (kotlinx.browser.window.performance.now()).toLong()

actual fun tryOpenNativeApp() {}

actual fun playIntroMusic() {}

actual fun stopIntroMusic() {}

actual fun playWelcomeSequence() {}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => new Date().getHours()")
private external fun getJsHours(): Int

actual fun getCurrentHour(): Int = getJsHours()

actual fun setVirtualOperatorText(text: String) {
    // Implementación para Wasm si fuera necesario
}

actual fun showSystemNotification(title: String, message: String) {
    // Implementación para Wasm si fuera necesario
}

actual fun fetchTourismInfo(city: String, callback: (String?) -> Unit) {
    callback(null)
}
