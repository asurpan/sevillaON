package com.sagon.on

/**
 * Platform definitions
 */
interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun playScanClick()

expect fun vibratePtt()

expect fun triggerUiSound(type: String)

expect fun getTimeMillis(): Long

expect fun tryOpenNativeApp()

expect fun playIntroMusic()

expect fun stopIntroMusic()

expect fun playWelcomeSequence()

expect fun getCurrentHour(): Int

expect fun getCurrentFormattedTime(): String

expect fun setVirtualOperatorText(text: String)

expect fun showSystemNotification(title: String, message: String)

expect fun fetchTourismInfo(city: String, callback: (String?) -> Unit)

expect fun fetchLiveNewsTopic(city: String, callback: (String?) -> Unit)

expect fun fetchGeminiApi(endpoint: String, jsonBody: String, callback: (String?) -> Unit)
