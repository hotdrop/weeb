package jp.hotdrop.weeb.model

data class CategoryWithBookmarks(
    val bookMarkCategory: BookMarkCategory,
    val bookmarks: List<Bookmark>
)
