package jp.hotdrop.weeb.ui.main

import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.weeb.data.repository.BookmarkRepository
import jp.hotdrop.weeb.model.BookMarkCategory
import jp.hotdrop.weeb.model.SaveBookmarkError
import jp.hotdrop.weeb.model.SaveBookmarkResult
import jp.hotdrop.weeb.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import java.net.URLEncoder
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<MainEffect>()
    val effects: SharedFlow<MainEffect> = _effects.asSharedFlow()

    private var hasLoadedHome = false

    init {
        launch {
            bookmarkRepository.observeCategories().collectLatest { categories ->
                val dialog = _uiState.value.bookmarkDialog
                val selectedId = dialog.selectedCategoryId ?: categories.firstOrNull()?.id
                _uiState.emit(
                    _uiState.value.copy(
                        categories = categories,
                        bookmarkDialog = dialog.copy(selectedCategoryId = selectedId)
                    )
                )
            }
        }
        launch {
            _effects.emit(MainEffect.LoadUrl(HOME_URL))
            hasLoadedHome = true
        }
    }

    fun updateAddressInput(text: String) {
        _uiState.value = _uiState.value.copy(addressText = text)
    }

    fun submitAddress() {
        val target = buildUrlFromInput(_uiState.value.addressText)
        _uiState.value = _uiState.value.copy(addressText = target)
        launch {
            _effects.emit(MainEffect.LoadUrl(target))
        }
    }

    fun loadHome() {
        _uiState.value = _uiState.value.copy(addressText = HOME_URL)
        launch {
            _effects.emit(MainEffect.LoadUrl(HOME_URL))
        }
    }

    fun onPageUpdated(url: String, title: String?, canGoBack: Boolean) {
        _uiState.value = _uiState.value.copy(
            currentUrl = url,
            currentTitle = title.orEmpty(),
            canGoBack = canGoBack,
            addressText = url
        )
    }

    fun onTogglePcMode() {
        val newValue = !_uiState.value.isPcMode
        _uiState.value = _uiState.value.copy(isPcMode = newValue)
    }

    fun openBookmarkDialog() {
        val currentUrl = _uiState.value.currentUrl
        if (currentUrl.isBlank()) {
            return
        }
        launch {
            val isRegistered = dispatcherIO { bookmarkRepository.isBookmarkRegistered(currentUrl) }
            val defaultCategory = _uiState.value.categories.firstOrNull()?.id
            _uiState.emit(
                _uiState.value.copy(
                    bookmarkDialog = BookmarkDialogState(
                        isVisible = true,
                        titleInput = _uiState.value.currentTitle.ifBlank { currentUrl },
                        url = currentUrl,
                        selectedCategoryId = defaultCategory,
                        message = if (isRegistered) "すでに登録されています" else null
                    )
                )
            )
        }
    }

    fun closeBookmarkDialog() {
        _uiState.value = _uiState.value.copy(
            bookmarkDialog = BookmarkDialogState()
        )
    }

    fun updateBookmarkTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            bookmarkDialog = _uiState.value.bookmarkDialog.copy(titleInput = title)
        )
    }

    fun selectCategory(categoryId: Long) {
        _uiState.value = _uiState.value.copy(
            bookmarkDialog = _uiState.value.bookmarkDialog.copy(selectedCategoryId = categoryId)
        )
    }

    fun createCategory(name: String) {
        if (name.isBlank()) return
        launch {
            val id = dispatcherIO { bookmarkRepository.createCategory(name) }
            _uiState.emit(
                _uiState.value.copy(
                    bookmarkDialog = _uiState.value.bookmarkDialog.copy(selectedCategoryId = id)
                )
            )
        }
    }

    fun saveBookmark() {
        val dialog = _uiState.value.bookmarkDialog
        val categoryId = dialog.selectedCategoryId ?: return
        val title = dialog.titleInput.ifBlank { dialog.url }
        if (dialog.url.isBlank()) return

        _uiState.value = _uiState.value.copy(
            bookmarkDialog = dialog.copy(isSaving = true, message = null)
        )

        launch {
            when (val result = dispatcherIO { bookmarkRepository.saveBookmark(title, dialog.url, categoryId) }) {
                SaveBookmarkResult.Success -> _uiState.emit(_uiState.value.copy(bookmarkDialog = BookmarkDialogState()))
                is SaveBookmarkResult.Error -> {
                    when (result.error) {
                        is SaveBookmarkError.DataBaseError -> _uiState.emit(
                            _uiState.value.copy(
                                bookmarkDialog = dialog.copy(
                                    isSaving = false,
                                    message = "保存に失敗しました"
                                )
                            )
                        )
                        SaveBookmarkError.DuplicateBookMarkError -> _uiState.emit(
                        _uiState.value.copy(
                                bookmarkDialog = dialog.copy(
                                    isSaving = false,
                                    message = "すでに登録されています"
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    fun loadBookmark(url: String) {
        _uiState.value = _uiState.value.copy(addressText = url)
        launch {
            _effects.emit(MainEffect.LoadUrl(url))
        }
    }

    private fun buildUrlFromInput(input: String): String {
        val trimmed = input.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
        val looksLikeUrl = trimmed.contains(".") && !trimmed.contains(" ")
        return if (looksLikeUrl) {
            "https://$trimmed"
        } else {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            "https://www.google.com/search?q=$encoded"
        }
    }
}

private val HOME_URL = "https://www.google.com/"

data class BookmarkDialogState(
    val isVisible: Boolean = false,
    val titleInput: String = "",
    val url: String = "",
    val selectedCategoryId: Long? = null,
    val message: String? = null,
    val isSaving: Boolean = false
)

data class MainUiState(
    val addressText: String = HOME_URL,
    val currentUrl: String = HOME_URL,
    val currentTitle: String = "",
    val canGoBack: Boolean = false,
    val isPcMode: Boolean = true,
    val categories: List<BookMarkCategory> = emptyList(),
    val bookmarkDialog: BookmarkDialogState = BookmarkDialogState()
)

sealed class MainEffect {
    data class LoadUrl(val url: String, val reload: Boolean = false) : MainEffect()
    data object Reload : MainEffect()
}