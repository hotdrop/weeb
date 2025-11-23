package jp.hotdrop.weeb.data.repository

import android.database.sqlite.SQLiteConstraintException
import jp.hotdrop.weeb.data.dao.BookmarkDao
import jp.hotdrop.weeb.data.dao.CategoryDao
import jp.hotdrop.weeb.data.entity.BookmarkEntity
import jp.hotdrop.weeb.data.entity.CategoryEntity
import jp.hotdrop.weeb.model.AppComplete
import jp.hotdrop.weeb.model.AppError
import jp.hotdrop.weeb.model.AppResult
import jp.hotdrop.weeb.model.Bookmark
import jp.hotdrop.weeb.model.BookMarkCategory
import jp.hotdrop.weeb.model.CategoryWithBookmarks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val bookmarkDao: BookmarkDao
) {
    fun observeCategories(): Flow<List<BookMarkCategory>> = flow {
        ensureDefaultCategory()
        emitAll(
            categoryDao.observeAll().map { entities ->
                val ensured = entities.ifEmpty { ensureDefaultCategory() }
                ensured.map { it.toModel() }
            }
        )
    }

    fun observeCategoriesWithBookmarks(): Flow<List<CategoryWithBookmarks>> = flow {
        ensureDefaultCategory()
        emitAll(
            categoryDao.observeAll().map { categories ->
                val ensured = categories.ifEmpty { ensureDefaultCategory() }
                bookmarkDao.findAllCategoriesWithBookmarks(ensured).map { it.toModel() }
            }
        )
    }

    suspend fun getCategories(): AppResult<List<BookMarkCategory>> {
        val categories = ensureDefaultCategory().map { it.toModel() }
        return AppResult.Success(categories)
    }

    suspend fun createCategory(name: String): AppResult<Long> {
        val id = categoryDao.insert(CategoryEntity(name = name)).takeIf { it > 0L } ?: findExistingIdByName(name)
        return AppResult.Success(id)
    }

    suspend fun renameCategory(id: Long, name: String) {
        val current = categoryDao.findById(id) ?: return
        categoryDao.update(current.copy(name = name))
    }

    suspend fun deleteCategory(id: Long) {
        categoryDao.findById(id)?.let { categoryDao.delete(it) }
        if (categoryDao.findAll().isEmpty()) {
            categoryDao.insert(CategoryEntity(name = BookMarkCategory.DEFAULT_NAME))
        }
    }

    suspend fun saveBookmark(title: String, url: String, categoryId: Long): AppComplete {
        ensureDefaultCategory()
        return try {
            bookmarkDao.insert(
                BookmarkEntity(
                    title = title,
                    url = url,
                    categoryId = categoryId
                )
            )
            AppComplete.Complete
        } catch (e: SQLiteConstraintException) {
            AppComplete.Error(AppError.DuplicateBookMarkError)
        } catch (t: Throwable) {
            AppComplete.Error(AppError.DataBaseError(t))
        }
    }


    suspend fun updateBookmark(id: Long, title: String, categoryId: Long) {
        bookmarkDao.findById(id)?.let { entity ->
            bookmarkDao.update(entity.copy(title = title, categoryId = categoryId))
        }
    }

    suspend fun deleteBookmark(id: Long) {
        bookmarkDao.findById(id)?.let { bookmarkDao.delete(it) }
    }

    suspend fun isBookmarkRegistered(url: String): AppResult<Boolean> {
        val result = bookmarkDao.findByUrl(url) != null
        return AppResult.Success(result)
    }

    suspend fun getBookmarkById(id: Long): AppResult<Bookmark?> {
        val result = bookmarkDao.findById(id)?.toModel()
        return AppResult.Success(result)
    }

    private suspend fun ensureDefaultCategory(): List<CategoryEntity> {
        val categories = categoryDao.findAll()
        if (categories.isEmpty()) {
            val id = categoryDao.insert(CategoryEntity(name = BookMarkCategory.DEFAULT_NAME))
            val created = if (id == -1L || id == 0L) {
                categoryDao.findAll()
            } else {
                listOf(CategoryEntity(id = id, name = BookMarkCategory.DEFAULT_NAME))
            }
            return created
        }
        return categories
    }

    private suspend fun findExistingIdByName(name: String): Long {
        return categoryDao.findAll().firstOrNull { it.name == name }?.id ?: 0L
    }
}
