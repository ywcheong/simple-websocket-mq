package com.ywcheong.smq.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Date

class PushTitleTest {

    @Test
    fun `should create PushTitle when value length is within limits`() {
        val title = PushTitle("Valid Title")
        assertEquals("Valid Title", title.value)
    }

    @Test
    fun `should throw IllegalArgumentException when value is empty`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            PushTitle("")
        }
        assertTrue(exception.message!!.contains("필수 필드"))
    }

    @Test
    fun `should throw IllegalArgumentException when value length exceeds max`() {
        val longTitle = "a".repeat(PushTitle.MAX_TITLE_LENGTH + 1)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            PushTitle(longTitle)
        }
        assertTrue(exception.message!!.contains("이하여야 합니다"))
    }
}

class PushBodyTest {

    @Test
    fun `should create PushBody when value length is within limits`() {
        val body = PushBody("Valid Body")
        assertEquals("Valid Body", body.value)
    }

    @Test
    fun `should throw IllegalArgumentException when value is empty`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            PushBody("")
        }
        assertTrue(exception.message!!.contains("필수 필드"))
    }

    @Test
    fun `should throw IllegalArgumentException when value length exceeds max`() {
        val longBody = "a".repeat(PushBody.MAX_BODY_LENGTH + 1)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            PushBody(longBody)
        }
        assertTrue(exception.message!!.contains("이하여야 합니다"))
    }
}

class PushTopicTest {

    @Test
    fun `should create PushTopic when value length is within limits`() {
        val topic = PushTopic("ValidTopic")
        assertEquals("ValidTopic", topic.value)
    }

    @Test
    fun `should throw IllegalArgumentException when value is empty`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            PushTopic("")
        }
        assertTrue(exception.message!!.contains("비어 있을 수 없습니다"))
    }

    @Test
    fun `should throw IllegalArgumentException when value length exceeds max`() {
        val longTopic = "a".repeat(PushTopic.MAX_TOPIC_LENGTH + 1)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            PushTopic(longTopic)
        }
        assertTrue(exception.message!!.contains("이하여야 합니다"))
    }
}

class PushUnitTest {

    @Test
    fun `should create PushUnit with valid fields`() {
        val title = PushTitle("Valid Title")
        val body = PushBody("Valid Body")
        val topics = setOf(PushTopic("Topic1"), PushTopic("Topic2"))
        val date = Date()

        val pushUnit = PushUnit(title, body, topics, date)

        assertEquals(title, pushUnit.title)
        assertEquals(body, pushUnit.body)
        assertEquals(topics, pushUnit.topics)
        assertEquals(date, pushUnit.occuredAt)
    }

    @Test
    fun `should create PushUnit with empty topics`() {
        val title = PushTitle("Valid Title")
        val body = PushBody("Valid Body")
        val emptyTopics = emptySet<PushTopic>()
        val date = Date()

        val pushUnit = PushUnit(title, body, emptyTopics, date)

        assertEquals(title, pushUnit.title)
        assertEquals(body, pushUnit.body)
        assertEquals(emptyTopics, pushUnit.topics)
        assertEquals(date, pushUnit.occuredAt)
    }
}