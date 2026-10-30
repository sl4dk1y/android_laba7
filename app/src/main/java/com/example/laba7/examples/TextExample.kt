package com.example.laba7.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.laba7.R

@Composable
fun TextExample(onBack: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        // Получаем текст из строковых ресурсов приложения.
        Text(
            text = stringResource(R.string.compose_hello),
            color = Color.Cyan,
            fontSize = 28.sp,
            modifier = Modifier
                .background(Color.LightGray)
                .padding(30.dp)
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedButton(
            onClick = onBack
        ) {
            Text("Назад")
        }
    }
}