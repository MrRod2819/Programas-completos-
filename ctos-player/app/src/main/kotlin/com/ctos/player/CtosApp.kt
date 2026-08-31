package com.ctos.player

import android.app.Application
import com.ctos.player.data.repository.MediaRepository
import com.ctos.player.data.repository.SettingsRepository

/** Minimal manual DI container — no third party graph needed for this app. */
class CtosApp : Application() {
    val mediaRepository: MediaRepository by lazy { MediaRepository(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
}
