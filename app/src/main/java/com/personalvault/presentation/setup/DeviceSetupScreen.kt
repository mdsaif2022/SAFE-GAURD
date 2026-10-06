package com.personalvault.presentation.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Phonelink
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personalvault.domain.model.DeviceMode
import com.personalvault.presentation.components.VaultTopAppBar

@Composable
fun DeviceSetupScreen(
    viewModel: DeviceSetupViewModel,
    onNavigateToAgent: () -> Unit,
    onNavigateToController: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    if (uiState.showConfirmationDialog) {
        val pendingMode = uiState.pendingMode ?: uiState.selectedMode
        val isAgent = pendingMode == DeviceMode.AGENT

        val dialogTitle = if (isAgent) {
            "Set this phone as Agent Device?"
        } else {
            "Set this phone as Controller Device?"
        }

        val dialogText = if (isAgent) {
            "This phone will remain at home and serve as your secure personal Agent device."
        } else {
            "This phone will act as your remote Controller device to access your home Agent."
        }

        val titleContent: @Composable () -> Unit = {
            Text(
                text = dialogTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        val textContent: @Composable () -> Unit = {
            Text(
                text = dialogText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        val confirmContent: @Composable () -> Unit = {
            Button(
                onClick = {
                    viewModel.confirmRoleRegistration { mode ->
                        when (mode) {
                            DeviceMode.AGENT -> onNavigateToAgent()
                            DeviceMode.CONTROLLER -> onNavigateToController()
                            else -> onNavigateToAgent()
                        }
                    }
                }
            ) {
                Text(
                    text = "Confirm Role",
                    fontWeight = FontWeight.Bold
                )
            }
        }
        val dismissContent: @Composable () -> Unit = {
            OutlinedButton(onClick = viewModel::dismissConfirmationDialog) {
                Text(text = "Cancel")
            }
        }

        AlertDialog(
            onDismissRequest = viewModel::dismissConfirmationDialog,
            title = titleContent,
            text = textContent,
            confirmButton = confirmContent,
            dismissButton = dismissContent
        )
    }

    Scaffold(
        topBar = {
            VaultTopAppBar(
                title = "Device Role Setup"
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
                    .widthIn(max = 500.dp)
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Configure This Device",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Choose the operational role for this phone in your PersonalVault architecture:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (uiState.errorMessage != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Option 1: Agent Device
                    DeviceRoleCard(
                        title = "Agent Device",
                        description = "This phone stays at home and provides approved personal data and remote-access features.",
                        icon = Icons.Default.Dns,
                        isSelected = uiState.selectedMode == DeviceMode.AGENT,
                        onClick = {
                            viewModel.selectMode(DeviceMode.AGENT)
                            viewModel.requestConfirmation(DeviceMode.AGENT)
                        }
                    )

                    // Option 2: Controller Device
                    DeviceRoleCard(
                        title = "Controller Device",
                        description = "This phone is used remotely to access your registered personal device.",
                        icon = Icons.Default.Phonelink,
                        isSelected = uiState.selectedMode == DeviceMode.CONTROLLER,
                        onClick = {
                            viewModel.selectMode(DeviceMode.CONTROLLER)
                            viewModel.requestConfirmation(DeviceMode.CONTROLLER)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        Button(
                            onClick = {
                                viewModel.requestConfirmation(uiState.selectedMode)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = if (uiState.selectedMode == DeviceMode.AGENT) {
                                    "Continue as Agent Device"
                                } else {
                                    "Continue as Controller Device"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.RoleTextContent(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.weight(1f)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeviceRoleCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            RoleTextContent(
                title = title,
                description = description
            )

            Spacer(modifier = Modifier.width(8.dp))

            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
    }
}
