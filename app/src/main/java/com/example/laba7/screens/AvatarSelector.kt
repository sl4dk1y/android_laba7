package com.example.laba7.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@Composable
fun AvatarSelector(
    currentUri: String?,
    onAvatarSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    var pendingCapturePath by rememberSaveable { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val galleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            onAvatarSelected(uri.toString())
            errorMessage = null
        }
    }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        val temporaryFile = pendingCapturePath?.let(::File)
        pendingCapturePath = null
        if (captured && temporaryFile?.isFile == true) {
            runCatching {
                val avatarDirectory = File(context.filesDir, "avatars")
                check(avatarDirectory.exists() || avatarDirectory.mkdirs())
                val permanentFile = File(avatarDirectory, "avatar_${UUID.randomUUID()}.jpg")
                temporaryFile.copyTo(permanentFile, overwrite = false)
                permanentFile.absolutePath
            }.onSuccess { savedPath ->
                onAvatarSelected(savedPath)
                errorMessage = null
                temporaryFile.delete()
            }.onFailure {
                errorMessage = "Не удалось сохранить фотографию"
                temporaryFile.delete()
            }
        } else {
            temporaryFile?.delete()
        }
    }

    val avatarBitmap by produceState<android.graphics.Bitmap?>(null, currentUri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                val uri = currentUri?.let(Uri::parse)
                when (uri?.scheme) {
                    "content", "file" -> context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                    null -> currentUri?.let { BitmapFactory.decodeFile(it, options) }
                    else -> null
                }
            }.getOrNull()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (avatarBitmap != null) {
            Image(
                bitmap = avatarBitmap!!.asImageBitmap(),
                contentDescription = "Аватар пользователя",
                modifier = Modifier.size(112.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        OutlinedButton(onClick = { galleryPicker.launch(arrayOf("image/*")) }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text("Выбрать из галереи")
        }
        OutlinedButton(
            onClick = {
                runCatching {
                    val captureDirectory = File(context.cacheDir, "avatar-captures")
                    check(captureDirectory.exists() || captureDirectory.mkdirs())
                    val file = File(captureDirectory, "capture_${UUID.randomUUID()}.jpg")
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    pendingCapturePath = file.absolutePath
                    camera.launch(uri)
                }.onFailure {
                    pendingCapturePath?.let(::File)?.delete()
                    pendingCapturePath = null
                    errorMessage = "Не удалось открыть камеру"
                }
            },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
                Text("Сделать фото")
        }
        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
        if (currentUri != null && avatarBitmap == null) {
            Text("Аватар выбран", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
