package com.example.smartledger.presentation.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 基础顶部栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    backgroundColor: Color = AppColors.Background,
    contentColor: Color = AppColors.TextPrimary
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = AppTypography.TitleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "导航",
                        tint = contentColor
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        scrollBehavior = scrollBehavior,
        windowInsets = WindowInsets.statusBars
    )
}

/**
 * 带返回按钮的顶部栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBarWithBack(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    backgroundColor: Color = AppColors.Background,
    contentColor: Color = AppColors.TextPrimary
) {
    AppTopBar(
        title = title,
        modifier = modifier,
        navigationIcon = Icons.Filled.ArrowBack,
        onNavigationClick = onBackClick,
        actions = actions,
        scrollBehavior = scrollBehavior,
        backgroundColor = backgroundColor,
        contentColor = contentColor
    )
}

/**
 * 带关闭按钮的顶部栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBarWithClose(
    title: String,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    backgroundColor: Color = AppColors.Background,
    contentColor: Color = AppColors.TextPrimary
) {
    AppTopBar(
        title = title,
        modifier = modifier,
        navigationIcon = Icons.Default.Close,
        onNavigationClick = onCloseClick,
        actions = actions,
        scrollBehavior = scrollBehavior,
        backgroundColor = backgroundColor,
        contentColor = contentColor
    )
}

/**
 * 居中标题顶部栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenteredTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    backgroundColor: Color = AppColors.Background,
    contentColor: Color = AppColors.TextPrimary
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = AppTypography.TitleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "导航",
                        tint = contentColor
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        scrollBehavior = scrollBehavior,
        windowInsets = WindowInsets.statusBars
    )
}

/**
 * 大标题顶部栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeAppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    backgroundColor: Color = AppColors.Background,
    contentColor: Color = AppColors.TextPrimary
) {
    LargeTopAppBar(
        title = {
            Text(
                text = title,
                style = AppTypography.TitleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "导航",
                        tint = contentColor
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        scrollBehavior = scrollBehavior,
        windowInsets = WindowInsets.statusBars
    )
}

/**
 * 透明顶部栏 - 用于有背景图的页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransparentTopBar(
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = Icons.Filled.ArrowBack,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    contentColor: Color = Color.White
) {
    TopAppBar(
        title = { },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "导航",
                        tint = contentColor
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        windowInsets = WindowInsets.statusBars
    )
}

/**
 * 通用操作按钮
 */
@Composable
fun TopBarActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String? = null,
    tint: Color = AppColors.TextPrimary
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

/**
 * 更多操作按钮
 */
@Composable
fun TopBarMoreButton(
    onClick: () -> Unit,
    tint: Color = AppColors.TextPrimary
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "更多",
            tint = tint
        )
    }
}
