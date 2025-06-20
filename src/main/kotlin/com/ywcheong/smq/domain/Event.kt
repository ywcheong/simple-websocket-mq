package com.ywcheong.smq.domain

data class PushPublishedEvent(val pushUnit: PushUnit)
data class PushDeliveredEvent(val receiverId: ReceiverId, val pushUnit: PushUnit)