package jp.hotdrop.weeb.model

data class BookMarkCategory(
    val id: Long,
    val name: String
) {
    companion object {
        const val DEFAULT_NAME = "未分類"
    }
}
