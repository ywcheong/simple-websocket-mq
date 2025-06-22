package com.ywcheong.smq.domain

data class ReceiverId(val id: String)

data class Receiver(
    val receiverId: ReceiverId, val subscription: Set<PushTopic>
) {
    init {
        check(subscription.size <= MAX_SUBSCRIPTIONS) {
            "최대 ${MAX_SUBSCRIPTIONS}개의 주제만 구독할 수 있습니다."
        }
    }

    fun subscribe(topic: PushTopic): Receiver = Receiver(
        receiverId = receiverId, subscription = subscription + topic
    )

    fun unsubscribe(topic: PushTopic): Receiver = Receiver(
        receiverId = receiverId, subscription = subscription - topic
    )

    fun isSubscribed(topic: PushTopic): Boolean = subscription.contains(topic)

    companion object {
        const val MAX_SUBSCRIPTIONS = 20
    }
}