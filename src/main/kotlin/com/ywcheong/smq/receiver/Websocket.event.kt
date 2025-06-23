package com.ywcheong.smq.receiver

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.*

private val logger_ = KotlinLogging.logger {}

// Websocket STOMP 연결 간 발생하는 모든 이벤트를 등록합니다.
@Suppress("DuplicatedCode")
@Component
class WebSocketEventListener(
    private val receiverService: ReceiverService
) {
    // 연결 시작
    @EventListener
    fun handleSessionConnect(event: SessionConnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val user = accessor.user
        logger_.info { "WebSocket CONNECT ATTEMPT: SessionID=$user" }
    }

    // 연결 완료
    @EventListener
    fun handleSessionConnected(event: SessionConnectedEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val user = accessor.user
        logger_.info { "WebSocket CONNECT SUCCESS: Principal=$user" }

        check(user != null) {
            "Principal 정보를 찾을 수 없습니다. (세션 ID = ${accessor.sessionId})"
        }

        receiverService.connected(user.name)
    }

    // 연결 해제
    @EventListener
    fun handleSessionDisconnect(event: SessionDisconnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val user = accessor.user
        logger_.info { "WebSocket DISCONNECT: Principal=$user" }

        check(user != null) {
            "Principal 정보를 찾을 수 없습니다. (세션 ID = ${accessor.sessionId})"
        }

        receiverService.disconnected(user.name)
    }

    // 구독 요청
    @EventListener
    fun handleSessionSubscribe(event: SessionSubscribeEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val user = accessor.user
        val destination = accessor.destination
        logger_.info { "WebSocket SUBSCRIBE: Principal=$user, Destination=$destination" }

        check(user != null) {
            "Principal 정보를 찾을 수 없습니다. (세션 ID = ${accessor.sessionId})"
        }

        check(destination != null) {
            "헤더 정보에서 목적지가 없습니다."
        }

        receiverService.subscribe(user.name, removePrefixWithCheck(destination, DESTINATION_PREFIX))
    }

    // 구독 해제 요청
    @EventListener
    fun handleSessionUnsubscribe(event: SessionUnsubscribeEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val user = accessor.user
        val id = accessor.subscriptionId
        logger_.info { "WebSocket UNSUBSCRIBE: Principal=$user, Id=$id" }

        check(user != null) {
            "Principal 정보를 찾을 수 없습니다. (세션 ID = ${accessor.sessionId})"
        }

        check(id != null) {
            "헤더 정보에서 구독 ID 가 없습니다."
        }

        receiverService.unsubscribe(user.name, removePrefixWithCheck(id, ID_PREFIX))
    }

    companion object {
        const val DESTINATION_PREFIX = "/topic/"
        const val ID_PREFIX = "sub-"

        private fun removePrefixWithCheck(target: String, prefix: String): String {
            require(target.startsWith(prefix)) {
                "${target}이 유효하지 않습니다. ${prefix}로 시작해야 합니다."
            }

            return target.removePrefix(prefix)
        }
    }
}