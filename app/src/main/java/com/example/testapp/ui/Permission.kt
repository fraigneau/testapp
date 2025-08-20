package com.example.testapp.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(multiplePermissionsState: MultiplePermissionsState) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        if (multiplePermissionsState.allPermissionsGranted) {
            Text("Permissions accordées.")
        } else {
            val perms = multiplePermissionsState.revokedPermissions.joinToString {
                it.permission
            }

            Text("Permissions requises : $perms")

            Spacer(modifier = Modifier.height(16.dp))

            if (multiplePermissionsState.shouldShowRationale) {
                Button(onClick = {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                }) {
                    Text("Demander les permissions")
                }
            } else {
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Ouvrir les paramètres")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Permissions bloquées, vas dans les paramètres.")
            }
        }
    }
}

