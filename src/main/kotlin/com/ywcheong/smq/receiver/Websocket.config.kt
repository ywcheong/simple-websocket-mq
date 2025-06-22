package com.ywcheong.smq.receiver

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal
import java.util.*

private val logger_ = KotlinLogging.logger {}

// 모든 유저에게 랜덤 생성된 고유 식별자를 부여해, 이를 Websocket 간 유저 구분에 활용합니다.
data class StompPrincipal(val id: String) : Principal {
    override fun getName(): String = id
}

class GiveRandomPrincipalHandshakeHandler : DefaultHandshakeHandler() {
    override fun determineUser(
        request: ServerHttpRequest, wsHandler: WebSocketHandler, attributes: MutableMap<String, Any>
    ): Principal {
        val principal = StompPrincipal(UUID.randomUUID().toString())
        logger_.info { "New handshake conferred Principal ${principal.id}" }
        return principal
    }
}

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic", "/queue")
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/receiver").setHandshakeHandler(GiveRandomPrincipalHandshakeHandler()).withSockJS()
    }
}

// 클라이언트가 SimpleBroker 를 활용해 직접 Topic 에 브로드캐스트하는 것을 방지하기 위해 모든 SEND 패킷을 드롭합니다.
@Configuration
class WebSocketInterceptorConfig(
    val stompSendDropInterceptor: StompSendDropInterceptor
) : WebSocketMessageBrokerConfigurer {
    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        // 클라이언트 -> 서버 필터
        registration.interceptors(stompSendDropInterceptor)
    }

//    override fun configureClientOutboundChannel(registration: ChannelRegistration) {
//        // 서버 -> 클라이언트 필터
//    }
}

@Component
class StompSendDropInterceptor : ChannelInterceptor {
    // 클라이언트가 발신한 모든 패킷은 SimpleBroker 가 MessageChannel 로 이송하기 전에 이 함수를 통과합니다.
    override fun preSend(
        message: org.springframework.messaging.Message<*>, channel: MessageChannel
    ): org.springframework.messaging.Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)

        if (accessor.command != StompCommand.SEND) {
            return message // Send 패킷이 아니면 살려둡니다.
        }

        val destination = accessor.destination
        val user = accessor.user
        logger_.info { "WebSocket SEND INTERCEPTED -> DROPPED: Principal=$user, Destination=$destination" }
        return null // null 반환 시 패킷이 삭제되는 효과가 발생합니다.
    }
}