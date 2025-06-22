package com.ywcheong.smq.receiver

import com.ywcheong.smq.domain.PushDeliveredEvent
import com.ywcheong.smq.domain.PushUnit
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

// EventReceiver -> EventHandler 구조가 Controller -> Service 와 유사해 이렇게 Annotate

@Controller
class KafkaMessageReceiver constructor(
    private val receiverRepository: ReceiverRepository, private val events: ApplicationEventPublisher
) {
    @KafkaListener(topics = ["\${smq.kafka-topic-name}"])
    fun receive(pushUnit: PushUnit) {
        logger.info { "PushUnit [$pushUnit] comes from Kafka" }
        val messageTopics = pushUnit.topics.toList()
        receiverRepository.findAllWithTopics(messageTopics).forEach { receiver ->
            // 도메인 이벤트 PushDeliveredEvent 발생
            events.publishEvent(PushDeliveredEvent(receiver, pushUnit))
        }
    }
}

@Service
class PushDeliveredEventHandler(
    private val pushToClientService: PushToClientService
) {
    @EventListener
    fun handle(pushDeliveredEvent: PushDeliveredEvent) {
        // 도메인 이벤트 PushDeliveredEvent 처리
        val (receiver, pushUnit) = pushDeliveredEvent
        logger.info { "PushDeliveredEvent: PushUnit[title=${pushUnit.title}] -> Receiver[${receiver.receiverId.id}]" }
        pushToClientService.push(receiver, pushUnit)
    }
}