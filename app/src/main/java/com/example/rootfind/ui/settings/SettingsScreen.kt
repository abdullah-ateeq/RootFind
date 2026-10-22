package com.example.rootfind.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rootfind.model.AppSettings
import com.example.rootfind.model.AppTheme
import com.example.rootfind.model.CalculationRoundingMode
import com.example.rootfind.ui.home.RootFindUiState
import com.example.rootfind.ui.home.RootFindViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: RootFindUiState,
    viewModel: RootFindViewModel,
    onNavigateBack: () -> Unit
) {
    val settings = uiState.settings

    // Clear History Dialog
    if (uiState.showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showClearHistoryDialog(false) },
            title = { Text("Clear All History?") },
            text = { Text("Are you sure you want to clear all saved calculations?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmClearHistory() }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showClearHistoryDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Settings Dialog
    if (uiState.showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showResetSettingsDialog(false) },
            title = { Text("Reset All Settings?") },
            text = { Text("Reset all preferences to default values? Your calculation history will not be affected.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmResetSettings() }) {
                    Text("Reset", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showResetSettingsDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics { contentDescription = "Navigate back" }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // APPEARANCE SECTION
            SettingsSectionHeader(title = "APPEARANCE")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Theme Selector
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppTheme.entries.forEach { themeOption ->
                            val isSelected = settings.theme == themeOption
                            OutlinedButton(
                                onClick = { viewModel.updateSettings(settings.copy(theme = themeOption)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = if (isSelected) {
                                    androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                } else {
                                    androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                                }
                            ) {
                                Text(
                                    text = when (themeOption) {
                                        AppTheme.SYSTEM -> "System"
                                        AppTheme.LIGHT -> "Light"
                                        AppTheme.DARK -> "Dark"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Dynamic Color
                    SettingsSwitchRow(
                        title = "Dynamic Color",
                        subtitle = "Use Android 12+ wallpaper color palette",
                        checked = settings.dynamicColor,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(dynamicColor = it)) }
                    )
                }
            }

            // BEHAVIOR SECTION
            SettingsSectionHeader(title = "BEHAVIOR")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingsSwitchRow(
                        title = "Haptic Feedback",
                        subtitle = "Tactile feedback for button taps and actions",
                        checked = settings.hapticFeedback,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(hapticFeedback = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    SettingsSwitchRow(
                        title = "Animations",
                        subtitle = "Enable smooth UI transitions and cards",
                        checked = settings.animationsEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(animationsEnabled = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    SettingsSwitchRow(
                        title = "Confirm Delete",
                        subtitle = "Show confirmation dialog before deleting history",
                        checked = settings.confirmDelete,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(confirmDelete = it)) }
                    )
                }
            }

            // CALCULATION SECTION
            SettingsSectionHeader(title = "CALCULATION")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Decimal places dropdown
                    var decimalDropdownExpanded by remember { mutableStateOf(false) }
                    Text(
                        text = "Displayed Decimal Places",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Internal calculation remains at 80 digits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = decimalDropdownExpanded,
                        onExpandedChange = { decimalDropdownExpanded = !decimalDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${settings.decimalPlaces} decimal places",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = decimalDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = decimalDropdownExpanded,
                            onDismissRequest = { decimalDropdownExpanded = false }
                        ) {
                            AppSettings.ALLOWED_DECIMAL_PLACES.forEach { places ->
                                DropdownMenuItem(
                                    text = { Text("$places decimal places") },
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(decimalPlaces = places))
                                        decimalDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Rounding mode dropdown
                    var roundingDropdownExpanded by remember { mutableStateOf(false) }
                    Text(
                        text = "Rounding Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Applies only to approximate decimal displays.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = roundingDropdownExpanded,
                        onExpandedChange = { roundingDropdownExpanded = !roundingDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = settings.roundingMode.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roundingDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = roundingDropdownExpanded,
                            onDismissRequest = { roundingDropdownExpanded = false }
                        ) {
                            CalculationRoundingMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.displayName) },
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(roundingMode = mode))
                                        roundingDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // DATA SECTION
            SettingsSectionHeader(title = "DATA")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.showClearHistoryDialog(true) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Clear History", color = MaterialTheme.colorScheme.error)
                    }

                    OutlinedButton(
                        onClick = { viewModel.showResetSettingsDialog(true) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reset Settings")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp
    )
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
