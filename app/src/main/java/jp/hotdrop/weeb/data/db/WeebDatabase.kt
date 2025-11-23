package jp.hotdrop.weeb.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import jp.hotdrop.weeb.data.dao.BookmarkDao
import jp.hotdrop.weeb.data.dao.CategoryDao
import jp.hotdrop.weeb.data.entity.BookmarkEntity
import jp.hotdrop.weeb.data.entity.CategoryEntity

@Database(
    entities = [
        CategoryEntity::class,
        BookmarkEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WeebDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun bookmarkDao(): BookmarkDao
}
