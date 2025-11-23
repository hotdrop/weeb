package jp.hotdrop.weeb.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import jp.hotdrop.weeb.model.BookMarkCategory

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
) {
    fun toModel(): BookMarkCategory = BookMarkCategory(id = id, name = name)
}
