package net.kajilab.elpissender.presenter.ui.view.setting

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import net.kajilab.elpissender.repository.LogSendRepository

@Composable
fun SettingScreen(
    viewModel: SettingViewModel = viewModel(),
    showSnackbar: (String) -> Unit,
) {
    var isSensingTimeDialogOpen by remember { mutableStateOf(false) }
    var isModelInputDialogOpen by remember { mutableStateOf(false) }
    var modelInputDialogType by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableIntStateOf(0) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getSetting(context)
        viewModel.init(context)
    }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            elapsedTime = 0
            while (isLoading) {
                kotlinx.coroutines.delay(1000)
                elapsedTime++
            }
        }
    }

    Column(
        modifier =
            Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        SettingSectionTitle(title = "センシング設定") {
            SettingNavigationItem(
                title = "センシング時間と待機時間",
                description = "センシング中の時間: ${viewModel.sensingTime} 分, 待機時間: ${viewModel.waitTime} 分",
                onClick = { isSensingTimeDialogOpen = true },
            )
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            SettingItem(
                title = "BLEとWi-Fiを10秒間取得する",
                description = "10秒間BLEとWi-Fiを取得した後、送信します",
                onClick = {
                    viewModel.startSensing10second()
                    Log.d("SettingScreen", "BLEとWi-Fiがクリックされました")
                },
            )
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            SettingSwitchItem(
                title = "BLEとWi-Fiを取得する",
                description = "BLEとWi-Fiを取得し、送信します。止めるまで送信はされません。",
                checked = false,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        viewModel.startSensing()
                    } else {
                        viewModel.stopSensing()
                    }
                },
            )
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
        }

        SettingSectionTitle(title = "Negativeモデル Positiveモデルの送信") {
            SettingItem(
                title = "Negativeモデルを送信する",
                description = "Negativeモデルを送信します",
                onClick = {
                    Log.d("SettingScreen", "Negativeモデルがクリックされました")
                    modelInputDialogType = "negative"
                    isModelInputDialogOpen = true
                },
            )
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            SettingItem(
                title = "Positiveモデルを送信する",
                description = "Positiveモデルを送信します",
                onClick = {
                    Log.d("SettingScreen", "Positiveモデルがクリックされました")
                    modelInputDialogType = "positive"
                    isModelInputDialogOpen = true
                },
            )
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
        }

        SettingSectionTitle(title = "その他の設定") {
            SettingNavigationItem(
                title = "アプリについて",
                description = "バージョン情報・プライバシーポリシーなど",
                onClick = { Log.d("SettingScreen", "アプリについてがクリックされました") },
            )
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
        }

        SettingSectionTitle(title = "デバック実装") {
            SettingItem(
                title = "とりあえず、アナリティクスを送るぞ",
                description = "アナリティクスを送ってみます",
                onClick = {
                    val logSendRepository = LogSendRepository()
                    logSendRepository.sendLog(
                        result = "success",
                        message = "test",
                        level = 1,
                    )
                },
            )
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
        }
    }

    // センシング時間と待機時間のダイアログ
    if (isSensingTimeDialogOpen) {
        TimeIntervalInputDialog(
            title = "センシング設定",
            description = "センシング中の時間と待機時間を設定してください",
            initialSensingTime = viewModel.sensingTime,
            initialWaitTime = viewModel.waitTime,
            onDismiss = { isSensingTimeDialogOpen = false },
            onSave = { newSensingTime, newWaitTime ->
                viewModel.sensingTime = newSensingTime
                viewModel.waitTime = newWaitTime
                viewModel.setSensingTime(context)
                isSensingTimeDialogOpen = false
                Log.d("SettingScreen", "センシング中の時間: $newSensingTime 分, 待機時間: $newWaitTime 分")
            },
        )
    }

    if (isModelInputDialogOpen) {
        ModelInputDialog(
            title = if (modelInputDialogType == "negative") "Negativeモデル計測" else "Positiveモデル計測",
            description = "部屋番号と計測時間を入力してください",
            initialRoomId = 0,
            initialMeasurementTime = 10,
            onDismiss = { isModelInputDialogOpen = false },
            onStartMeasurement = { roomId, time ->
                isModelInputDialogOpen = false
                isLoading = true
                viewModel.sendNegativeModel(
                    roomId = roomId,
                    sampleType = if (modelInputDialogType == "negative") "negative" else "positive",
                    context = context,
                    sensingTime = time,
                    onStopped = {
                        isLoading = false
                        showSnackbar("計測が終了し送信しました")
                    },
                )
            },
        )
    }

    if (isLoading) {
        LoadingScreen(elapsedTime = elapsedTime)
    }
}

@Composable
fun ModelInputDialog(
    title: String,
    description: String,
    initialRoomId: Int,
    initialMeasurementTime: Int,
    onDismiss: () -> Unit,
    onStartMeasurement: (Int, Int) -> Unit,
) {
    var roomId by remember { mutableIntStateOf(initialRoomId) }
    var measurementTime by remember { mutableIntStateOf(initialMeasurementTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                Text(text = description, color = Color.Gray)

                OutlinedTextField(
                    value = roomId.toString(),
                    onValueChange = { roomId = it.toIntOrNull() ?: roomId },
                    label = { Text("部屋番号") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )

                OutlinedTextField(
                    value = measurementTime.toString(),
                    onValueChange = { measurementTime = it.toIntOrNull() ?: measurementTime },
                    label = { Text("計測時間（秒）") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onStartMeasurement(roomId, measurementTime)
            }) {
                Text("開始")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        },
    )
}

@Preview
@Composable
fun PreviewNegativeModelInputDialog() {
    ModelInputDialog(
        title = "Negativeモデル計測",
        description = "部屋番号と計測時間を入力してください",
        initialRoomId = 0,
        initialMeasurementTime = 10,
        onDismiss = {},
        onStartMeasurement = { _, _ -> },
    )
}

@Composable
fun LoadingScreen(elapsedTime: Int) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
        // 背景を薄暗く設定
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            androidx.compose.material3.CircularProgressIndicator()
            Text(
                text = "計測中: ${elapsedTime}秒経過",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoadingScreen() {
    LoadingScreen(elapsedTime = 10)
}

@Composable
fun SettingSectionTitle(
    title: String,
    content: @Composable () -> Unit = {},
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .padding(vertical = 8.dp),
        )
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingSectionTitle() {
    SettingSectionTitle(title = "センシング設定") {
        SettingNavigationItem(
            title = "センシング時間と待機時間",
            description = "センシング中の時間: 10 分, 待機時間: 5 分",
            onClick = {},
        )
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    var switchState by remember { mutableStateOf(checked) }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(text = description, color = Color.Gray, fontSize = 12.sp)
        }
        Switch(
            checked = switchState,
            onCheckedChange = {
                switchState = it
                onCheckedChange(it)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingSwitchItem() {
    SettingSwitchItem(
        title = "BLEとWi-Fiを取得する",
        description = "BLEとWi-Fiを取得し、送信します。止めるまで送信はされません。",
        checked = false,
        onCheckedChange = {},
    )
}

@Composable
fun SettingNavigationItem(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(text = description, color = Color.Gray, fontSize = 12.sp)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.Gray,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingNavigationItem() {
    SettingNavigationItem(
        title = "センシング時間と待機時間",
        description = "センシング中の時間: 10 分, 待機時間: 5 分",
        onClick = {},
    )
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(text = description, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingItem() {
    SettingItem(
        title = "センシング時間と待機時間",
        description = "センシング中の時間: 10 分, 待機時間: 5 分",
        onClick = {},
    )
}

@Composable
fun UrlInputDialog(
    title: String,
    description: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var url by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                Text(text = description, color = Color.Gray)
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URLを入力") },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(url)
                },
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        },
    )
}

@Preview
@Composable
fun PreviewUrlInputDialog() {
    UrlInputDialog(
        title = "URLを入力",
        description = "URLを入力してください",
        initialValue = "https://example.com",
        onDismiss = {},
        onSave = {},
    )
}

@Composable
fun TimeIntervalInputDialog(
    title: String,
    description: String,
    initialSensingTime: Int,
    initialWaitTime: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit,
) {
    var sensingTime by remember { mutableStateOf(initialSensingTime) }
    var waitTime by remember { mutableStateOf(initialWaitTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                Text(text = description, color = Color.Gray)

                OutlinedTextField(
                    value = sensingTime.toString(),
                    onValueChange = { newTime ->
                        sensingTime = newTime.toIntOrNull() ?: sensingTime
                    },
                    label = { Text("センシング中の時間（分）") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = waitTime.toString(),
                    onValueChange = { newTime ->
                        waitTime = newTime.toIntOrNull() ?: waitTime
                    },
                    label = { Text("待機時間（分）") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(sensingTime, waitTime)
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        },
    )
}

@Preview
@Composable
fun PreviewTimeIntervalInputDialog() {
    TimeIntervalInputDialog(
        title = "センシング設定",
        description = "センシング中の時間と待機時間を設定してください",
        initialSensingTime = 10,
        initialWaitTime = 5,
        onDismiss = {},
        onSave = { _, _ -> },
    )
}
