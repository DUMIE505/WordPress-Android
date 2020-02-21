package org.wordpress.android.imageeditor.preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageLoadToFileState.ImageLoadToFileSuccessState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageLoadToFileState.ImageLoadToFileFailedState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageLoadToFileState.ImageStartLoadingToFileState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageUiState.ImageDataStartLoadingUiState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageUiState.ImageInHighResLoadFailedUiState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageUiState.ImageInHighResLoadSuccessUiState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageUiState.ImageInLowResLoadFailedUiState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageUiState.ImageInLowResLoadSuccessUiState

class PreviewImageViewModel : ViewModel() {
    private val _uiState: MutableLiveData<ImageUiState> = MutableLiveData()
    val uiState: LiveData<ImageUiState> = _uiState

    private val _loadIntoFile: MutableLiveData<ImageLoadToFileState> = MutableLiveData()
    val loadIntoFile: LiveData<ImageLoadToFileState> = _loadIntoFile

    fun onCreateView(loResImageUrl: String, hiResImageUrl: String) {
        updateUiState(
            ImageDataStartLoadingUiState(
                ImageData(loResImageUrl, hiResImageUrl)
            )
        )
    }

    fun onImageLoadSuccess(url: String) {
        val newState = when (val currentState = uiState.value) {
            is ImageDataStartLoadingUiState -> {
                if (url == currentState.imageData.lowResImageUrl) {
                    ImageInLowResLoadSuccessUiState
                } else {
                    ImageInHighResLoadSuccessUiState
                }
            }
            else -> ImageInHighResLoadSuccessUiState
        }

        if (newState == ImageInHighResLoadSuccessUiState) {
            updateLoadIntoFileState(ImageStartLoadingToFileState(url))
        }
        updateUiState(newState)
    }

    fun onImageLoadFailed(url: String) {
        val newState = when (val currentState = uiState.value) {
            is ImageDataStartLoadingUiState -> {
                val lowResImageUrl = currentState.imageData.lowResImageUrl
                if (url == lowResImageUrl) {
                    ImageInLowResLoadFailedUiState
                } else {
                    ImageInHighResLoadFailedUiState
                }
            }
            else -> ImageInHighResLoadFailedUiState
        }

        updateUiState(newState)
    }

    fun onLoadIntoFileSuccess(filePath: String) {
        updateLoadIntoFileState(ImageLoadToFileSuccessState(filePath))
    }

    fun onLoadIntoFileFailed() {
        updateLoadIntoFileState(ImageLoadToFileFailedState)
    }

    private fun updateUiState(uiState: ImageUiState) {
        _uiState.value = uiState
    }

    private fun updateLoadIntoFileState(fileState: ImageLoadToFileState) {
        _loadIntoFile.value = fileState
    }

    data class ImageData(val lowResImageUrl: String, val highResImageUrl: String)

    sealed class ImageUiState(
        val progressBarVisible: Boolean = false
    ) {
        data class ImageDataStartLoadingUiState(val imageData: ImageData) : ImageUiState(progressBarVisible = true)
        // Continue displaying progress bar on low res image load success
        object ImageInLowResLoadSuccessUiState : ImageUiState(progressBarVisible = true)
        object ImageInLowResLoadFailedUiState : ImageUiState(progressBarVisible = true)
        object ImageInHighResLoadSuccessUiState : ImageUiState(progressBarVisible = false)
        object ImageInHighResLoadFailedUiState : ImageUiState(progressBarVisible = false)
    }

    sealed class ImageLoadToFileState {
        data class ImageStartLoadingToFileState(val imageUrl: String) : ImageLoadToFileState()
        data class ImageLoadToFileSuccessState(val filePath: String) : ImageLoadToFileState()
        object ImageLoadToFileFailedState : ImageLoadToFileState()
    }
}
