package com.ywcheong.smq.sender

import com.ywcheong.smq.domain.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

data class PushSendRequestDto(
    val title: String,
    val body: String,
    val topics: List<String>,
)

@RestController
class PushSendController @Autowired constructor(
    private val pushSendService: PushSendService
) {
    @PostMapping("/send")
    fun send(pushSendRequestDto: PushSendRequestDto): ResponseEntity<Unit> = when (pushSendService.send(
        title = PushTitle(pushSendRequestDto.title),
        body = PushBody(pushSendRequestDto.body),
        topics = pushSendRequestDto.topics.map {
            PushTopic(it)
        }.toSet()
    )) {
        true -> ResponseEntity.status(HttpStatus.ACCEPTED).body(null)
        false -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
    }
}

interface PushSendService {
    fun send(title: PushTitle, body: PushBody, topics: Set<PushTopic>): Boolean
}

@Service
class DefaultPushSendService @Autowired constructor(
    private val pushPublishPort: PushPublishPort
) : PushSendService {
    override fun send(title: PushTitle, body: PushBody, topics: Set<PushTopic>): Boolean {
        val pushUnit = PushUnit(title, body, topics, Date())
        return pushPublishPort.publish(PushPublishedEvent(pushUnit))
    }
}

interface PushPublishPort {
    fun publish(event: PushPublishedEvent): Boolean
}

@Repository
class KafkaPushPublishPort @Autowired constructor(
    private val kafkaTemplate: KafkaTemplate<String, PushUnit>
) : PushPublishPort {

    @Value("\${smq.kafka-topic-name}")
    private lateinit var kafkaTopicName: String

    override fun publish(event: PushPublishedEvent): Boolean {
        // 나쁜 코드: 동기 처리를 강요하고, 명시적 오류 처리가 부재함
        // 다만 학습 단계이므로 일단은 이렇게 유지
        kafkaTemplate.send(kafkaTopicName, event.pushUnit).get()
        return true
    }
}