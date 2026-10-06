package com.personalvault.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personalvault.domain.model.DeviceStatus
import com.personalvault.ui.theme.StatusOfflineRed
import com.personalvault.ui.theme.StatusOnlineGreen
import com.personalvault.ui.theme.StatusWarningAmber

@Composable
fun StatusBadge(
    status: DeviceStatus,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusText) = when (status) {
        DeviceStatus.ONLINE -> StatusOnlineGreen to "ONLINE"
        DeviceStatus.OFFLINE -> StatusOfflineRed to "OFFLINE"
        DeviceStatus.CONNECTING -> StatusWarningAmber to "CONNECTING"
        DeviceStatus.STANDBY -> StatusWarningAmber to "STANDBY"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(statusColor.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium ?: MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
