package com.sportswipe.shared.presentation.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

open class BaseViewModel {
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
