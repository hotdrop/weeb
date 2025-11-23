package jp.hotdrop.weeb.ui.bookmark

import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.weeb.data.repository.BookmarkRepository
import jp.hotdrop.weeb.model.Bookmark
import jp.hotdrop.weeb.model.BookMarkCategory
import jp.hotdrop.weeb.model.CategoryWithBookmarks
import jp.hotdrop.weeb.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(BookmarkUiState())
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()

    init {
        launch {
            bookmarkRepository.observeCategoriesWithBookmarks().collectLatest { categories ->
                val previousCategories = _uiState.value.categories.associateBy { it.bookMarkCategory.id }
                val categoryIds = categories.map { it.bookMarkCategory.id }.toSet()
                val retainedExpanded = _uiState.value.expandedCategoryIds.filter { it in categoryIds }.toSet()
                val newlyAddedExpanded = categoryIds - previousCategories.keys
                _uiState.emit(
                    _uiState.value.copy(
                        categories = categories,
                        expandedCategoryIds = retainedExpanded + newlyAddedExpanded
                    )
                )
            }
        }
    }

    fun updateNewCategoryName(name: String) {
        _uiState.value = _uiState.value.copy(newCategoryName = name)
    }

    fun createCategory() {
        val name = _uiState.value.newCategoryName.trim()
        if (name.isBlank()) return
        launch {
            dispatcherIO { bookmarkRepository.createCategory(name) }
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
        launch {
            dispatcherIO { bookmarkRepository.renameCategory(editState.id, name) }
            _uiState.emit(_uiState.value.copy(categoryEditState = null))
        }
    }

    fun deleteCategory(bookMarkCategory: BookMarkCategory) {
        launch {
            dispatcherIO { bookmarkRepository.deleteCategory(bookMarkCategory.id) }
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
        launch {
            dispatcherIO {
                bookmarkRepository.updateBookmark(
                    id = editState.id,
                    title = title,
                    categoryId = editState.categoryId
                )
            }
            _uiState.emit(_uiState.value.copy(bookmarkEditState = null))
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        launch {
            dispatcherIO { bookmarkRepository.deleteBookmark(bookmark.id) }
        }
    }

    fun dismissDialogs() {
        _uiState.value = _uiState.value.copy(
            bookmarkEditState = null,
            categoryEditState = null
        )
    }

    fun toggleCategoryExpanded(categoryId: Long) {
        val updatedExpanded = if (_uiState.value.expandedCategoryIds.contains(categoryId)) {
            _uiState.value.expandedCategoryIds - categoryId
        } else {
            _uiState.value.expandedCategoryIds + categoryId
        }
        _uiState.value = _uiState.value.copy(expandedCategoryIds = updatedExpanded)
    }
}

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
    val message: String? = null,
    val expandedCategoryIds: Set<Long> = emptySet()
)
