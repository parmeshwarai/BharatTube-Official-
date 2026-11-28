package com.example

import com.example.chhotuai.ChhotuAiBackendClient
import com.example.chhotuai.OpenAiAssistantService
import com.example.data.model.ChhotuChatMessageEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OpenAiAssistantRobolectricTest {

    @Test
    fun openAiServiceSanitizesPayloadProtectsAdminSplitAndParsesJson() {
        val service = OpenAiAssistantService(keyProvider = { "OPENAI_API_KEY_DEFAULT_VALUE" })
        assertFalse(service.isConfigured())

        val userPayload = service.buildChatPayload(
            userMessage = "Email me at aarav@bharattube.in or 9876543210 for video ideas",
            isAdmin = false,
            languageCode = "hi",
            healthSnapshot = null,
            totalUsers = 5,
            totalVideos = 12,
            totalChannels = 3,
            creatorSharePct = 50,
            platformSharePct = 50,
            indiaThresholdInr = 2000.0,
            recentHistory = listOf(
                ChhotuChatMessageEntity(
                    id = "m1",
                    userId = "u1",
                    role = "USER",
                    text = "Namaste Chhotu AI",
                    languageCode = "hi",
                    sourceMode = "USER_INPUT"
                )
            )
        ).toString()

        assertFalse(userPayload.contains("aarav@bharattube.in"))
        assertFalse(userPayload.contains("9876543210"))
        assertFalse(userPayload.contains("50%"))
        assertFalse(userPayload.contains("OPENAI_API_KEY_DEFAULT_VALUE"))
        assertTrue(userPayload.contains("gpt-4o-mini"))
        assertTrue(userPayload.contains("Namaste Chhotu AI"))

        val adminPayload = service.buildChatPayload(
            userMessage = "Summarize channel status",
            isAdmin = true,
            languageCode = "en",
            healthSnapshot = null,
            totalUsers = 5,
            totalVideos = 12,
            totalChannels = 3,
            creatorSharePct = 50,
            platformSharePct = 50,
            indiaThresholdInr = 2000.0,
            recentHistory = emptyList()
        ).toString()
        assertTrue(adminPayload.contains("50% Creator"))

        val parsed = service.extractAssistantContent(
            """{"choices":[{"message":{"role":"assistant","content":"Namaste! Here are 3 viral title ideas."}}]}"""
        )
        assertEquals("Namaste! Here are 3 viral title ideas.", parsed)
    }

    @Test
    fun chhotuBackendClientPreservesBuiltInHealthAndOpenAiReadinessMetadata() {
        val client = ChhotuAiBackendClient(
            openAiService = OpenAiAssistantService(keyProvider = { "OPENAI_API_KEY_DEFAULT_VALUE" })
        )
        val state = client.probeHealth()
        assertTrue(state.isConfigured)
        assertTrue(state.isOnline)
        assertFalse(state.openAiConfigured)
        assertTrue(state.openAiStatusLabel.contains("OPENAI_API_KEY"))
    }
}
