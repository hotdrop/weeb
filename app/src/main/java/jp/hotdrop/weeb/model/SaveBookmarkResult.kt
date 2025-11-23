package jp.hotdrop.weeb.model

sealed interface SaveBookmarkResult {
    data object Success : SaveBookmarkResult
    data class Error(val error: SaveBookmarkError) : SaveBookmarkResult
}

sealed interface SaveBookmarkError {
    object DuplicateBookMarkError: SaveBookmarkError
    data class DataBaseError(val throwable: Throwable): SaveBookmarkError
}
