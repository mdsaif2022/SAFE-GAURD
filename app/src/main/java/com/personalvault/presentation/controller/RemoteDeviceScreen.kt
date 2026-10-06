package com.personalvault.presentation.controller

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personalvault.presentation.components.StatusBadge
import com.personalvault.presentation.components.VaultCard
import com.personalvault.presentation.components.VaultTopAppBar

data class RemoteFeatureSpec(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun RemoteDeviceScreen(
    viewModel: ControllerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val filesFeatures = listOf(
        RemoteFeatureSpec("Photos", "Remote image vault", Icons.Default.Image),
        RemoteFeatureSpec("Videos", "Video stream & recordings", Icons.Default.Videocam),
        RemoteFeatureSpec("Documents", "Encrypted files", Icons.Default.Description),
        RemoteFeatureSpec("Audio", "Voice notes & audio", Icons.Default.Audiotrack)
    )

    val personalDataFeatures = listOf(
        RemoteFeatureSpec("Contacts", "Phonebook sync", Icons.Default.Contacts),
        RemoteFeatureSpec("SMS", "Text message log", Icons.Default.Sms),
        RemoteFeatureSpec("Notifications", "System alerts", Icons.Default.Notifications),
        RemoteFeatureSpec("Location", "GPS position track", Icons.Default.LocationOn)
    )

    val liveSessionsFeatures = listOf(
        RemoteFeatureSpec("Camera", "Remote lens feed", Icons.Default.CameraAlt),
        RemoteFeatureSpec("Screen Share", "Live mirror feed", Icons.AutoMirrored.Filled.ScreenShare)
    )

    if (uiState.selectedFeature != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissFeatureDialog,
            title = {
                Text(
                    text = "${uiState.selectedFeature} (UI Placeholder)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Remote access for ${uiState.selectedFeature} is represented as a UI placeholder in this phase. Real peer-to-peer data transport is not initialized.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::dismissFeatureDialog) {
                    Text("Got It", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            VaultTopAppBar(
                title = "Remote Agent Access",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header Device Summary
                val agent = uiState.agentDevice
                if (agent != null) {
                    VaultCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = agent.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Device ID: ${agent.id}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${agent.networkType} • ${agent.batteryLevel}% Battery",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            StatusBadge(status = agent.status)
                        }
                    }
                }

                // Section 1: Files
                FeatureSectionHeader(title = "Files")
                FeatureGrid(
                    items = filesFeatures,
                    onItemClick = { viewModel.onSelectFeature(it.title) }
                )

                // Section 2: Personal Data
                FeatureSectionHeader(title = "Personal Data")
                FeatureGrid(
                    items = personalDataFeatures,
                    onItemClick = { viewModel.onSelectFeature(it.title) }
                )

                // Section 3: Live Sessions
                FeatureSectionHeader(title = "Live Sessions")
                FeatureGrid(
                    items = liveSessionsFeatures,
                    onItemClick = { viewModel.onSelectFeature(it.title) }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun FeatureSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun FeatureGrid(
    items: List<RemoteFeatureSpec>,
    onItemClick: (RemoteFeatureSpec) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { rowSpecs ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowSpecs.forEach { spec ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onItemClick(spec) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Icon(
                                        imageVector = spec.icon,
                                        contentDescription = spec.title,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxSize()
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Placeholder",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = spec.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = spec.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                if (rowSpecs.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
