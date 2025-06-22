package com.ywcheong.smq.receiver

import com.ywcheong.smq.domain.PushTopic
import com.ywcheong.smq.domain.PushUnit
import com.ywcheong.smq.domain.Receiver
import com.ywcheong.smq.domain.ReceiverId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.util.*

private val logger_ = KotlinLogging.logger {}

// 수신자가 연결, 구독, 구독해제, 연결해제 행동을 취할 때 이를 기록합니다.
interface ReceiverService {
    fun connected(id: String)
    fun disconnected(id: String)
    fun subscribe(id: String, topic: String)
    fun unsubscribe(id: String, topic: String)
}

@Service
class DefaultReceiverService(
    private val receiverRepository: ReceiverRepository
) : ReceiverService {
    override fun connected(id: String) {
        val receiver = Receiver(ReceiverId(id), subscription = emptySet())
        receiverRepository.save(receiver)
    }

    override fun disconnected(id: String) {
        val receiver = receiverRepository.find(ReceiverId(id))
        check(receiver != null) {
            throw IllegalStateException("ReceiverRepository 에서 Principal 대응 Receiver 를 찾지 못했습니다. (Principal=${id})")
        }

        receiverRepository.delete(receiver)
    }

    override fun subscribe(id: String, topic: String) {
        val receiver = receiverRepository.find(ReceiverId(id))

        check(receiver != null) {
            throw IllegalStateException("ReceiverRepository 에서 Principal 대응 Receiver 를 찾지 못했습니다. (Principal=${id})")
        }

        val newTopic = PushTopic(topic)

        check(!receiver.isSubscribed(newTopic)) {
            throw IllegalArgumentException("이미 구독 중입니다.")
        }

        val newReceiver = receiver.subscribe(newTopic)
        receiverRepository.save(newReceiver)
    }

    override fun unsubscribe(id: String, topic: String) {
        val receiver = receiverRepository.find(ReceiverId(id))

        check(receiver != null) {
            throw IllegalStateException("ReceiverRepository 에서 Principal 대응 Receiver 를 찾지 못했습니다. (Principal=${id})")
        }

        val oldTopic = PushTopic(topic)

        check(receiver.isSubscribed(oldTopic)) {
            throw IllegalArgumentException("구독 중이지 않습니다.")
        }

        val newReceiver = receiver.unsubscribe(oldTopic)
        receiverRepository.save(newReceiver)
    }

}

data class PushResponseDto(
    val title: String, val body: String, val topics: List<String>, val occuredAt: Date
) {
    constructor(pushUnit: PushUnit) : this(
        pushUnit.title.value, pushUnit.body.value, pushUnit.topics.map { it.value }, pushUnit.occuredAt
    )
}

interface PushToClientService {
    fun feedback(receiver: Receiver, message: String)
    fun push(receiver: Receiver, pushUnit: PushUnit)
}

@Service
class WebsocketPushToClientService(
    val messagingTemplate: SimpMessagingTemplate
) : PushToClientService {
    override fun feedback(receiver: Receiver, message: String) {
        logger_.info { "Message[${message}] -> Receiver[${receiver.receiverId.id}]" }
        messagingTemplate.convertAndSendToUser(receiver.receiverId.id, "/queue", message)
    }

    override fun push(receiver: Receiver, pushUnit: PushUnit) {
        logger_.info { "PushUnit[title=${pushUnit.title}] -> Receiver[${receiver.receiverId.id}]" }
        messagingTemplate.convertAndSendToUser(receiver.receiverId.id, "/queue", PushResponseDto(pushUnit))
    }
}