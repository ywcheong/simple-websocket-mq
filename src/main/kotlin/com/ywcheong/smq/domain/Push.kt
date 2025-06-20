package com.ywcheong.smq.domain

import java.util.*

data class PushTitle(val value: String) {
    init {
        require(0 < value.length) {
            "알림의 제목은 필수 필드입니다."
        }

        require(value.length <= MAX_TITLE_LENGTH) {
            "알림의 제목은 ${MAX_TITLE_LENGTH}자 이하여야 합니다. (현재 ${value.length}자)"
        }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 60
    }
}

data class PushBody(val value: String) {
    init {
        require(0 < value.length) {
            "알림의 본문은 필수 필드입니다."
        }

        require(value.length <= MAX_BODY_LENGTH) {
            "알림의 본문은 ${MAX_BODY_LENGTH}자 이하여야 합니다. (현재 ${value.length}자)"
        }
    }

    companion object {
        const val MAX_BODY_LENGTH = 240
    }
}

data class PushTopic(val value: String) {
    init {
        require(0 < value.length) {
            "주제의 이름은 비어 있을 수 없습니다."
        }

        require(value.length <= MAX_TOPIC_LENGTH) {
            "주제는 ${MAX_TOPIC_LENGTH}자 이하여야 합니다. (현재 ${value.length}자)"
        }
    }

    companion object {
        const val MAX_TOPIC_LENGTH = 15
    }
}

data class PushUnit(
    val title: PushTitle, val body: PushBody, val topics: Set<PushTopic>, val occuredAt: Date
)