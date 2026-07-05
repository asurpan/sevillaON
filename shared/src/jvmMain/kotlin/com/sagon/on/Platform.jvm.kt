package com.sagon.on

class JvmPlatform : Platform {
    override val name: String = "JVM"
}

actual fun getPlatform(): Platform = JvmPlatform()

actual fun playScanClick() {}

actual fun vibratePtt() {}

actual fun triggerUiSound(type: String) {}

actual fun getTimeMillis(): Long = System.currentTimeMillis()

actual fun tryOpenNativeApp() {}

actual fun playIntroMusic() {}

actual fun stopIntroMusic() {}

actual fun playWelcomeSequence() {}

actual fun getCurrentHour(): Int = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

actual fun setVirtualOperatorText(text: String) {}

actual fun showSystemNotification(title: String, message: String) {}
