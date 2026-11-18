// Copyright 2025, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package cc.meteormc.hmapro.manager.ui.weight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBarDefaults
import top.yukonga.miuix.kmp.basic.TopAppBarLayout
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * A [BlurTopAppBar] whose large title blurs as it collapses.
 *
 * @param title The title of the [BlurTopAppBar].
 * @param blurRadius How far the large title blurs out as it collapses. It reaches this
 *   radius at the point the title has fully faded.
 * @param modifier The modifier to be applied to the [BlurTopAppBar].
 * @param expandable Whether the large title can expand. `false` keeps the compact title visible.
 * @param color The background color of the [BlurTopAppBar].
 * @param titleColor The color of the collapsed small title text.
 * @param largeTitle The large title of the [BlurTopAppBar].
 * @param largeTitleColor The color of the expanded large title text.
 * @param subtitle The subtitle displayed below the title bar area.
 * @param subtitleColor The color of the subtitle text.
 * @param navigationIcon The content that represents the navigation icon.
 * @param actions The content that represents the action icons.
 * @param scrollBehavior The behavior that controls the [BlurTopAppBar].
 * @param defaultWindowInsetsPadding Whether to apply default window insets padding.
 * @param titlePadding The horizontal padding of the title and large title.
 * @param navigationIconPadding The start padding of the navigation icon.
 * @param actionIconPadding The end padding of the action icons.
 * @param titleAlpha Draw-phase opacity applied to both title containers.
 * @param bottomContent Content displayed below the title bar area.
 */
@Composable
fun BlurTopAppBar(
    title: String,
    blurRadius: Dp,
    modifier: Modifier = Modifier,
    expandable: Boolean = true,
    color: Color = MiuixTheme.colorScheme.surface,
    titleColor: Color = MiuixTheme.colorScheme.onSurface,
    largeTitle: String = title,
    largeTitleColor: Color = MiuixTheme.colorScheme.onSurface,
    subtitle: String = "",
    subtitleColor: Color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: ScrollBehavior? = null,
    defaultWindowInsetsPadding: Boolean = true,
    titlePadding: Dp = TopAppBarDefaults.TitlePadding,
    navigationIconPadding: Dp = TopAppBarDefaults.NavigationIconPadding,
    actionIconPadding: Dp = TopAppBarDefaults.ActionIconPadding,
    titleAlpha: () -> Float = { 1f },
    bottomContent: @Composable () -> Unit = {},
) {
    // Wrap the given actions in a Row.
    val actionsRow =
        @Composable {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }

    // Compose a Surface with a TopAppBarLayout content.
    // The surface's background color is animated as specified above.
    // The height of the app bar is determined by subtracting the bar's height offset from the
    // app bar's defined constant height value (i.e. the ContainerHeight token).
    TopAppBarLayout(
        title = title,
        titleAlpha = titleAlpha,
        expandable = expandable,
        color = color,
        titleColor = titleColor,
        largeTitle = largeTitle,
        largeTitleColor = largeTitleColor,
        titleBlurRadius = blurRadius,
        subtitle = subtitle,
        subtitleColor = subtitleColor,
        navigationIcon = navigationIcon,
        actions = actionsRow,
        titlePadding = titlePadding,
        navigationIconPadding = navigationIconPadding,
        actionIconPadding = actionIconPadding,
        scrollBehavior = scrollBehavior,
        modifier = modifier,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
        bottomContent = bottomContent,
    )
}