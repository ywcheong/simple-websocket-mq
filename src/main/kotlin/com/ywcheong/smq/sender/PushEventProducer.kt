package com.ywcheong.smq.sender

import com.ywcheong.smq.domain.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

private val logger = KotlinLogging.logger {}

data class PushSendRequestDto(
    val title: String,
    val body: String,
    val topics: List<String>,
)

@Tag(
    name = "발신자 API", description = "알림을 발신하기 위한 API 입니다."
)
@RestController
class PushSendController @Autowired constructor(
    private val pushSendService: PushSendService
) {
    @Operation(
        summary = "알림을 발행",
        description = """알림의 주제를 구독한 수신자 모두에게 이 알림이 전달됩니다. 주제를 구독한 수신자가 없어도 알림은 발행되지만, 아무도 알림을 받지 않습니다.
알림의 제목, 본문, 그리고 주제 목록을 지정해서 POST 요청의 Body 에 JSON 형식으로 제공해야 합니다.""",
        responses = [ApiResponse(
            responseCode = "202", description = "알림이 성공적으로 접수되었습니다. 수신자가 알림을 실제로 수신하는 것은 비동기적으로 처리됩니다."
        ), ApiResponse(
            responseCode = "400", description = "요청이 유효하지 않습니다."
        ), ApiResponse(
            responseCode = "500", description = "서버 문제로 알림 접수에 실패했습니다."
        )]
    )
    @PostMapping("/send")
    fun send(
        @RequestBody pushSendRequestDto: PushSendRequestDto
    ): ResponseEntity<Unit> {
        val pushResult: Boolean = pushSendService.send(
            title = PushTitle(pushSendRequestDto.title),
            body = PushBody(pushSendRequestDto.body),
            topics = pushSendRequestDto.topics.map {
                PushTopic(it)
            }.toSet()
        )

        return when (pushResult) {
            true -> {
                logger.info { "Push [${pushSendRequestDto.title}] is sent" }
                ResponseEntity.status(HttpStatus.ACCEPTED).body(null)
            }

            false -> {
                logger.info { "Push [${pushSendRequestDto.title}] is not sent" }
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
            }
        }
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