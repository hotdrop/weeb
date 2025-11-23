package jp.hotdrop.weeb

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import jp.hotdrop.weeb.ui.auth.BiometricAuthScreen
import jp.hotdrop.weeb.ui.auth.BiometricAuthViewModel
import jp.hotdrop.weeb.ui.bookmark.BookmarkScreen
import jp.hotdrop.weeb.ui.bookmark.BookmarkViewModel
import jp.hotdrop.weeb.ui.main.MainScreen
import jp.hotdrop.weeb.ui.main.MainViewModel
import jp.hotdrop.weeb.ui.navigation.AppDestination
import jp.hotdrop.weeb.ui.theme.WeebTheme

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeebTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = AppDestination.BiometricAuth.route
                ) {
                    composable(AppDestination.BiometricAuth.route) {
                        val viewModel: BiometricAuthViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                        BiometricAuthScreen(
                            state = uiState,
                            effects = viewModel.effects,
                            onRetryAuthentication = viewModel::requestAuthentication,
                            onAuthenticationSucceeded = viewModel::onAuthenticationSucceeded,
                            onAuthenticationFailed = viewModel::onAuthenticationFailed,
                            onAuthenticationError = viewModel::onAuthenticationError,
                            onNavigateToMain = {
                                navController.navigate(AppDestination.Main.route) {
                                    popUpTo(AppDestination.BiometricAuth.route) { inclusive = true }
                                }
                            },
                            onExitApp = { this@MainActivity.finish() }
                        )
                    }
                    composable(AppDestination.Main.route) { backStackEntry ->
                        val viewModel: MainViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        val bookmarkUrlState = backStackEntry.savedStateHandle.getStateFlow<String?>("selected_bookmark", null)
                        val bookmarkUrl by bookmarkUrlState.collectAsStateWithLifecycle()

                        LaunchedEffect(bookmarkUrl) {
                            bookmarkUrl?.let { url ->
                                viewModel.loadBookmark(url)
                                backStackEntry.savedStateHandle["selected_bookmark"] = null
                            }
                        }

                        MainScreen(
                            state = uiState,
                            effects = viewModel.effects,
                            onAddressChange = viewModel::updateAddressInput,
                            onSubmitAddress = viewModel::submitAddress,
                            onLoadHome = viewModel::loadHome,
                            onTogglePcMode = viewModel::onTogglePcMode,
                            onOpenBookmarkList = { navController.navigate(AppDestination.Bookmark.route) },
                            onOpenBookmarkDialog = viewModel::openBookmarkDialog,
                            onPageUpdated = viewModel::onPageUpdated,
                            onSelectCategory = viewModel::selectCategory,
                            onBookmarkTitleChange = viewModel::updateBookmarkTitle,
                            onSaveBookmark = viewModel::saveBookmark,
                            onCloseBookmarkDialog = viewModel::closeBookmarkDialog
                        )
                    }
                    composable(AppDestination.Bookmark.route) {
                        val viewModel: BookmarkViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                        BookmarkScreen(
                            state = uiState,
                            onBack = { navController.popBackStack() },
                            onBookmarkSelected = { url ->
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "selected_bookmark",
                                    url
                                )
                                navController.popBackStack()
                            },
                            onStartEditBookmark = viewModel::startEditBookmark,
                            onBookmarkTitleChange = viewModel::updateBookmarkTitle,
                            onBookmarkCategoryChange = viewModel::selectBookmarkCategory,
                            onSaveBookmarkEdit = viewModel::saveBookmarkEdit,
                            onDeleteBookmark = viewModel::deleteBookmark,
                            onStartEditCategory = viewModel::startEditCategory,
                            onCategoryNameChange = viewModel::updateCategoryName,
                            onSaveCategoryEdit = viewModel::saveCategoryEdit,
                            onDeleteCategory = viewModel::deleteCategory,
                            onCreateCategory = viewModel::createCategory,
                            onNewCategoryNameChange = viewModel::updateNewCategoryName,
                            newCategoryName = uiState.newCategoryName,
                            categoriesForSelection = uiState.categories.map { it.bookMarkCategory },
                            onDismissDialog = viewModel::dismissDialogs,
                            onToggleCategoryExpanded = viewModel::toggleCategoryExpanded
                        )
                    }
                }
            }
        }
    }
}
