package ru.ushakov.warehouse.domain

import jakarta.persistence.*

@Entity
@Table(name = "items")
data class Item(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true, nullable = false)
    val name: String,
    var quantity: Int
) {
    constructor() : this(0, "", 0)
}