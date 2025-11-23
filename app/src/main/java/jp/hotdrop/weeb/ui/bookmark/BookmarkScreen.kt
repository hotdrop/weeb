package jp.hotdrop.weeb.ui.bookmark

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jp.hotdrop.weeb.model.Bookmark
import jp.hotdrop.weeb.model.BookMarkCategory
import jp.hotdrop.weeb.model.CategoryWithBookmarks

@Composable
fun BookmarkScreen(
    state: BookmarkUiState,
    onBack: () -> Unit,
    onBookmarkSelected: (String) -> Unit,
    onStartEditBookmark: (Bookmark) -> Unit,
    onBookmarkTitleChange: (String) -> Unit,
    onBookmarkCategoryChange: (Long) -> Unit,
    onSaveBookmarkEdit: () -> Unit,
    onDeleteBookmark: (Bookmark) -> Unit,
    onStartEditCategory: (BookMarkCategory) -> Unit,
    onCategoryNameChange: (String) -> Unit,
    onSaveCategoryEdit: () -> Unit,
    onDeleteCategory: (BookMarkCategory) -> Unit,
    onCreateCategory: () -> Unit,
    onNewCategoryNameChange: (String) -> Unit,
    newCategoryName: String,
    categoriesForSelection: List<BookMarkCategory>,
    onDismissDialog: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ブックマーク") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = onNewCategoryNameChange,
                    label = { Text(text = "カテゴリ追加") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = onCreateCategory,
                    enabled = newCategoryName.isNotBlank()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "追加")
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.categories) { categoryWithBookmarks ->
                    CategorySection(
                        categoryWithBookmarks = categoryWithBookmarks,
                        onBookmarkSelected = onBookmarkSelected,
                        onEditBookmark = onStartEditBookmark,
                        onDeleteBookmark = onDeleteBookmark,
                        onEditCategory = onStartEditCategory,
                        onDeleteCategory = onDeleteCategory
                    )
                }
            }
        }
    }

    state.bookmarkEditState?.let { editState ->
        BookmarkEditDialog(
            editState = editState,
            categories = categoriesForSelection,
            onTitleChange = onBookmarkTitleChange,
            onCategoryChange = onBookmarkCategoryChange,
            onSave = onSaveBookmarkEdit,
            onDismiss = onDismissDialog
        )
    }

    state.categoryEditState?.let { editState ->
        CategoryEditDialog(
            editState = editState,
            onNameChange = onCategoryNameChange,
            onSave = onSaveCategoryEdit,
            onDismiss = onDismissDialog
        )
    }
}

@Composable
private fun CategorySection(
    categoryWithBookmarks: CategoryWithBookmarks,
    onBookmarkSelected: (String) -> Unit,
    onEditBookmark: (Bookmark) -> Unit,
    onDeleteBookmark: (Bookmark) -> Unit,
    onEditCategory: (BookMarkCategory) -> Unit,
    onDeleteCategory: (BookMarkCategory) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = categoryWithBookmarks.bookMarkCategory.name, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = { onEditCategory(categoryWithBookmarks.bookMarkCategory) }) {
                    Icon(Icons.Default.Edit, contentDescription = "カテゴリ編集")
                }
                IconButton(onClick = { onDeleteCategory(categoryWithBookmarks.bookMarkCategory) }) {
                    Icon(Icons.Default.Delete, contentDescription = "カテゴリ削除")
                }
            }
        }
        categoryWithBookmarks.bookmarks.forEach { bookmark ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBookmarkSelected(bookmark.url) }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = bookmark.title, fontWeight = FontWeight.Medium)
                    Text(text = bookmark.url)
                }
                Row {
                    IconButton(onClick = { onEditBookmark(bookmark) }) {
                        Icon(Icons.Default.Edit, contentDescription = "編集")
                    }
                    IconButton(onClick = { onDeleteBookmark(bookmark) }) {
                        Icon(Icons.Default.Delete, contentDescription = "削除")
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkEditDialog(
    editState: BookmarkEditState,
    categories: List<BookMarkCategory>,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (Long) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onSave) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "閉じる")
            }
        },
        title = { Text(text = "ブックマーク編集") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = editState.title,
                    onValueChange = onTitleChange,
                    label = { Text(text = "タイトル") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(text = "カテゴリ")
                categories.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = category.name, modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = editState.categoryId == category.id,
                            onClick = { onCategoryChange(category.id) }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun CategoryEditDialog(
    editState: CategoryEditState,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onSave) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "閉じる")
            }
        },
        title = { Text(text = "カテゴリ編集") },
        text = {
            OutlinedTextField(
                value = editState.name,
                onValueChange = onNameChange,
                label = { Text(text = "カテゴリ名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
