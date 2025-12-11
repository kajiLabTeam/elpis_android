package net.kajilab.elpissender.presenter.ui.view.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDialog() {
    val context = LocalContext.current
    val activity = context as Activity

    // フォアグラウンドパーミッション
    val foregroundPermissionsState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberMultiplePermissionsState(
                permissions =
                    listOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH_CONNECT,
                        android.Manifest.permission.BLUETOOTH_ADVERTISE,
                        android.Manifest.permission.BLUETOOTH_ADMIN,
                        android.Manifest.permission.POST_NOTIFICATIONS,
                    ),
            )
        } else {
            rememberMultiplePermissionsState(
                permissions =
                    listOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.BLUETOOTH_ADMIN,
                    ),
            )
        }

    // バックグラウンドパーミッション
    val backgroundPermissionState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            rememberPermissionState(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            null
        }

    // 状態に応じたログやダイアログの表示
    when {
        foregroundPermissionsState.allPermissionsGranted && (backgroundPermissionState?.status?.isGranted != false) -> {
            Log.d("LocationPermissionDialog", "全てのパーミッションが許可されました")
        }
        !foregroundPermissionsState.allPermissionsGranted -> {
            PermissionRationaleDialog(
                onDismissRequest = {
                    activity.finish()
                },
                onConfirm = {
                    foregroundPermissionsState.launchMultiplePermissionRequest()
                },
            )
        }
        foregroundPermissionsState.allPermissionsGranted && backgroundPermissionState?.status?.isGranted == false -> {
            backgroundPermissionState.let {
                if (!it.status.isGranted) {
                    BackgroundPermissionRequestDialog(
                        onDismissRequest = {
                            activity.finish()
                        },
                        onOpenSettings = {
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            context.startActivity(intent)
                        },
                    )
                }
            }
        }
        else -> {
            Log.d("LocationPermissionDialog", "パーミッションが拒否されました")
        }
    }
}

@Composable
fun PermissionRationaleDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("このアプリケーションのパーミッションについて") },
        text = {
            Column {
                Text("位置情報のパーミッションが必要です。")
                Text("バックグラウンドで位置情報を取得するためには、フォアグラウンドパーミッションに加え、追加の許可が必要です。")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("許可する")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("キャンセル")
            }
        },
    )
}

@Composable
fun BackgroundPermissionRequestDialog(
    onDismissRequest: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { /* 何もしない */ },
        title = { Text("バックグラウンドパーミッションが必要です") },
        text = {
            Column {
                Text("バックグラウンドでの位置情報の取得を許可する必要があります。")
                Text("この許可は、アプリの設定画面で有効にすることもできます。")
            }
        },
        confirmButton = {
            Button(onClick = onOpenSettings) {
                Text("設定画面を開く")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("キャンセル")
            }
        },
    )
}
