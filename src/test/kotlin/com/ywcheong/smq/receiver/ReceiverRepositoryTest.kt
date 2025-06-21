package com.ywcheong.smq.receiver

import com.ywcheong.smq.domain.PushTopic
import com.ywcheong.smq.domain.Receiver
import com.ywcheong.smq.domain.ReceiverId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ReceiverRepositoryTest {

    @Test
    fun `save should add a receiver`() {
        val repo = InMemoryReceiverRepository()
        val receiver = Receiver(ReceiverId("1"), setOf(PushTopic("value1")))
        val saved = repo.save(receiver)
        assertEquals(receiver, saved)
        assertEquals(receiver, repo.find(receiver.receiverId))
    }

    @Test
    fun `save should update a receiver`() {
        val repo = InMemoryReceiverRepository()
        val receiver = Receiver(ReceiverId("1"), setOf(PushTopic("value1")))
        repo.save(receiver)

        val newReceiver = Receiver(ReceiverId("1"), setOf(PushTopic("value2")))
        repo.save(newReceiver)

        val foundReceiver = repo.find(ReceiverId("1"))
        assertNotNull(foundReceiver)
        assertEquals(foundReceiver, newReceiver)
        assertNotEquals(foundReceiver, receiver)
    }

    @Test
    fun `delete should remove a receiver`() {
        val repo = InMemoryReceiverRepository()
        val receiver = Receiver(ReceiverId("2"), setOf(PushTopic("value2")))
        repo.save(receiver)
        repo.delete(receiver)
        assertNull(repo.find(receiver.receiverId))
    }

    @Test
    fun `find should return null if receiver not found`() {
        val repo = InMemoryReceiverRepository()
        assertNull(repo.find(ReceiverId("999")))
    }

    @Test
    fun `concurrent save, delete, and find should not cause race condition`() {
        val repo = InMemoryReceiverRepository()
        val receiverCount = 100000
        val threadCount = 32
        val receivers = List(receiverCount) { Receiver(ReceiverId(it.toString()), setOf(PushTopic("value$it"))) }

        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)

        repeat(threadCount) { threadIdx ->
            executor.submit {
                latch.await()
                for (i in 0 until receiverCount) {
                    val receiver = receivers[i]
                    when (threadIdx % 3) {
                        0 -> repo.save(receiver)
                        1 -> repo.delete(receiver)
                        2 -> repo.find(receiver.receiverId)
                    }
                }
                doneLatch.countDown()
            }
        }

        // Start all threads at the same time
        latch.countDown()
        // Wait for all threads to finish
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS), "Threads did not finish in time")

        // Check repository consistency: all receivers should be either present or absent, but never corrupted
        repeat(receiverCount) {
            val receiver = repo.find(ReceiverId(it.toString()))
            if (receiver != null) {
                assertEquals(receivers[it], receiver)
            }
        }
    }
}