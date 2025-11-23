package jp.hotdrop.weeb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import jp.hotdrop.weeb.model.Bookmark

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("category_id"),
        Index(value = ["url"], unique = true)
    ]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    val title: String,
    val url: String
) {
    fun toModel(): Bookmark = Bookmark(
        id = id,
        categoryId = categoryId,
        title = title,
        url = url
    )
}
