package com.ywcheong.smq.receiver

import com.ywcheong.smq.domain.PushDeliveredEvent
import com.ywcheong.smq.domain.PushUnit
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

interface MessageReceiver {
    fun receive(pushUnit: PushUnit)
}

@Component
class KafkaMessageReceiver constructor(
    private val receiverRepository: ReceiverRepository, private val events: ApplicationEventPublisher
) : MessageReceiver {
    @KafkaListener(topics = ["\${smq.kafka-topic-name}"])
    override fun receive(pushUnit: PushUnit) {
        logger.info { "PushUnit [$pushUnit] comes from Kafka" }
        val messageTopics = pushUnit.topics.toList()
        receiverRepository.findAllWithTopics(messageTopics).forEach { receiver ->
            events.publishEvent(PushDeliveredEvent(receiver, pushUnit))
        }
    }
}

@Component
class PushDeliveredEventHandler {
    @EventListener
    fun handle(pushDeliveredEvent: PushDeliveredEvent) {
        val (receiver, pushUnit) = pushDeliveredEvent
        logger.info { "PushDeliveredEvent: PushUnit[title=${pushUnit.title}] -> Receiver[${receiver.receiverId.id}]" }
        TODO("Websocket Callback")
    }
}