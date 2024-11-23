package ru.ushakov.warehouse.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class WarehouseEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    val objectMapper: ObjectMapper
) {

    fun sendItemReservedEvent(event: ItemReservedEvent) {
        kafkaTemplate.send("ItemReserved", objectMapper.writeValueAsString(event))
        println("ItemReservedEvent sent to Kafka: $event")
    }

    fun sendItemReserveFailedEvent(event: ItemReserveFailedEvent) {
        kafkaTemplate.send("ItemReserveFailed",  objectMapper.writeValueAsString(event))
        println("ItemReserveFailedEvent sent to Kafka: $event")
    }
}