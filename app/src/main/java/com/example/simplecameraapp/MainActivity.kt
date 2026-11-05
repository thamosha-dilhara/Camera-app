
package com.example.simplecameraapp

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                CameraScreen()
            }
        }
    }
}

@Composable
fun CameraScreen() {

    val context = LocalContext.current

    var photoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var pendingUri by remember {
        mutableStateOf<Uri?>(null)
    }

    // Launch Android camera application
    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->

            if (success) {
                photoUri = pendingUri
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text(
            text = "Simple Camera App",
            style = MaterialTheme.typography.headlineMedium
        )

        // Display captured photograph
        if (photoUri != null) {

            AsyncImage(
                model = photoUri,
                contentDescription = "Captured Photograph",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                contentScale = ContentScale.Fit
            )

        } else {

            Text(
                text = "No photograph captured yet"
            )
        }

        // Open Camera Button
        Button(
            onClick = {

                val imageDirectory = File(
                    context.cacheDir,
                    "images"
                ).apply {
                    mkdirs()
                }

                val imageFile = File.createTempFile(
                    "captured_",
                    ".jpg",
                    imageDirectory
                )

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    imageFile
                )

                pendingUri = uri

                cameraLauncher.launch(uri)
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Open Camera")
        }

        // Share Button
        Button(
            onClick = {

                photoUri?.let { uri ->

                    val shareIntent = Intent(
                        Intent.ACTION_SEND
                    ).apply {

                        type = "image/jpeg"

                        putExtra(
                            Intent.EXTRA_STREAM,
                            uri
                        )

                        clipData = ClipData.newUri(
                            context.contentResolver,
                            "Captured Photo",
                            uri
                        )

                        addFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }

                    val chooser = Intent.createChooser(
                        shareIntent,
                        "Share Photograph"
                    )

                    context.startActivity(chooser)
                }
            },
            enabled = photoUri != null,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Share Photo")
        }
    }
}
