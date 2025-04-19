package com.example.geminiapi

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// Holographic color scheme
val HoloCyan = Color(0xFF00FFFF)
val HoloBlue = Color(0xFF0088FF)
val HoloDarkBlue = Color(0xFF001030)
val HoloBackground = Color(0xFF000820)
val HoloPink = Color(0xFFFF00FF)

// Using the ChatMessage class from ChatMessage.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAgentScreen(
    aiViewModel: AIAgentViewModel = viewModel(),
    onNavigateToBaking: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    // TTS setup
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    
    // Initialize TTS engine
    DisposableEffect(context) {
        // Create a new TTS instance
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Set language after successful initialization
                val engine = ttsEngine // Capture the current engine
                if (engine != null) {
                    val result = engine.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported")
                    }
                }
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }
        
        // Store the engine reference
        ttsEngine = tts
        
        // Cleanup when leaving composition
        onDispose {
            ttsEngine?.stop()
            ttsEngine?.shutdown()
            ttsEngine = null
        }
    }
    
    // TTS DisposableEffect is handled above
    
    // Camera permission
    val cameraPermissionState = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraPermissionState.value = isGranted
    }
    
    // States
    val messages by aiViewModel.messages.collectAsState()
    val isLoading by aiViewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var showCamera by remember { mutableStateOf(false) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    val listState = rememberLazyListState()
    
    // Auto-scroll to the latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
    
    // Speak function
    fun speakText(text: String) {
        ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
    }
    
    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "screenTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            HoloCyan.copy(alpha = pulseAlpha),
            HoloBlue.copy(alpha = pulseAlpha * 0.7f)
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HoloBackground,
                        HoloDarkBlue
                    )
                )
            )
    ) {
        // Holographic elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI AGENT",
                    color = HoloCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                
                // Baking button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(HoloDarkBlue.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF9800).copy(alpha = pulseAlpha),  // Orange
                                    Color(0xFFE91E63).copy(alpha = pulseAlpha * 0.7f)  // Pink
                                )
                            ),
                            shape = CircleShape
                        )
                        .clickable(onClick = onNavigateToBaking),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cake,
                        contentDescription = "Baking Assistant",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Camera preview
            AnimatedVisibility(
                visible = showCamera,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp)
            ) {
                if (cameraPermissionState.value) {
                    CameraPreview(
                        onImageCaptured = { bitmap ->
                            capturedImage = bitmap
                            showCamera = false
                        },
                        onError = { Log.e("Camera", "Error: $it") }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 1.dp,
                                brush = borderBrush,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(HoloDarkBlue.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HoloCyan.copy(alpha = 0.2f)
                            )
                        ) {
                            Text("Grant Camera Permission")
                        }
                    }
                }
            }
            
            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (showCamera) 370.dp else 60.dp, bottom = 80.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(
                        message = message,
                        onPlayClick = { speakText(message.content) }
                    )
                }
                
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = HoloCyan,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                if (capturedImage != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(HoloDarkBlue.copy(alpha = 0.5f))
                                    .border(
                                        width = 1.dp,
                                        brush = borderBrush,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    "Captured Image",
                                    color = HoloCyan,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                
                                Image(
                                    bitmap = capturedImage!!.asImageBitmap(),
                                    contentDescription = "Captured image",
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = 1.dp,
                                            brush = borderBrush,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                aiViewModel.sendImageMessage(capturedImage!!, "What do you see in this image?")
                                                capturedImage = null
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = HoloCyan.copy(alpha = 0.2f)
                                        ),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text("Ask AI")
                                    }
                                    
                                    Button(
                                        onClick = { capturedImage = null },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Red.copy(alpha = 0.2f)
                                        )
                                    ) {
                                        Text("Discard")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                HoloDarkBlue.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Camera button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(HoloDarkBlue.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            brush = borderBrush,
                            shape = CircleShape
                        )
                        .clickable {
                            if (cameraPermissionState.value) {
                                showCamera = !showCamera
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Camera",
                        tint = HoloCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Text input
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "Type a message...",
                            color = HoloCyan.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedTextColor = HoloCyan,
                        focusedTextColor = HoloCyan,
                        cursorColor = HoloCyan,
                        focusedIndicatorColor = HoloCyan,
                        unfocusedIndicatorColor = HoloBlue.copy(alpha = 0.5f),
                        unfocusedContainerColor = HoloDarkBlue.copy(alpha = 0.3f),
                        focusedContainerColor = HoloDarkBlue.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Send button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotEmpty()) HoloCyan.copy(alpha = 0.2f)
                            else HoloDarkBlue.copy(alpha = 0.5f)
                        )
                        .border(
                            width = 1.dp,
                            brush = borderBrush,
                            shape = CircleShape
                        )
                        .clickable(enabled = inputText.isNotEmpty()) {
                            if (inputText.isNotEmpty()) {
                                coroutineScope.launch {
                                    aiViewModel.sendTextMessage(inputText)
                                    inputText = ""
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotEmpty()) HoloCyan else HoloCyan.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onPlayClick: (String) -> Unit
) {
    val isUser = message.isUser
    val bubbleColor = if (isUser) HoloBlue.copy(alpha = 0.2f) else HoloCyan.copy(alpha = 0.2f)
    val textColor = if (isUser) HoloBlue else HoloCyan
    val horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    val contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val borderBrush = Brush.linearGradient(
        colors = if (isUser) {
            listOf(HoloBlue, HoloPink.copy(alpha = 0.5f))
        } else {
            listOf(HoloCyan, HoloBlue.copy(alpha = 0.5f))
        }
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = contentAlignment
    ) {
        Column(
            horizontalAlignment = horizontalAlignment
        ) {
            // Sender label
            Text(
                text = if (isUser) "YOU" else "AI",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            
            // Message bubble
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
            ) {
                // Play button for AI messages
                if (!isUser) {
                    IconButton(
                        onClick = { onPlayClick(message.content) },
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Play message",
                            tint = textColor
                        )
                    }
                }
                
                // Message content
                Column(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            )
                        )
                        .background(bubbleColor)
                        .border(
                            width = 1.dp,
                            brush = borderBrush,
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            )
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = message.content,
                        color = textColor,
                        fontSize = 14.sp
                    )
                    
                    // Show image if present
                    message.image?.let { bitmap ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Message image",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
                
                // Play button for user messages
                if (isUser) {
                    IconButton(
                        onClick = { onPlayClick(message.content) },
                        modifier = Modifier
                            .size(32.dp)
                            .padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Play message",
                            tint = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    onImageCaptured: (Bitmap) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var preview by remember { mutableStateOf<Preview?>(null) }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(HoloCyan, HoloBlue)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
                
                val executor = ContextCompat.getMainExecutor(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    
                    preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        onError(e.message ?: "Camera binding failed")
                    }
                }, executor)
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Capture button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(HoloDarkBlue.copy(alpha = 0.5f))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(HoloCyan, HoloBlue)
                        ),
                        shape = CircleShape
                    )
                    .clickable {
                        captureImage(
                            imageCapture = imageCapture,
                            executor = ContextCompat.getMainExecutor(context),
                            onImageCaptured = onImageCaptured,
                            onError = onError
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(HoloCyan.copy(alpha = 0.3f))
                )
            }
        }
        
        // Close button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(HoloDarkBlue.copy(alpha = 0.7f))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(HoloCyan, HoloBlue)
                    ),
                    shape = CircleShape
                )
                .clickable { onImageCaptured(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close camera",
                tint = HoloCyan
            )
        }
    }
}

private fun captureImage(
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (Bitmap) -> Unit,
    onError: (String) -> Unit
) {
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = image.toBitmap()
                onImageCaptured(bitmap)
                image.close()
            }
            
            override fun onError(exception: ImageCaptureException) {
                onError(exception.message ?: "Image capture failed")
            }
        }
    )
}

private fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

suspend fun ImageCapture.takePicture(executor: Executor): Bitmap = suspendCoroutine { continuation ->
    takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = image.toBitmap()
                image.close()
                continuation.resume(bitmap)
            }
            
            override fun onError(exception: ImageCaptureException) {
                continuation.resume(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
            }
        }
    )
}
