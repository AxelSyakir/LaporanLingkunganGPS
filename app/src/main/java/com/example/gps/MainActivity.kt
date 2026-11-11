package com.example.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import com.example.gps.ui.theme.component.MainTopBar
import com.example.gps.ui.theme.component.MainBottomBar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.gps.ui.theme.GPSTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

enum class Screen { Login, SignUp, Community, Post, Search, Profile, Settings, News, Preferences, Manage }

enum class PostStatus(val label: String) {
    INCOMPLETE("Incomplete"),
    PROCESS("Process"),
    COMPLETED("Completed")
}

data class Community(
    val name: String = "",
    val code: String = "",
    val createdBy: String = "",
    val description: String = "",
    val moderators: List<String> = emptyList(),
    val members: List<String> = emptyList(),
    val bannedMembers: List<String> = emptyList()
)

data class Post(
    val id: String = "",
    val communityCode: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val locationTag: String = "",
    val description: String = "",
    val status: String = PostStatus.INCOMPLETE.name.lowercase(),
    val authorId: String = "",
    val authorName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GPSTheme {
                var currentScreen by remember {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    mutableStateOf(if (currentUser != null) Screen.Community else Screen.Login)
                }
                var previousScreen by remember { mutableStateOf(Screen.Post) }
                var community by remember { mutableStateOf<Community?>(null) }

                val showBars = currentScreen == Screen.Post || currentScreen == Screen.Search || currentScreen == Screen.Profile

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (showBars) {
                            MainTopBar(
                                onSettingsClick = {
                                    previousScreen = currentScreen
                                    currentScreen = Screen.Settings
                                },
                                onNewsClick = {
                                    previousScreen = currentScreen
                                    currentScreen = Screen.News
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (showBars) {
                            MainBottomBar(
                                onPostClick = { currentScreen = Screen.Post },
                                onSearchClick = { currentScreen = Screen.Search },
                                onProfileClick = { currentScreen = Screen.Profile }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            Screen.Login -> LoginScreen(
                                onLoggedIn = { currentScreen = Screen.Community },
                                onSignup = { currentScreen = Screen.SignUp }
                            )

                            Screen.SignUp -> SignUpScreen(
                                onSignedUp = { currentScreen = Screen.Community },
                                onBackToLogin = { currentScreen = Screen.Login }
                            )

                            Screen.Community -> CommunityScreen(
                                onEntered = {
                                    community = it
                                    currentScreen = Screen.Post
                                }
                            )

                            Screen.Post -> PostScreen(
                                community = community,
                                onLeave = { currentScreen = Screen.Community }
                            )

                            Screen.Search -> SearchScreen()

                            Screen.Profile -> ProfileScreen()

                            Screen.Settings -> SettingsScreen(
                                community = community,
                                onBack = { currentScreen = previousScreen },
                                onCommunitiesClick = { currentScreen = Screen.Community },
                                onPreferencesClick = { currentScreen = Screen.Preferences },
                                onManageClick = { currentScreen = Screen.Manage }
                            )

                            Screen.News -> NewsScreen(
                                onBack = { currentScreen = previousScreen }
                            )

                            Screen.Preferences -> PreferencesScreen(
                                onBack = { currentScreen = Screen.Settings }
                            )

                            Screen.Manage -> ManageScreen(
                                community = community,
                                onCommunityDeleted = {
                                    community = null
                                    currentScreen = Screen.Community
                                },
                                onBack = { currentScreen = Screen.Settings }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityScreen(onEntered: (Community) -> Unit) {
    var isCreating by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var joinedCommunities by remember { mutableStateOf<List<Community>>(emptyList()) }

    val isInspection = LocalInspectionMode.current
    val currentUser = if (isInspection) null else try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null }
    val db = if (isInspection) null else try { FirebaseFirestore.getInstance() } catch (_: Exception) { null }
    val uid = currentUser?.uid ?: ""

    fun isValidCode(c: String) = c.length >= 4 && c.all { it.isLetterOrDigit() }

    LaunchedEffect(uid) {
        if (uid.isNotBlank() && db != null) {
            db.collection("communities").whereArrayContains("members", uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            val commName = doc.getString("name") ?: ""
                            val commCode = doc.getString("code") ?: doc.id
                            val createdBy = doc.getString("createdBy") ?: ""
                            val moderators = (doc.get("moderators") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                            val members = (doc.get("members") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                            val bannedMembers = (doc.get("bannedMembers") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                            if (!bannedMembers.contains(uid)) {
                                Community(commName, commCode, createdBy, "", moderators, members, bannedMembers)
                            } else null
                        }
                        joinedCommunities = list
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.communityicon),
            contentDescription = null,
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(Modifier.height(12.dp))

        if (joinedCommunities.isNotEmpty()) {
            Text(
                text = "Your Communities",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(8.dp))

            joinedCommunities.forEach { comm ->
                Surface(
                    onClick = { onEntered(comm) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = comm.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Code: ${comm.code}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = { onEntered(comm) },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text("Enter", style = MaterialTheme.typography.labelSmall)
                        }

                        TextButton(
                            onClick = {
                                if (uid.isNotBlank() && db != null) {
                                    db.collection("communities").document(comm.code).update(
                                        mapOf(
                                            "members" to FieldValue.arrayRemove(uid),
                                            "moderators" to FieldValue.arrayRemove(uid)
                                        )
                                    )
                                }
                            }
                        ) {
                            Text("Leave", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        Text(
            if (isCreating) "Create a community" else "Join a community",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))

        if (isCreating) {
            OutlinedTextField(
                name, { name = it },
                label = { Text("Community name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            code, { code = it.uppercase() },
            label = { Text(if (isCreating) "Choose a code" else "Community code") },
            supportingText = if (isCreating) {
                { Text("At least 4 letters or numbers") }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))

        Button(
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val cleanCode = code.trim()
                val cleanName = name.trim()
                error = null

                if (!isValidCode(cleanCode)) {
                    error = "Code must be at least 4 letters or numbers"
                    return@Button
                }

                if (isCreating) {
                    if (cleanName.isBlank()) {
                        error = "Please enter a community name"
                        return@Button
                    }
                    loading = true
                    val ref = db?.collection("communities")?.document(cleanCode)
                    if (ref != null) {
                        db.runTransaction { tx ->
                            if (tx.get(ref).exists()) {
                                throw FirebaseFirestoreException(
                                    "That code is already taken",
                                    FirebaseFirestoreException.Code.ALREADY_EXISTS
                                )
                            }
                            tx.set(
                                ref, mapOf(
                                    "name" to cleanName,
                                    "code" to cleanCode,
                                    "createdBy" to uid,
                                    "moderators" to emptyList<String>(),
                                    "members" to listOf(uid),
                                    "bannedMembers" to emptyList<String>()
                                )
                            )
                            null
                        }
                            .addOnSuccessListener {
                                loading = false
                                onEntered(Community(cleanName, cleanCode, uid, "", emptyList(), listOf(uid), emptyList()))
                            }
                            .addOnFailureListener {
                                loading = false
                                error = it.message
                            }
                    }
                } else {
                    loading = true
                    db?.collection("communities")?.document(cleanCode)?.get()
                        ?.addOnSuccessListener { doc ->
                            loading = false
                            if (doc.exists()) {
                                val bannedMembers = (doc.get("bannedMembers") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                                if (bannedMembers.contains(uid)) {
                                    error = "You are banned from this community."
                                    return@addOnSuccessListener
                                }

                                val createdBy = doc.getString("createdBy") ?: ""
                                val moderators = (doc.get("moderators") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                                val members = (doc.get("members") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                                if (!members.contains(uid) && uid.isNotEmpty()) {
                                    db.collection("communities").document(cleanCode)
                                        .update("members", FieldValue.arrayUnion(uid))
                                }
                                val commName = doc.getString("name") ?: ""
                                val updatedMembers = if (members.contains(uid)) members else members + uid
                                onEntered(Community(commName, cleanCode, createdBy, "", moderators, updatedMembers, bannedMembers))
                            } else {
                                error = "No community found with that code"
                            }
                        }
                        ?.addOnFailureListener {
                            loading = false
                            error = it.message
                        }
                }
            }
        ) {
            Text(if (isCreating) "Create" else "Join")
        }

        TextButton(
            onClick = { isCreating = !isCreating; error = null },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isCreating) "Have a code? Join instead" else "Create a new community")
        }
    }
}

@Composable
fun SignUpScreen(onSignedUp: () -> Unit, onBackToLogin: () -> Unit) {
    val image = painterResource(R.drawable.placeholdersignup)
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    //Returns to loginScreen
    BackHandler { onBackToLogin() }

    Column(Modifier
        .fillMaxSize()
        .padding(24.dp), verticalArrangement = Arrangement.Center) {
        Image(
            painter = image,
            contentDescription = null,
            modifier = Modifier
                .size(128.dp)
                .align(Alignment.CenterHorizontally)
        )
        Text(
            "Sign In",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            password, { password = it }, label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                error = "Please fill in all fields"
            } else {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener { result ->
                        // Save the name on the new user's profile
                        val profile = UserProfileChangeRequest.Builder()
                            .setDisplayName(name.trim())
                            .build()
                        result.user?.updateProfile(profile)
                        onSignedUp()
                    }
                    .addOnFailureListener { error = it.message }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Sign up")
        }

        TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
            Text("Already have an account? Log in")
        }
    }
}

@Composable
fun LoginScreen(onLoggedIn: () -> Unit, onSignup: () -> Unit) {
    val image = painterResource(R.drawable.placeholderlogo)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier
        .fillMaxSize()
        .padding(24.dp), verticalArrangement = Arrangement.Center) {
        Image(
            painter = image,
            contentDescription = null,
            modifier = Modifier
                .size(128.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            password, { password = it }, label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (email.isBlank() || password.isBlank()) {
                error = "Please enter your email and password"
            } else {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener { onLoggedIn() }
                    .addOnFailureListener { error = it.message }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Log in")
        }

        TextButton(onClick = onSignup, modifier = Modifier.fillMaxWidth()) {
            Text("Create account")
        }
    }
}

@Composable
fun PostStatusBadge(status: String) {
    val (backgroundColor, textColor, label) = when (status.lowercase()) {
        "completed" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Completed")
        "process" -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Process")
        else -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Incomplete")
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PostCard(
    post: Post,
    isModerator: Boolean,
    onStatusChange: (String) -> Unit,
    onDeletePost: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val formattedDate = remember(post.createdAt) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        sdf.format(Date(post.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Author, Date, and Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                PostStatusBadge(status = post.status)

                if (isModerator) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Post Options")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Set Status: Incomplete") },
                                onClick = {
                                    onStatusChange("incomplete")
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Set Status: Process") },
                                onClick = {
                                    onStatusChange("process")
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Set Status: Completed") },
                                onClick = {
                                    onStatusChange("completed")
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Post", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge
            )

            // Location Tag
            if (post.locationTag.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.locationTag,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Image
            if (post.imageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            // Description
            if (post.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to delete this post?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeletePost()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LocationPickerDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val presetLocations = listOf(
        "Central Park, NYC",
        "Monas, Jakarta",
        "Town Square Plaza",
        "University Campus",
        "City Library",
        "Downtown Coffee Shop",
        "Community Center"
    )

    val filteredLocations = remember(searchQuery) {
        if (searchQuery.isBlank()) presetLocations
        else presetLocations.filter { it.contains(searchQuery, ignoreCase = true) } + searchQuery
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Tag Location")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search or type location") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Suggested Places:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                LazyColumn(modifier = Modifier.height(180.dp)) {
                    items(filteredLocations) { place ->
                        Surface(
                            onClick = {
                                onLocationSelected(place)
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(place, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (searchQuery.isNotBlank()) {
                        onLocationSelected(searchQuery)
                        onDismiss()
                    }
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@SuppressLint("MissingPermission")
fun getDeviceLocationName(context: Context): String {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager != null) {
            val hasGps = try { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } catch (_: Exception) { false }
            val hasNetwork = try { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { false }

            val lastKnownLocation = if (hasGps) {
                try { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (_: SecurityException) { null }
            } else if (hasNetwork) {
                try { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (_: SecurityException) { null }
            } else null

            if (lastKnownLocation != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lastKnownLocation.latitude, lastKnownLocation.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val city = address.locality ?: address.subAdminArea ?: address.adminArea
                    val country = address.countryName
                    if (!city.isNullOrBlank() && !country.isNullOrBlank()) {
                        return "$city, $country"
                    } else if (!city.isNullOrBlank()) {
                        return city
                    }
                }
            }
        }
    } catch (_: Exception) {}
    return "Current Location"
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, imageUrl: String, locationTag: String, description: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var locationTag by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // Auto-detect and pre-fill user's location on dialog open
    LaunchedEffect(Unit) {
        if (locationTag.isBlank()) {
            locationTag = getDeviceLocationName(context)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) selectedImageUri = uri }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                selectedImageUri = tempCameraUri
            }
        }
    )

    fun launchCamera() {
        try {
            val file = File(context.externalCacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            error = "Failed to launch camera: ${e.message}"
        }
    }

    AlertDialog(
        onDismissRequest = if (isUploading) { {} } else onDismiss,
        title = { Text("Create Post") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Location Tag Field
                OutlinedTextField(
                    value = locationTag,
                    onValueChange = { locationTag = it },
                    label = { Text("Location Tag") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showLocationPicker = true }) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Pick Location", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Image Picker / Camera controls
                Text("Post Photo", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))

                if (selectedImageUri != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = Color.Red)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Gallery")
                    }

                    Button(
                        onClick = { launchCamera() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Camera")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isUploading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Uploading image...", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isUploading,
                onClick = {
                    if (title.isBlank() || description.isBlank()) {
                        error = "Title and Description are required"
                    } else if (selectedImageUri != null) {
                        isUploading = true
                        error = null
                        val uri = selectedImageUri!!
                        try {
                            val storageRef = FirebaseStorage.getInstance().reference.child("posts/${UUID.randomUUID()}.jpg")
                            storageRef.putFile(uri)
                                .addOnSuccessListener {
                                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                        isUploading = false
                                        onSubmit(title, downloadUri.toString(), locationTag, description)
                                    }.addOnFailureListener {
                                        isUploading = false
                                        error = "Failed to get image URL: ${it.message}"
                                    }
                                }
                                .addOnFailureListener {
                                    isUploading = false
                                    error = "Failed to upload image: ${it.message}"
                                }
                        } catch (e: Exception) {
                            isUploading = false
                            error = "Storage error: ${e.message}"
                        }
                    } else {
                        onSubmit(title, "", locationTag, description)
                    }
                }
            ) {
                Text(if (isUploading) "Posting..." else "Post")
            }
        },
        dismissButton = {
            if (!isUploading) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )

    if (showLocationPicker) {
        LocationPickerDialog(
            onDismiss = { showLocationPicker = false },
            onLocationSelected = { location -> locationTag = location }
        )
    }
}

@Composable
fun PostScreen(community: Community?, onLeave: () -> Unit) {
    BackHandler { onLeave() }

    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val isInspection = LocalInspectionMode.current
    val currentUser = if (isInspection) null else try { FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
    val db = if (isInspection) null else try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }

    val isOwner = community?.createdBy == currentUser?.uid
    val isModerator = isOwner || (community?.moderators?.contains(currentUser?.uid) == true)

    // Real-time listener for ban enforcement & community deletion
    LaunchedEffect(community?.code) {
        val code = community?.code
        if (!code.isNullOrBlank() && db != null) {
            db.collection("communities").document(code)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot == null || !snapshot.exists()) {
                        onLeave()
                    } else {
                        val banned = (snapshot.get("bannedMembers") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                        if (currentUser?.uid != null && banned.contains(currentUser.uid)) {
                            onLeave()
                        }
                    }
                }
        }
    }

    // Real-time listener for posts in this community
    LaunchedEffect(community?.code) {
        val code = community?.code
        if (!code.isNullOrBlank() && db != null) {
            db.collection("communities").document(code).collection("posts")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val fetchedPosts = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Post::class.java)?.copy(id = doc.id)
                        }.sortedByDescending { it.createdAt }
                        posts = fetchedPosts
                    }
                }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (posts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No posts yet in this community.\nTap '+' to create one!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            isModerator = isModerator,
                            onStatusChange = { newStatus ->
                                community?.code?.let { code ->
                                    db?.collection("communities")?.document(code)
                                        ?.collection("posts")?.document(post.id)
                                        ?.update("status", newStatus)
                                }
                            },
                            onDeletePost = {
                                community?.code?.let { code ->
                                    db?.collection("communities")?.document(code)
                                        ?.collection("posts")?.document(post.id)
                                        ?.delete()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePostDialog(
            onDismiss = { showCreateDialog = false },
            onSubmit = { title, imageUrl, locationTag, description ->
                val code = community?.code
                if (!code.isNullOrBlank() && db != null) {
                    val ref = db.collection("communities").document(code).collection("posts").document()
                    val newPost = Post(
                        id = ref.id,
                        communityCode = code,
                        title = title,
                        imageUrl = imageUrl,
                        locationTag = locationTag,
                        description = description,
                        status = PostStatus.INCOMPLETE.name.lowercase(),
                        authorId = currentUser?.uid ?: "",
                        authorName = currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "Member",
                        createdAt = System.currentTimeMillis()
                    )
                    ref.set(newPost)
                }
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun SearchScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Search Screen", style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun SettingsOptionRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsScreen(
    community: Community?,
    onBack: () -> Unit,
    onCommunitiesClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onManageClick: () -> Unit
) {
    BackHandler { onBack() }

    val isInspection = LocalInspectionMode.current
    val currentUser = if (isInspection) null else try { FirebaseAuth.getInstance().currentUser } catch (_: Throwable) { null }
    val isOwner = community?.createdBy == currentUser?.uid
    val isModerator = isOwner || (community?.moderators?.contains(currentUser?.uid) == true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close settings"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsOptionRow(
            title = "Communities",
            icon = Icons.Default.Home,
            onClick = onCommunitiesClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsOptionRow(
            title = "Preferences",
            icon = Icons.Default.Tune,
            onClick = onPreferencesClick
        )

        // Manage option is only visible to Owner and Moderators
        if (isModerator) {
            Spacer(modifier = Modifier.height(16.dp))

            SettingsOptionRow(
                title = "Manage",
                icon = Icons.Default.Build,
                onClick = onManageClick
            )
        }
    }
}

@Composable
fun NewsScreen(onBack: () -> Unit) {
    BackHandler { onBack() }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("News Screen", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}

@Composable
fun PreferencesScreen(onBack: () -> Unit) {
    BackHandler { onBack() }

    var joinedCommunities by remember { mutableStateOf<List<Community>>(emptyList()) }
    val isInspection = LocalInspectionMode.current
    val currentUser = if (isInspection) null else try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null }
    val db = if (isInspection) null else try { FirebaseFirestore.getInstance() } catch (_: Exception) { null }
    val uid = currentUser?.uid ?: ""

    LaunchedEffect(uid) {
        if (uid.isNotBlank() && db != null) {
            db.collection("communities").whereArrayContains("members", uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            val commName = doc.getString("name") ?: ""
                            val commCode = doc.getString("code") ?: doc.id
                            val createdBy = doc.getString("createdBy") ?: ""
                            val moderators = (doc.get("moderators") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                            val members = (doc.get("members") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                            val bannedMembers = (doc.get("bannedMembers") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                            Community(commName, commCode, createdBy, "", moderators, members, bannedMembers)
                        }
                        joinedCommunities = list
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(8.dp))
            Text("Joined Communities", style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (joinedCommunities.isEmpty()) {
            Text("You have not joined any community yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(joinedCommunities) { comm ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = comm.name, style = MaterialTheme.typography.titleMedium)
                                Text(text = "Code: ${comm.code}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            TextButton(
                                onClick = {
                                    if (uid.isNotBlank() && db != null) {
                                        db.collection("communities").document(comm.code).update(
                                            mapOf(
                                                "members" to FieldValue.arrayRemove(uid),
                                                "moderators" to FieldValue.arrayRemove(uid)
                                            )
                                        )
                                    }
                                }
                            ) {
                                Text("Leave", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageScreen(
    community: Community?,
    onCommunityDeleted: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val isInspection = LocalInspectionMode.current
    val currentUser = if (isInspection) null else try { FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
    val db = if (isInspection) null else try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }

    var liveCommunity by remember { mutableStateOf(community) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val code = community?.code
    val isOwner = liveCommunity?.createdBy == currentUser?.uid
    val isModerator = isOwner || (liveCommunity?.moderators?.contains(currentUser?.uid) == true)

    // Real-time listener for community document updates & deletion detection
    LaunchedEffect(code) {
        if (!code.isNullOrBlank() && db != null) {
            db.collection("communities").document(code)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot == null || !snapshot.exists()) {
                        // Community was deleted
                        onCommunityDeleted()
                    } else {
                        val name = snapshot.getString("name") ?: ""
                        val createdBy = snapshot.getString("createdBy") ?: ""
                        val moderators = (snapshot.get("moderators") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                        val members = (snapshot.get("members") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                        val bannedMembers = (snapshot.get("bannedMembers") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                        if (currentUser?.uid != null && bannedMembers.contains(currentUser.uid)) {
                            onBack()
                        } else {
                            liveCommunity = Community(name, code, createdBy, "", moderators, members, bannedMembers)
                        }
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Manage Community",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Members (${liveCommunity?.members?.size ?: 0})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        val memberList = liveCommunity?.members ?: emptyList()

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(memberList) { memberUid ->
                val isMemberOwner = memberUid == liveCommunity?.createdBy
                val isMemberMod = liveCommunity?.moderators?.contains(memberUid) == true

                val roleLabel = when {
                    isMemberOwner -> "Owner"
                    isMemberMod -> "Moderator"
                    else -> "Member"
                }

                val roleColor = when {
                    isMemberOwner -> Color(0xFFE65100)
                    isMemberMod -> Color(0xFF1565C0)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (memberUid == currentUser?.uid) "You ($memberUid)" else memberUid,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                            Text(
                                text = roleLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = roleColor
                            )
                        }

                        if (isOwner && !isMemberOwner) {
                            if (isMemberMod) {
                                Button(
                                    onClick = {
                                        if (!code.isNullOrBlank()) {
                                            db?.collection("communities")?.document(code)
                                                ?.update("moderators", FieldValue.arrayRemove(memberUid))
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Text("Remove Mod", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (!code.isNullOrBlank()) {
                                            db?.collection("communities")?.document(code)
                                                ?.update("moderators", FieldValue.arrayUnion(memberUid))
                                        }
                                    },
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Text("Make Mod", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        val isMemberOwner = memberUid == liveCommunity?.createdBy
                        val isMemberMod = liveCommunity?.moderators?.contains(memberUid) == true
                        val canBan = (isOwner && !isMemberOwner) || (isModerator && !isMemberOwner && !isMemberMod)

                        if (canBan) {
                            Button(
                                onClick = {
                                    if (!code.isNullOrBlank() && db != null) {
                                        db.collection("communities").document(code).update(
                                            mapOf(
                                                "bannedMembers" to FieldValue.arrayUnion(memberUid),
                                                "members" to FieldValue.arrayRemove(memberUid),
                                                "moderators" to FieldValue.arrayRemove(memberUid)
                                            )
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text("Ban", color = MaterialTheme.colorScheme.onError, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        if (isOwner) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Community", color = MaterialTheme.colorScheme.onError)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Community") },
            text = { Text("Are you sure you want to delete this community? All posts and data will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        code?.let { communityCode ->
                            db?.collection("communities")?.document(communityCode)?.delete()
                                ?.addOnSuccessListener { onCommunityDeleted() }
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Profile Screen", style = MaterialTheme.typography.headlineSmall)
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    GPSTheme {
        SettingsScreen(
            community = Community("Sample", "1234", "ownerUid"),
            onBack = {},
            onCommunitiesClick = {},
            onPreferencesClick = {},
            onManageClick = {}
        )
    }
}