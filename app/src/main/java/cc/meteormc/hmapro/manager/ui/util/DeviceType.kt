// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package cc.meteormc.hmapro.manager.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalWindowInfo

@Composable
fun currentDeviceType(): DeviceType {
    val windowSize = LocalWindowInfo.current.containerSize
    return if (windowSize.width < windowSize.height) {
        DeviceType.PHONE
    } else {
        DeviceType.PAD
    }
}

enum class DeviceType {
    PHONE, PAD
}