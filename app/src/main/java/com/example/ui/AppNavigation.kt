package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.EditorScreen
import com.example.ui.theme.Background
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.OutlineColor

import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onNavigateToEditor = { 
                    navController.navigate("editor") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("editor") {
            EditorScreen(
                onNavigateToAbout = { navController.navigate("about") }
            )
        }
        composable("about") {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun SplashScreen(onNavigateToEditor: () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onNavigateToEditor()
    }
    
    // Shared prefs to read the image URI if set from About page
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    val savedUriString = sharedPref.getString("dev_photo_uri", null)
    
    var imageError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(OutlineColor),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (savedUriString != null) android.net.Uri.parse(savedUriString) else R.drawable.developer_photo)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Developer Splash",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = { imageError = true },
                    onSuccess = { imageError = false }
                )
                if (imageError && savedUriString == null) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "App Logo",
                        tint = PrimaryAccent,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Sumit's Editor",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    
    val sharedPref = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    val savedUriString = sharedPref.getString("dev_photo_uri", null)
    
    var localImageUri by remember { mutableStateOf<android.net.Uri?>(if (savedUriString != null) android.net.Uri.parse(savedUriString) else null) }
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = java.io.File(context.filesDir, "dev_photo.jpg")
                val outputStream = java.io.FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                val newUri = android.net.Uri.fromFile(file)
                localImageUri = newUri
                sharedPref.edit().putString("dev_photo_uri", newUri.toString()).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Developer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Logo Placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(OutlineColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "App Logo",
                    tint = PrimaryAccent,
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Sumit's Editor",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Developer Profile Picture
            var devImageError by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(OutlineColor),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(localImageUri ?: R.drawable.developer_photo)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Developer Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = { devImageError = true },
                    onSuccess = { devImageError = false }
                )
                if (devImageError && localImageUri == null) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Developer Profile",
                        tint = TextSecondary,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { launcher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)
            ) {
                Text("Set My Photo")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Sumit Bhumihar",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/sumit_bhumihar_7"))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text("Instagram: @sumit_bhumihar_7")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                "Crafted with precision by Sumit Bhumihar | Professional Photo Studio",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
