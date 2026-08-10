package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.theme.CanvasBg
import com.example.ui.theme.OverlayBg
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.OutlineColor
import com.example.ui.theme.ButtonActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onNavigateToAbout: () -> Unit,
    viewModel: EditorViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isComparing by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
        }
    }

    LaunchedEffect(state.aiMessage) {
        state.aiMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.background
            ) {
                Spacer(Modifier.height(32.dp))
                Text(
                    "Menu",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider(color = OutlineColor)
                NavigationDrawerItem(
                    label = { Text("About Developer", color = MaterialTheme.colorScheme.onBackground) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToAbout()
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Sumit's Editor", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { launcher.launch("image/*") }) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Upload Image")
                        }
                        Button(
                            onClick = { isComparing = !isComparing },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isComparing) MaterialTheme.colorScheme.primary else OutlineColor
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(if (isComparing) "After" else "Before", color = if (isComparing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground)
                        }
                    }
                )
            },
            containerColor = CanvasBg
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            // Main Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(CanvasBg)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offset += pan
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isComparing = true
                                tryAwaitRelease()
                                isComparing = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Color Matrix logic
                val colorMatrix = ColorMatrix().apply {
                    if (!isComparing) {
                        val androidMatrix = android.graphics.ColorMatrix()
                        
                        // Adjust Brightness
                        val b = state.brightness * 255
                        val brightnessMatrix = android.graphics.ColorMatrix(floatArrayOf(
                            1f, 0f, 0f, 0f, b,
                            0f, 1f, 0f, 0f, b,
                            0f, 0f, 1f, 0f, b,
                            0f, 0f, 0f, 1f, 0f
                        ))
                        
                        // Adjust Contrast
                        val c = state.contrast
                        val t = (1.0f - c) * 255.0f / 2.0f
                        val contrastMatrix = android.graphics.ColorMatrix(floatArrayOf(
                            c, 0f, 0f, 0f, t,
                            0f, c, 0f, 0f, t,
                            0f, 0f, c, 0f, t,
                            0f, 0f, 0f, 1f, 0f
                        ))
                        
                        androidMatrix.setConcat(contrastMatrix, brightnessMatrix)
                        
                        // Adjust Saturation
                        val satMatrix = android.graphics.ColorMatrix().apply { setSaturation(state.saturation) }
                        androidMatrix.setConcat(satMatrix, androidMatrix)
                        
                        // Adjust Warmth (Tinting Red/Blue)
                        val w = state.warmth
                        val rScale = 1f + (w * 0.2f)
                        val bScale = 1f - (w * 0.2f)
                        val warmthMatrix = android.graphics.ColorMatrix(floatArrayOf(
                            rScale, 0f, 0f, 0f, 0f,
                            0f, 1f, 0f, 0f, 0f,
                            0f, 0f, bScale, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        ))
                        androidMatrix.setConcat(warmthMatrix, androidMatrix)
                        
                        // Set the compose matrix from android matrix
                        this.values.forEachIndexed { index, _ -> 
                            this.values[index] = androidMatrix.array[index]
                        }
                    }
                }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri ?: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=1000") // Sample Portrait or User Image
                        .crossfade(true)
                        .build(),
                    contentDescription = "Editing Canvas",
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.colorMatrix(colorMatrix),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
                
                if (isComparing) {
                    Text(
                        "BEFORE",
                        color = TextMain,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .background(OverlayBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // Toolbar Modules
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item { CircularDial("Exposure", state.brightness, -1f..1f, viewModel::updateBrightness) }
                item { CircularDial("Contrast", state.contrast, 0f..2f, viewModel::updateContrast) }
                item { CircularDial("Saturation", state.saturation, 0f..2f, viewModel::updateSaturation) }
                item { CircularDial("Warmth", state.warmth, -1f..1f, viewModel::updateWarmth) }
                item { CircularDial("Bokeh", state.blur, 0f..25f, viewModel::updateBlur) }
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                        if (state.isAIProcessing) {
                            CircularProgressIndicator(color = PrimaryAccent)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Analyzing...", color = TextMain, fontSize = 12.sp)
                        } else {
                            Button(
                                onClick = { viewModel.applyAIRetouch() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Retouch")
                            }
                        }
                    }
                }
                val presets = listOf("Moody", "Vintage Film", "Dramatic B&W")
                items(presets) { preset ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
                        Button(
                            onClick = { viewModel.applyPreset(preset) },
                            colors = ButtonDefaults.buttonColors(containerColor = OutlineColor, contentColor = TextMain)
                        ) {
                            Text(preset)
                        }
                    }
                }
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Button(
                            onClick = { 
                                scope.launch { snackbarHostState.showSnackbar("Exporting image to gallery...") } 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


}
