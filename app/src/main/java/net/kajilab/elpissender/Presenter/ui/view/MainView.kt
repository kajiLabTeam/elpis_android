package net.kajilab.elpissender.Presenter.ui.view

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kajilab.elpissender.Presenter.ui.theme.EstimatingLocationUsingRadioWavesTheme
import net.kajilab.elpissender.Service.SensingService
import net.kajilab.elpissender.ViewModel.MainViewModel
import net.kajilab.elpissender.ViewModel.PermissionState
import net.kajilab.elpissender.ViewModel.UploadState
import java.io.File

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Permission : BottomNavItem("permission", Icons.Default.Security, "権限")
    object Scan : BottomNavItem("scan", Icons.Default.Sensors, "スキャン")
    object Files : BottomNavItem("files", Icons.Default.FolderOpen, "ファイル")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    val activity: Activity = LocalContext.current as Activity

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(BottomNavItem.Permission, BottomNavItem.Scan, BottomNavItem.Files)

    val permissionState by viewModel.permissionState.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val fileList by viewModel.fileList.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val bucketName by viewModel.bucketName.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
        viewModel.refreshFileList()
    }

    // 権限が変更されたときに再チェック
    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            viewModel.checkPermissions()
        } else if (selectedTab == 2) {
            viewModel.refreshFileList()
        }
    }

    // アップロード状態の監視
    LaunchedEffect(uploadState) {
        when (uploadState) {
            is UploadState.Success -> {
                snackbarHostState.showSnackbar((uploadState as UploadState.Success).message)
                viewModel.clearUploadState()
            }
            is UploadState.Error -> {
                snackbarHostState.showSnackbar("エラー: ${(uploadState as UploadState.Error).message}")
                viewModel.clearUploadState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Elpis Sender") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isError = data.visuals.message.startsWith("エラー")
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isError)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isError)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> PermissionScreen(
                    permissionState = permissionState,
                    onRequestPermission = {
                        viewModel.getPermission(activity)
                        viewModel.checkPermissions()
                    }
                )
                1 -> ScanScreen(
                    isScanning = isScanning,
                    viewModel = viewModel,
                    context = context,
                    onStartScan = {
                        viewModel.addSensor()
                        viewModel.start("elpis_scan")
                    },
                    onStopScan = {
                        viewModel.stop()
                    }
                )
                2 -> FilesScreen(
                    fileList = fileList,
                    uploadState = uploadState,
                    bucketName = bucketName,
                    onBucketNameChange = { viewModel.setBucketName(it) },
                    onRefresh = { viewModel.refreshFileList() },
                    onSendFiles = { wifiFile, bleFile ->
                        viewModel.postCsvData(wifiFile, bleFile)
                    }
                )
            }
        }
    }
}

@Composable
fun PermissionScreen(
    permissionState: PermissionState,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "必要な権限",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "このアプリはWi-FiとBLEをスキャンするために以下の権限が必要です。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        PermissionItem(
            title = "位置情報",
            description = "Wi-FiとBLEスキャンに必要",
            isGranted = permissionState.locationPermission
        )

        PermissionItem(
            title = "Bluetooth",
            description = "BLEビーコンのスキャンに必要",
            isGranted = permissionState.bluetoothPermission
        )

        PermissionItem(
            title = "Wi-Fi",
            description = "Wi-Fiアクセスポイントのスキャンに必要",
            isGranted = permissionState.wifiPermission
        )

        PermissionItem(
            title = "通知",
            description = "バックグラウンド実行の通知に必要",
            isGranted = permissionState.notificationPermission
        )

        PermissionItem(
            title = "バックグラウンド位置情報",
            description = "バックグラウンドでのスキャンに必要",
            isGranted = permissionState.backgroundLocationPermission
        )

        Spacer(modifier = Modifier.height(24.dp))

        val allGranted = permissionState.locationPermission &&
                permissionState.bluetoothPermission &&
                permissionState.wifiPermission &&
                permissionState.notificationPermission

        if (!allGranted) {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("権限を許可する")
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "すべての権限が許可されています",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                contentDescription = if (isGranted) "許可済み" else "未許可",
                tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ScanScreen(
    isScanning: Boolean,
    viewModel: MainViewModel,
    context: android.content.Context,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "センサースキャン",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // スキャン状態表示
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isScanning)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "スキャン中...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Wi-FiとBLEビーコンを検出しています",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "待機中",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "スキャンを開始してください",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // スキャン操作ボタン
        Text(
            text = "フォアグラウンドスキャン",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onStartScan,
                enabled = !isScanning,
                modifier = Modifier.weight(1f)
            ) {
                Text("開始")
            }

            OutlinedButton(
                onClick = onStopScan,
                enabled = isScanning,
                modifier = Modifier.weight(1f)
            ) {
                Text("停止")
            }
        }

        // タイマースキャン
        Button(
            onClick = {
                viewModel.addSensor()
                CoroutineScope(Dispatchers.Main).launch {
                    viewModel.timerStart("elpis_timer") {}
                }
            },
            enabled = !isScanning,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("10秒間スキャン")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // バックグラウンドスキャン
        Text(
            text = "バックグラウンドスキャン",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "バックグラウンドでは2分間スキャン、1分間休止を繰り返します",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val serviceIntent = Intent(context, SensingService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("バックグラウンド開始")
            }

            OutlinedButton(
                onClick = {
                    val serviceIntent = Intent(context, SensingService::class.java)
                    context.stopService(serviceIntent)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("停止")
            }
        }
    }
}

@Composable
fun FilesScreen(
    fileList: List<File>,
    uploadState: UploadState,
    bucketName: String,
    onBucketNameChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onSendFiles: (wifiFile: File, bleFile: File) -> Unit
) {
    var selectedWifiFile by remember { mutableStateOf<File?>(null) }
    var selectedBleFile by remember { mutableStateOf<File?>(null) }

    val isUploading = uploadState is UploadState.Uploading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "保存ファイル",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(onClick = onRefresh, enabled = !isUploading) {
                Text("更新")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Wi-FiファイルとBLEファイルを選択して送信できます",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // バケット名入力
        OutlinedTextField(
            value = bucketName,
            onValueChange = onBucketNameChange,
            label = { Text("バケット名") },
            placeholder = { Text("elpis") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 選択状態表示
        if (selectedWifiFile != null || selectedBleFile != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "選択中のファイル",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "WiFi: ${selectedWifiFile?.name ?: "未選択"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "BLE: ${selectedBleFile?.name ?: "未選択"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (selectedWifiFile != null && selectedBleFile != null) {
                        onSendFiles(selectedWifiFile!!, selectedBleFile!!)
                    }
                },
                enabled = selectedWifiFile != null && selectedBleFile != null && !isUploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("アップロード中...")
                } else {
                    Text("サーバーに送信")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ファイルリスト
        if (fileList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ファイルがありません",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "スキャンを実行するとファイルが保存されます",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(fileList) { file ->
                    val isWifi = file.name.contains("WiFi")
                    val isBle = file.name.contains("BLE")
                    val isSelected = (isWifi && file == selectedWifiFile) ||
                                   (isBle && file == selectedBleFile)

                    FileItem(
                        file = file,
                        isSelected = isSelected,
                        onClick = {
                            if (isWifi) {
                                selectedWifiFile = if (selectedWifiFile == file) null else file
                            } else if (isBle) {
                                selectedBleFile = if (selectedBleFile == file) null else file
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FileItem(
    file: File,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isWifi = file.name.contains("WiFi")
    val isBle = file.name.contains("BLE")
    val typeLabel = when {
        isWifi -> "WiFi"
        isBle -> "BLE"
        else -> "Other"
    }
    val typeColor = when {
        isWifi -> MaterialTheme.colorScheme.primary
        isBle -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = "${file.length() / 1024} KB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        color = typeColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = typeColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EstimatingLocationUsingRadioWavesTheme {
        MainView()
    }
}
