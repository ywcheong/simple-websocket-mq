package com.ywcheong.smq.receiver

import com.ywcheong.smq.domain.PushTopic
import com.ywcheong.smq.domain.Receiver
import com.ywcheong.smq.domain.ReceiverId
import org.springframework.stereotype.Repository
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

interface ReceiverRepository {
    fun save(receiver: Receiver): Receiver
    fun delete(receiver: Receiver)
    fun find(receiverId: ReceiverId): Receiver?
    fun findAllWithTopics(topics: List<PushTopic>): List<Receiver>
}

@Repository
class InMemoryReceiverRepository : ReceiverRepository {
    private val lock = ReentrantReadWriteLock()
    private val memoryMap: MutableMap<ReceiverId, Receiver> = mutableMapOf()

    override fun save(receiver: Receiver): Receiver {
        lock.writeLock().withLock {
            memoryMap[receiver.receiverId] = receiver
            return receiver
        }
    }

    override fun delete(receiver: Receiver) {
        lock.writeLock().withLock {
            memoryMap.remove(receiver.receiverId)
        }
    }

    override fun find(receiverId: ReceiverId): Receiver? {
        lock.readLock().withLock {
            return memoryMap[receiverId]
        }
    }

    override fun findAllWithTopics(topics: List<PushTopic>): List<Receiver> {
        lock.readLock().withLock {
            return memoryMap.filter {
                val receiver = it.value
                topics.any { eachTopic ->
                    receiver.isSubscribed(eachTopic)
                }
            }.map { it.value }
        }
    }

}