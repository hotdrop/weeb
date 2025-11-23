package jp.hotdrop.weeb.model

data class Bookmark(
    val id: Long,
    val categoryId: Long,
    val title: String,
    val url: String
)
