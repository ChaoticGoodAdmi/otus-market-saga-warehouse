package ru.ushakov.warehouse.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.ushakov.warehouse.domain.Item
import ru.ushakov.warehouse.repository.ItemRepository

@RestController
@RequestMapping("/warehouse")
class WarehouseController(
    private val itemRepository: ItemRepository
) {

    @PostMapping
    fun addItem(@RequestBody request: AddItemRequest): ResponseEntity<Any> {
        val existingItem = itemRepository.findByName(request.name)
        return if (existingItem != null) {
            existingItem.quantity += request.quantity
            itemRepository.save(existingItem)
            ResponseEntity.ok(mapOf("message" to "Item updated successfully: ${existingItem.name}, new quantity: ${existingItem.quantity}"))
        } else {
            val item = Item(name = request.name, quantity = request.quantity)
            itemRepository.save(item)
            ResponseEntity.ok(mapOf("message" to "Item added successfully: ${item.name}"))
        }
    }

    @GetMapping("/item")
    fun findItem(@RequestParam name: String): ResponseEntity<Item> {
        val item = itemRepository.findByName(name)
        return if (item != null) {
            ResponseEntity.ok(item)
        } else {
            ResponseEntity.status(404).body(null)
        }
    }
}

data class AddItemRequest(
    val name: String,
    val quantity: Int
)