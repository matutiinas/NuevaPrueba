package com.sportswipe.shared.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.sportswipe.shared.db.SportSwipeDatabase

actual class DriverFactory {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(SportSwipeDatabase.Schema, "sportswipe.db")
}
