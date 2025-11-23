package jp.hotdrop.weeb.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.hotdrop.weeb.data.dao.BookmarkDao
import jp.hotdrop.weeb.data.dao.CategoryDao
import jp.hotdrop.weeb.data.db.WeebDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WeebDatabase {
        return Room.databaseBuilder(
            context,
            WeebDatabase::class.java,
            "weeb.db"
        ).build()
    }

    @Provides
    fun provideCategoryDao(database: WeebDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideBookmarkDao(database: WeebDatabase): BookmarkDao = database.bookmarkDao()
}
