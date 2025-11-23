package jp.hotdrop.weeb.data.entity

import jp.hotdrop.weeb.model.CategoryWithBookmarks

data class CategoryWithBookmarkEntityModel(
    val category: CategoryEntity,
    val bookmarks: List<BookmarkEntity>
) {
    fun toModel(): CategoryWithBookmarks {
        return CategoryWithBookmarks(
            bookMarkCategory = category.toModel(),
            bookmarks = bookmarks.map { it.toModel() }
        )
    }
}