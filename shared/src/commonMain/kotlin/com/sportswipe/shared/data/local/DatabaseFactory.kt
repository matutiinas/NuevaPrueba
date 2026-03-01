package com.sportswipe.shared.data.local

import app.cash.sqldelight.db.SqlDriver
import com.sportswipe.shared.db.SportSwipeDatabase

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

class DatabaseFactory(driverFactory: DriverFactory) {
    val db: SportSwipeDatabase = SportSwipeDatabase(driverFactory.createDriver())
}
