package com.ywcheong.smq.receiver

import com.ywcheong.smq.domain.PushUnit
import com.ywcheong.smq.domain.Receiver
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.util.*

private val logger_ = KotlinLogging.logger {}

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