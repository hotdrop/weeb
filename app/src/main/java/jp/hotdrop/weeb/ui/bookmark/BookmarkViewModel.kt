package jp.hotdrop.weeb.ui.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.weeb.data.repository.BookmarkRepository
import jp.hotdrop.weeb.model.Bookmark
import jp.hotdrop.weeb.model.BookMarkCategory
import jp.hotdrop.weeb.model.CategoryWithBookmarks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookmarkEditState(
    val id: Long,
    val title: String,
    val url: String,
    val categoryId: Long
)

data class CategoryEditState(
    val id: Long,
    val name: String
)

data class BookmarkUiState(
    val categories: List<CategoryWithBookmarks> = emptyList(),
    val bookmarkEditState: BookmarkEditState? = null,
    val categoryEditState: CategoryEditState? = null,
    val newCategoryName: String = "",
    val message: String? = null
)

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarkUiState())
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            bookmarkRepository.observeCategoriesWithBookmarks().collectLatest { categories ->
                _uiState.emit(_uiState.value.copy(categories = categories))
            }
        }
    }

    fun updateNewCategoryName(name: String) {
        _uiState.value = _uiState.value.copy(newCategoryName = name)
    }

    fun createCategory() {
        val name = _uiState.value.newCategoryName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            bookmarkRepository.createCategory(name)
            _uiState.emit(_uiState.value.copy(newCategoryName = ""))
        }
    }

    fun startEditCategory(bookMarkCategory: BookMarkCategory) {
        _uiState.value = _uiState.value.copy(
            categoryEditState = CategoryEditState(id = bookMarkCategory.id, name = bookMarkCategory.name)
        )
    }

    fun updateCategoryName(name: String) {
        _uiState.value = _uiState.value.copy(
            categoryEditState = _uiState.value.categoryEditState?.copy(name = name)
        )
    }

    fun saveCategoryEdit() {
        val editState = _uiState.value.categoryEditState ?: return
        val name = editState.name.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            bookmarkRepository.renameCategory(editState.id, name)
            _uiState.emit(_uiState.value.copy(categoryEditState = null))
        }
    }

    fun deleteCategory(bookMarkCategory: BookMarkCategory) {
        viewModelScope.launch {
            bookmarkRepository.deleteCategory(bookMarkCategory.id)
        }
    }

    fun startEditBookmark(bookmark: Bookmark) {
        _uiState.value = _uiState.value.copy(
            bookmarkEditState = BookmarkEditState(
                id = bookmark.id,
                title = bookmark.title,
                url = bookmark.url,
                categoryId = bookmark.categoryId
            )
        )
    }

    fun updateBookmarkTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            bookmarkEditState = _uiState.value.bookmarkEditState?.copy(title = title)
        )
    }

    fun selectBookmarkCategory(categoryId: Long) {
        _uiState.value = _uiState.value.copy(
            bookmarkEditState = _uiState.value.bookmarkEditState?.copy(categoryId = categoryId)
        )
    }

    fun saveBookmarkEdit() {
        val editState = _uiState.value.bookmarkEditState ?: return
        val title = editState.title.ifBlank { editState.url }
        viewModelScope.launch {
            bookmarkRepository.updateBookmark(
                id = editState.id,
                title = title,
                categoryId = editState.categoryId
            )
            _uiState.emit(_uiState.value.copy(bookmarkEditState = null))
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            bookmarkRepository.deleteBookmark(bookmark.id)
        }
    }

    fun dismissDialogs() {
        _uiState.value = _uiState.value.copy(
            bookmarkEditState = null,
            categoryEditState = null
        )
    }
}
