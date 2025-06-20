package com.ywcheong.smq

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SimpleWebsocketMqApplication

fun main(args: Array<String>) {
    runApplication<SimpleWebsocketMqApplication>(*args)
}
