package jp.hotdrop.weeb.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import jp.hotdrop.weeb.data.entity.BookmarkEntity
import jp.hotdrop.weeb.data.entity.CategoryEntity
import jp.hotdrop.weeb.data.entity.CategoryWithBookmarkEntityModel

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(bookmark: BookmarkEntity): Long

    @Update
    suspend fun update(bookmark: BookmarkEntity)

    @Delete
    suspend fun delete(bookmark: BookmarkEntity)

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    suspend fun findByUrl(url: String): BookmarkEntity?

    @Query("SELECT * FROM bookmarks WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): BookmarkEntity?

    @Query("SELECT * FROM bookmarks WHERE category_id = :categoryId ORDER BY id DESC")
    suspend fun findByCategoryId(categoryId: Long): List<BookmarkEntity>

    @Transaction
    suspend fun findAllCategoriesWithBookmarks(categories: List<CategoryEntity>): List<CategoryWithBookmarkEntityModel> {
        return categories.map { category ->
            CategoryWithBookmarkEntityModel(
                category = category,
                bookmarks = findByCategoryId(category.id)
            )
        }
    }

}
