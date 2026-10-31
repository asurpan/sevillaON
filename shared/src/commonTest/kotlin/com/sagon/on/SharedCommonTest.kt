package com.sagon.on

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SharedCommonTest {

    @Test
    fun testChatMessageCreation() {
        val msg = ChatMessage(
            id = "msg123",
            senderNick = "ESTACION_1",
            text = "Hola a todos en la frecuencia",
            timestamp = 1700000000000L,
            isSystem = false,
            isPrivate = true
        )
        assertEquals("msg123", msg.id)
        assertEquals("ESTACION_1", msg.senderNick)
        assertEquals("Hola a todos en la frecuencia", msg.text)
        assertTrue(msg.isPrivate)
        assertFalse(msg.isSystem)
    }

    @Test
    fun testRadioStateDefaults() {
        val state = RadioState()
        assertEquals("SEVILLA", state.city)
        assertEquals("SEVILLA", state.channel)
        assertFalse(state.isChatMuted)
        assertTrue(state.isDspEnabled)
        assertTrue(state.isRogerBeepEnabled)
    }

    @Test
    fun testPeerSessionIdSanitization() {
        val rawNick = "ESTACIÓN 1 SEVILLA"
        val peerSafeNick = rawNick.replace(Regex("[^A-Za-z0-9_-]"), "_")
        assertEquals("ESTACI_N_1_SEVILLA", peerSafeNick)
        assertTrue(peerSafeNick.matches(Regex("^[A-Za-z0-9_-]+$")))
    }

    @Test
    fun testCityListsValidation() {
        assertTrue(SPAIN_CITIES.contains("SEVILLA"))
        assertTrue(SPAIN_CITIES.contains("MADRID"))
        assertTrue(ALL_CITIES.contains("BARCELONA"))
    }
}
