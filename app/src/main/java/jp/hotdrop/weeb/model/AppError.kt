package jp.hotdrop.weeb.model

sealed interface AppError {
    object DuplicateBookMarkError: AppError
    data class DataBaseError(val throwable: Throwable): AppError
    data class UnknownError(val message: String): AppError
}
