package ru.ushakov.warehouse.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import ru.ushakov.warehouse.repository.ItemRepository
import java.math.BigDecimal
import java.time.LocalDate

@Service
class WarehouseEventListener(
    val itemRepository: ItemRepository,
    val kafkaProducer: WarehouseEventProducer
) {

    @KafkaListener(topics = ["BillingReserved"], groupId = "warehouse-service-group")
    fun handleBillingReservedEvent(message: String) {
        val event = parseBillingReservedEvent(message)
        val items = event.items
        val insufficientItems = mutableListOf<String>()

        items.forEach { orderItem ->
            val warehouseItem = itemRepository.findByName(orderItem.name)

            if (warehouseItem == null || warehouseItem.quantity < orderItem.quantity) {
                insufficientItems.add(orderItem.name)
            }
        }

        if (insufficientItems.isNotEmpty()) {
            kafkaProducer.sendItemReserveFailedEvent(
                ItemReserveFailedEvent(
                    orderId = event.orderId,
                    accountNumber = event.accountNumber,
                    totalPrice = event.totalPrice
                )
            )
            println("ItemReserveFailedEvent sent for account ${event.accountNumber} due to insufficient items.")
        } else {
            items.forEach { orderItem ->
                val warehouseItem = itemRepository.findByName(orderItem.name)
                if (warehouseItem != null) {
                    warehouseItem.quantity -= orderItem.quantity
                    itemRepository.save(warehouseItem)
                }
            }
            kafkaProducer.sendItemReservedEvent(
                ItemReservedEvent(
                    orderId = event.orderId,
                    deliveryAddress = event.deliveryAddress,
                    deliveryDate = event.deliveryDate
                )
            )
            println("ItemReservedEvent sent for order ${event.orderId}.")
        }
    }

    private fun parseBillingReservedEvent(message: String): BillingReservedEvent {
        val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        return mapper.readValue(message, BillingReservedEvent::class.java)
    }
}

data class BillingReservedEvent(
    val orderId: Long,
    val items: List<Item>,
    val accountNumber: String,
    val totalPrice: BigDecimal,
    val deliveryAddress: String,
    val deliveryDate: LocalDate
)

data class ItemReservedEvent(
    val orderId: Long,
    val deliveryAddress: String,
    val deliveryDate: LocalDate
)

data class ItemReserveFailedEvent(
    val orderId: Long,
    val accountNumber: String,
    val totalPrice: BigDecimal
)

data class Item(
    val name: String,
    val price: BigDecimal,
    val quantity: Int
)

@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
    }
}