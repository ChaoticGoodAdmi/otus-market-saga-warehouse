package ru.ushakov.warehouse.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.ushakov.warehouse.domain.Item

interface ItemRepository : JpaRepository<Item, Long> {
    fun findByName(name: String): Item?
}