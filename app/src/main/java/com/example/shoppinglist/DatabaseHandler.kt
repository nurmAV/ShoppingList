package com.example.shoppinglist

/** An interface that promises that the class implementing it can perform database operations*/
interface DatabaseHandler {
    fun delete(name: String, price: Double)
}