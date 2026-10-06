package com.personalvault.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personalvault.domain.model.DeviceMode
import com.personalvault.presentation.components.VaultCard
import com.personalvault.presentation.components.VaultTopAppBar
import com.personalvault.utils.update.DownloadStatus
import com.personalvault.utils.update.UpdateManager

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onRoleSwitched: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentVersionName = UpdateManager.getCurrentAppVersion(context).first

    Scaffold(
        topBar = {
            VaultTopAppBar(
                title = "Vault Settings",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Card
            VaultCard {
                Text(
                    text = "Personal Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val session = uiState.session
                Text(
                    text = "Holder: ${session?.fullName ?: "Personal Vault Owner"}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Email: ${session?.email ?: "owner@personalvault.private"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // App Information & Update System Card
            VaultCard {
                Text(
                    text = "App Information & Updates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Installed Version: $currentVersionName",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Source: GitHub Official Releases",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.checkForUpdates(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isCheckingUpdate,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isCheckingUpdate) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Checking for Updates...")
                    } else {
                        Text(text = "Check for Updates")
                    }
                }

                if (uiState.infoMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.infoMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Current Role Switcher Card
            VaultCard {
                Text(
                    text = "Device Role & Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val currentMode = uiState.session?.deviceMode ?: DeviceMode.AGENT
                Text(
                    text = "Current Mode: $currentMode",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.switchRole(DeviceMode.AGENT, onRoleSwitched)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Agent Mode")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.switchRole(DeviceMode.CONTROLLER, onRoleSwitched)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Controller Mode")
                    }
                }
            }

            // Privacy & Architecture Card
            VaultCard {
                Text(
                    text = "Privacy & Security Baseline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Zero Third-Party Analytics / Ads\n• Zero Cloud Telemetry\n• End-to-End Encrypted Session Keys\n• Private Peer-to-Peer Remote Access Architecture\n• User-Controlled Package Installation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Logout Action
            Button(
                onClick = {
                    viewModel.logout(onLoggedOut)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Sign Out of Personal Vault",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }

    // Update Dialog when a release update is available
    if (uiState.showUpdateDialog && uiState.updateInfo != null) {
        val updateInfo = uiState.updateInfo!!
        val downloadStatus = uiState.downloadStatus

        AlertDialog(
            onDismissRequest = {
                if (downloadStatus !is DownloadStatus.Downloading) {
                    viewModel.dismissUpdateDialog()
                }
            },
            title = {
                Text(
                    text = "New Update Available",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Version ${updateInfo.latestVersionName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (updateInfo.releaseDate.isNotEmpty()) {
                        Text(
                            text = "Released: ${updateInfo.releaseDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "What's New:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = updateInfo.releaseNotes.ifBlank { "No release notes provided." },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    when (downloadStatus) {
                        is DownloadStatus.Downloading -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            if (downloadStatus.progressPct >= 0) {
                                LinearProgressIndicator(
                                    progress = { downloadStatus.progressPct / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "Downloading update... ${downloadStatus.progressPct}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            } else {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                Text(
                                    text = "Downloading update...",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                        is DownloadStatus.Success -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Download complete! Starting installation...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        is DownloadStatus.Error -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = downloadStatus.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        else -> {}
                    }
                }
            },
            confirmButton = {
                when (downloadStatus) {
                    is DownloadStatus.Downloading -> {
                        // Button disabled while download in progress
                    }
                    is DownloadStatus.Success -> {
                        Button(
                            onClick = {
                                viewModel.installDownloadedApk(context, downloadStatus.apkFile)
                            }
                        ) {
                            Text("Install Now")
                        }
                    }
                    else -> {
                        Button(
                            onClick = {
                                viewModel.startApkDownload(context)
                            }
                        ) {
                            Text("Update Now")
                        }
                    }
                }
            },
            dismissButton = {
                if (downloadStatus !is DownloadStatus.Downloading) {
                    OutlinedButton(
                        onClick = {
                            viewModel.dismissUpdateDialog()
                        }
                    ) {
                        Text("Later")
                    }
                }
            }
        )
    }
}
