package com.muthia0027.mobpro1.ui.screen

import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.muthia0027.mobpro1.BuildConfig
import com.muthia0027.mobpro1.R
import com.muthia0027.mobpro1.model.Item
import com.muthia0027.mobpro1.model.User
import com.muthia0027.mobpro1.network.ApiStatus
import com.muthia0027.mobpro1.network.UserDataStore
import com.muthia0027.mobpro1.ui.theme.Mobpro1Theme
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User("", "", ""))
    val viewModel: MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage

    var showDialog by remember { mutableStateOf(false) }
    var showItemDialog by remember { mutableStateOf(false) }

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    val launcher = rememberLauncherForActivityResult(CropImageContract()) {
        bitmap = getCroppedImage(context.contentResolver, it)
        if (bitmap != null) showItemDialog = true
    }

    val deleteStatus by viewModel.deleteStatus
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    LaunchedEffect(deleteStatus) {
        if (deleteStatus != null) {
            Toast.makeText(context, deleteStatus, Toast.LENGTH_SHORT).show()
            viewModel.clearDeleteStatus()
        }
    }
    var showEditDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    if (showContactDialog) {
        AboutDialog(onDismissRequest = { showContactDialog = false })
    }

    LaunchedEffect(Unit) {
        viewModel.retrieveData()
    }


    Scaffold (
        topBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 6.dp,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                color = Color(0xFFF8F8F8)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFD4AF37)
                            )
                            Text(
                                text = stringResource(R.string.app_slogan),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD4AF37)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color(0xFFD4AF37)
                    ),
                    actions = {
                        IconButton(onClick = { showContactDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = Color(0xFFD4AF37)
                            )
                        }

                        IconButton(onClick = {
                            if (user.email.isEmpty()) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    signIn(context, dataStore)
                                }
                            } else {
                                showDialog = true
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.account_circle),
                                contentDescription = stringResource(id = R.string.profil),
                                tint = Color(0xFFD4AF37)
                            )
                        }
                    }
                )
            }
        }
        ,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val options = CropImageContractOptions(
                    null,
                    CropImageOptions(
                        imageSourceIncludeGallery = false,
                        imageSourceIncludeCamera = true,
                        fixAspectRatio = true
                    )
                )
                launcher.launch(options)
            },
                containerColor = Color(0xFFD4AF37),
                ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.tambah_foto),
                    tint = Color(0xFFFFFBDE)
                )
            }
        }
    ){ innerPadding ->

        ScreenContent(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding),
            userId = user.email,
            onDeleteClick = {
                selectedItem = it
                showDeleteDialog = true
            },
            onEditClick = {
                selectedItem = it
                showEditDialog = true
            }
        )

        if (showDialog) {
            ProfilDialog(
                user = user,
                onDismissRequest = {
                    showDialog = false
                }
            ) {
                CoroutineScope(Dispatchers.IO).launch {
                    signOut(context, dataStore)
                }
                showDialog = false
            }
        }

        if (showItemDialog) {
            ItemDialog(
                bitmap = bitmap,
                onDismissRequest = {
                    showItemDialog = false
                }
            ) { title, description, detail ->

                viewModel.saveData(
                    name = title,
                    desc = description,
                    bitmap = bitmap!!,
                    ownerId = user.email,
                    detail = detail
                )

                showItemDialog = false
            }
        }

        if (showDeleteDialog && selectedItem != null) {

            DeleteConfirmDialog(
                item = selectedItem!!,
                onDismiss = {
                    showDeleteDialog = false
                    selectedItem = null
                },
                onConfirm = {

                    viewModel.deleteData(
                        selectedItem!!.id
                    )

                    showDeleteDialog = false
                    selectedItem = null
                }
            )
        }

        if (showEditDialog && selectedItem != null) {

            ItemEditDialog(
                bitmap = null,
                initialTitle = selectedItem!!.name,
                initialDescription = selectedItem!!.desc,
                initialDetail = selectedItem!!.detail,
                onDismissRequest = {
                    showEditDialog = false
                    selectedItem = null
                },
                onUpdate = { newTitle, newDescription, newDetail ->

                    viewModel.updateData(
                        id = selectedItem!!.id,
                        name = newTitle,
                        desc = newDescription,
                        bitmap = null,
                        detail = newDetail
                    )

                    showEditDialog = false
                    selectedItem = null
                }
            )
        }

        if (errorMessage != null) {

            Toast.makeText(
                context,
                errorMessage,
                Toast.LENGTH_LONG
            ).show()

            viewModel.clearMessage()
        }
    }
}

@Composable
fun ScreenContent(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    userId: String,
    onDeleteClick: (Item) -> Unit,
    onEditClick: (Item) -> Unit
) {

    val data by viewModel.data
    val status by viewModel.status.collectAsState()

    when {

        status == ApiStatus.LOADING -> {

            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        status == ApiStatus.ERROR && data.isEmpty() -> {

            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = stringResource(
                        id = R.string.error
                    )
                )

                Button(
                    onClick = {
                        viewModel.retrieveData()
                    },
                    modifier = Modifier.padding(
                        top = 16.dp
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 32.dp,
                        vertical = 16.dp
                    )
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.try_again
                        )
                    )
                }
            }
        }

        else -> {

            LazyVerticalGrid(
                modifier = modifier
                    .fillMaxSize()
                    .padding(4.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    bottom = 80.dp
                )
            ) {

                items(data) { item ->

                    ListItem(
                        item = item,
                        isUserLoggedIn = userId.isNotEmpty(),
                        currentUserEmail = userId,
                        onDeleteClick = {
                            onDeleteClick(item)
                        },
                        onEditClick = {
                            onEditClick(item)
                        }
                    )
                }
            }
        }
    }
}

private suspend fun signIn(context: Context, dataStore: UserDataStore) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(result: GetCredentialResponse, dataStore: UserDataStore) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.saveData(
                User(
                    name = nama,
                    email = email,
                    photoUrl = photoUrl
                )
            )
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    } else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type.")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User("", "", ""))
    } catch (e: ClearCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful) {
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }

    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

@Composable
fun ListItem(
    item: Item,
    isUserLoggedIn: Boolean,
    currentUserEmail: String?,
    onDeleteClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null
) {
    val priceValue = item.desc?.toString()?.toDoubleOrNull() ?: 0.0
    val formattedPrice = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        .format(priceValue)

    val isOwner =
        currentUserEmail == item.ownerId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column {

            AsyncImage(
                model = ImageRequest.Builder(
                    LocalContext.current
                )
                    .data(item.gambar)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(
                    R.string.gambar,
                    item.name
                ),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(
                    id = R.drawable.loading_img
                ),
                error = painterResource(
                    id = R.drawable.broken_img
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(12.dp)
            ) {

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFD4AF37)
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = item.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = formattedPrice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }

            if (isUserLoggedIn && isOwner) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        ),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    onEditClick?.let {

                        IconButton(
                            onClick = it
                        ) {

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(
                                    R.string.edit
                                ),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    onDeleteClick?.let {

                        IconButton(
                            onClick = it
                        ) {

                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(
                                    R.string.hapus
                                ),
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainScreenPreview() {
    Mobpro1Theme {
        MainScreen()
    }
}