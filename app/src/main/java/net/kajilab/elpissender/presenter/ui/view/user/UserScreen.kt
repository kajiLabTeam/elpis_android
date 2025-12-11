package net.kajilab.elpissender.presenter.ui.view.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun UserScreen(
    viewModel: UserViewModel = viewModel(),
    showSnackbar: (String) -> Unit,
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val isSensing = viewModel.getSensingStatus(context = context)
        viewModel.isSensing = isSensing

        val user = viewModel.getUserSetting(context = context)
        viewModel.userName = user.userName
        viewModel.password = user.password
        viewModel.serverUrl = user.serverUrl
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
        ) {
            Text(
                text = "🌐 位置データを常に送信する",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E88E5),
            )

            Spacer(modifier = Modifier.weight(1f))

            // スイッチ
            Switch(
                checked = viewModel.isSensing,
                onCheckedChange = { isChecked ->
                    viewModel.isSensing = isChecked
                    viewModel.startForegroundSensing(
                        isSensing = isChecked,
                        context = context,
                    )
                },
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = Color.Gray,
        )

        TextField(
            value = viewModel.userName,
            onValueChange = { viewModel.userName = it },
            label = { Text("ユーザー名") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("パスワード") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = viewModel.serverUrl,
            onValueChange = { viewModel.serverUrl = it },
            label = { Text("サーバーのURL") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.saveUserSetting(
                    context = context,
                    showSnackbar = showSnackbar,
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth(),
        ) {
            Text("保存")
        }
    }
}
