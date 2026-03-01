package com.sportswipe.shared.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.sportswipe.shared.db.SportSwipeDatabase

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(SportSwipeDatabase.Schema, context, "sportswipe.db")
}
