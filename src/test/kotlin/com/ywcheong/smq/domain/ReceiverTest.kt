package com.ywcheong.smq.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*


class ReceiverIdTest {

    @Test
    fun `should create ReceiverId with given id`() {
        val receiverId = ReceiverId("user123")
        assertEquals("user123", receiverId.id)
    }
}

class ReceiverTest {

    @Test
    fun `should create Receiver with up to MAX_SUBSCRIPTIONS topics`() {
        val topics = (1..Receiver.MAX_SUBSCRIPTIONS).map { PushTopic("topic$it") }.toSet()
        val receiver = Receiver(ReceiverId("user1"), topics)
        assertEquals(Receiver.MAX_SUBSCRIPTIONS, receiver.subscription.size)
    }

    @Test
    fun `should throw IllegalArgumentException when subscription exceeds MAX_SUBSCRIPTIONS`() {
        val topics = (1..(Receiver.MAX_SUBSCRIPTIONS + 1)).map { PushTopic("topic$it") }.toSet()
        val exception = assertThrows(IllegalStateException::class.java) {
            Receiver(ReceiverId("user2"), topics)
        }
        assertTrue(exception.message!!.contains("최대"))
    }
}

class ReceiverSubscriptionTest {

    private val receiverId = ReceiverId("user3")
    private val topicA = PushTopic("A")
    private val topicB = PushTopic("B")

    @Test
    fun `subscribe should add topic to subscription`() {
        val receiver = Receiver(receiverId, setOf(topicA))
        val updated = receiver.subscribe(topicB)
        assertTrue(updated.subscription.contains(topicB))
        assertEquals(2, updated.subscription.size)
    }

    @Test
    fun `unsubscribe should remove topic from subscription`() {
        val receiver = Receiver(receiverId, setOf(topicA, topicB))
        val updated = receiver.unsubscribe(topicA)
        assertFalse(updated.subscription.contains(topicA))
        assertEquals(1, updated.subscription.size)
    }

    @Test
    fun `unsubscribe multiple times should remove topic from subscription`() {
        val receiver = Receiver(receiverId, setOf(topicA, topicB))
        val updatedFirst = receiver.unsubscribe(topicA)
        val updatedNext = updatedFirst.unsubscribe(topicA)
        assertFalse(updatedNext.subscription.contains(topicA))
        assertEquals(1, updatedNext.subscription.size)
    }

    @Test
    fun `subscribe should not exceed MAX_SUBSCRIPTIONS`() {
        val topics = (1..Receiver.MAX_SUBSCRIPTIONS).map { PushTopic("T$it") }.toSet()
        val receiver = Receiver(receiverId, topics)
        val newTopic = PushTopic("Extra")
        val exception = assertThrows(IllegalStateException::class.java) {
            receiver.subscribe(newTopic)
        }
        assertTrue(exception.message!!.contains("최대"))
    }
}

class ReceiverCanReceiveTest {

    private val receiverId = ReceiverId("user4")
    private val topicA = PushTopic("A")
    private val topicB = PushTopic("B")
    private val title = PushTitle("Title")
    private val body = PushBody("Body")
    private val date = Date()

    @Test
    fun `canReceive should return true if any topic matches`() {
        val receiver = Receiver(receiverId, setOf(topicA, topicB))
        val pushUnit = PushUnit(title, body, setOf(topicB), date)
        assertTrue(receiver.canReceive(pushUnit))
    }

    @Test
    fun `canReceive should return false if no topic matches`() {
        val receiver = Receiver(receiverId, setOf(topicA))
        val pushUnit = PushUnit(title, body, setOf(topicB), date)
        assertFalse(receiver.canReceive(pushUnit))
    }

    @Test
    fun `receive should create PushDeliveredEvent with correct data`() {
        val receiver = Receiver(receiverId, setOf(topicA))
        val pushUnit = PushUnit(title, body, setOf(topicA), date)
        val event = receiver.receive(pushUnit)
        assertEquals(receiverId, event.receiverId)
        assertEquals(pushUnit, event.pushUnit)
    }
}