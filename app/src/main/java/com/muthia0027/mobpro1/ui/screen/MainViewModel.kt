package com.muthia0027.mobpro1.ui.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muthia0027.mobpro1.model.Item
import com.muthia0027.mobpro1.network.ItemRequest
import com.muthia0027.mobpro1.network.ApiStatus
import com.muthia0027.mobpro1.network.CloudinaryApi
import com.muthia0027.mobpro1.network.ItemApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class MainViewModel : ViewModel() {

    var data = mutableStateOf(emptyList<Item>())
        private set

    var status = MutableStateFlow(ApiStatus.LOADING)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    var deleteStatus = mutableStateOf<String?>(null)
        private set

    fun retrieveData() {

        viewModelScope.launch(Dispatchers.IO) {

            status.value = ApiStatus.LOADING

            try {

                val result =
                    ItemApi.service.getItems()

                withContext(Dispatchers.Main) {
                    data.value = result
                    status.value = ApiStatus.SUCCESS
                }

            } catch (e: Exception) {

                Log.e(
                    "MainViewModel",
                    e.message ?: "Unknown Error"
                )

                withContext(Dispatchers.Main) {
                    status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun saveData(
        name: String,
        desc: String,
        bitmap: Bitmap,
        ownerId: String,
        detail: String
    ) {

        viewModelScope.launch(Dispatchers.IO) {

            try {

                val cloudinaryResponse =
                    CloudinaryApi.service.uploadImage(
                        bitmap.toMultipartBody(),
                        "mobpro_upload"
                            .toRequestBody("text/plain".toMediaType())
                    )

                val imageUrl =
                    cloudinaryResponse.secure_url

                val request =
                    ItemRequest(
                        name = name,
                        desc = desc,
                        gambar = imageUrl,
                        ownerId = ownerId,
                        detail = detail
                    )

                ItemApi.service.createItem(request)

                retrieveData()

            } catch (e: Exception) {

                Log.e(
                    "MainViewModel",
                    e.message ?: "Unknown Error"
                )

                errorMessage.value =
                    "Gagal menyimpan data"
            }
        }
    }

    fun updateData(
        id: String,
        name: String,
        desc: String,
        bitmap: Bitmap?,
        detail: String
    ) {

        viewModelScope.launch(Dispatchers.IO) {

            try {

                val existingItem =
                    data.value.find { it.id == id }
                        ?: return@launch

                var imageUrl =
                    existingItem.gambar

                if (bitmap != null) {

                    val cloudinaryResponse =
                        CloudinaryApi.service.uploadImage(
                            bitmap.toMultipartBody(),
                            "mobpro_upload"
                                .toRequestBody("text/plain".toMediaType())
                        )

                    imageUrl =
                        cloudinaryResponse.secure_url
                }

                val request =
                    ItemRequest(
                        name = name,
                        desc = desc,
                        gambar = imageUrl,
                        ownerId = existingItem.ownerId,
                        detail = detail
                    )

                ItemApi.service.updateItem(
                    id,
                    request
                )

                retrieveData()

            } catch (e: Exception) {

                Log.e(
                    "MainViewModel",
                    e.message ?: "Unknown Error"
                )

                errorMessage.value =
                    "Gagal mengupdate data"
            }
        }
    }

    fun deleteData(
        id: String
    ) {

        viewModelScope.launch(Dispatchers.IO) {

            try {

                ItemApi.service.deleteItem(id)

                deleteStatus.value =
                    "Data berhasil dihapus"

                retrieveData()

            } catch (e: Exception) {

                Log.e(
                    "MainViewModel",
                    e.message ?: "Unknown Error"
                )

                deleteStatus.value =
                    "Gagal menghapus data"
            }
        }
    }

    fun clearDeleteStatus() {
        deleteStatus.value = null
    }

    fun clearMessage() {
        errorMessage.value = null
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {

        val stream =
            ByteArrayOutputStream()

        compress(
            Bitmap.CompressFormat.JPEG,
            90,
            stream
        )

        val byteArray =
            stream.toByteArray()

        val requestBody =
            byteArray.toRequestBody(
                "image/jpeg".toMediaType(),
                0,
                byteArray.size
            )

        return MultipartBody.Part.createFormData(
            "file",
            "image.jpg",
            requestBody
        )
    }
}