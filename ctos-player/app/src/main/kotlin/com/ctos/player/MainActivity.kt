package com.ctos.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ctos.player.core.theme.CtosTheme
import com.ctos.player.ui.CtosPlayerApp
import com.ctos.player.ui.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants[readAudioPermission()] == true) {
            viewModel.loadLibrary()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions()
        setContent {
            val palette by viewModel.palette.collectAsStateWithLifecycle()
            val ringStyle by viewModel.ringStyle.collectAsStateWithLifecycle()
            CtosTheme(palette = palette, ringStyle = ringStyle) {
                CtosPlayerApp(viewModel)
            }
        }
    }

    private fun requestPermissions() {
        val required = buildList {
            add(readAudioPermission())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        val missing = required.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) {
            viewModel.loadLibrary()
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    /** Android 13+ splits storage access into granular media permissions. */
    private fun readAudioPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
}
