package com.example.rootfind.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rootfind.domain.math.RootFindFormatter
import com.example.rootfind.model.RootType
import com.example.rootfind.ui.theme.ApproximateAmber
import com.example.rootfind.ui.theme.ApproximateAmberContainerDark
import com.example.rootfind.ui.theme.ApproximateAmberContainerLight
import com.example.rootfind.ui.theme.ExactGreen
import com.example.rootfind.ui.theme.ExactGreenContainerDark
import com.example.rootfind.ui.theme.ExactGreenContainerLight
import com.example.rootfind.ui.theme.NoRealRed
import com.example.rootfind.ui.theme.NoRealRedContainerDark
import com.example.rootfind.ui.theme.NoRealRedContainerLight
import com.example.rootfind.util.HapticFeedbackUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: RootFindUiState,
    viewModel: RootFindViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.userNotice) {
        uiState.userNotice?.let { notice ->
            snackbarHostState.showSnackbar(notice)
            viewModel.dismissNotice()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "RootFind",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Calculate square, cube, and nth roots.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.semantics { contentDescription = "View calculation history" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.semantics { contentDescription = "Settings" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = onNavigateToAbout,
                        modifier = Modifier.semantics { contentDescription = "About RootFind" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Segmented Selector [ SQUARE ] [ CUBE ] [ CUSTOM ]
            SegmentedRootTypeSelector(
                selectedType = uiState.rootType,
                onSelectType = { viewModel.setRootType(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mode Header and Subtitle
            ModeHeaderSection(rootType = uiState.rootType)

            Spacer(modifier = Modifier.height(16.dp))

            // VALUE Input
            Text(
                text = "VALUE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = uiState.valueInput,
                onValueChange = { viewModel.onValueChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Value to calculate the root of." },
                placeholder = {
                    Text(
                        when (uiState.rootType) {
                            RootType.SQUARE -> "e.g. 16"
                            RootType.CUBE -> "e.g. 125"
                            RootType.CUSTOM -> "e.g. 81"
                        }
                    )
                },
                trailingIcon = {
                    if (uiState.valueInput.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onValueChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear value input"
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (uiState.rootType == RootType.CUSTOM) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        viewModel.calculate()
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // CUSTOM ROOT INDEX INPUT (when in Custom mode)
            if (uiState.rootType == RootType.CUSTOM) {
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ROOT INDEX",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = uiState.rootIndexInput,
                    onValueChange = { viewModel.onRootIndexChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Root index." },
                    placeholder = { Text("e.g. 4") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            viewModel.calculate()
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick root buttons in Custom mode: [ √ Square ] [ ∛ Cube ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setQuickRoot(2) },
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Set root index to 2 for square root" },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("√ Square (2)", fontWeight = FontWeight.Medium)
                    }

                    OutlinedButton(
                        onClick = { viewModel.setQuickRoot(3) },
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Set root index to 3 for cube root" },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("∛ Cube (3)", fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Error display
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Button: CALCULATE ROOT or Calculating... [ CANCEL ]
            if (uiState.isCalculating) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Calculating…",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.cancelCalculation() },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("CANCEL")
                    }
                }
            } else {
                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.calculate()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .semantics { contentDescription = "Calculate root." },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "CALCULATE ROOT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Result Section with Subtle Animation
            AnimatedVisibility(
                visible = uiState.result != null,
                enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.95f, animationSpec = tween(250)),
                exit = fadeOut(tween(200))
            ) {
                uiState.result?.let { result ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Main Result Card
                        ResultCard(
                            rootType = uiState.rootType,
                            result = result,
                            displayedResult = uiState.displayedResult
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Root Type Summary Card
                        RootTypeSummaryCard(
                            rootType = uiState.rootType,
                            index = result.rootIndex,
                            isReal = result.isReal,
                            isExact = result.isExact
                        )

                        // Verification Card
                        if (result.verificationExpression != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            VerificationCard(
                                verificationText = result.verificationExpression,
                                isExact = result.isExact
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Expandable Calculation Steps
                        CalculationStepsCard(
                            steps = result.calculationSteps,
                            isExpanded = uiState.isCalculationExpanded,
                            onToggle = { viewModel.toggleCalculationExpanded() }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Actions: [ COPY ] [ SHARE ] [ SAVE ] [ CLEAR ]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Copy
                            OutlinedButton(
                                onClick = {
                                    val copyText = viewModel.getCopyText()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("RootFind Result", copyText)
                                    clipboard.setPrimaryClip(clip)
                                    HapticFeedbackUtil.performLightHaptic(context, uiState.settings.hapticFeedback)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Result copied to clipboard.")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { contentDescription = "Copy result to clipboard" },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("COPY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Share
                            OutlinedButton(
                                onClick = {
                                    val shareText = viewModel.getShareText()
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Root Result")
                                    context.startActivity(shareIntent)
                                    HapticFeedbackUtil.performLightHaptic(context, uiState.settings.hapticFeedback)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { contentDescription = "Share root result" },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SHARE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Save
                            OutlinedButton(
                                onClick = { viewModel.saveToHistory() },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { contentDescription = "Save calculation to history" },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SAVE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Clear
                            OutlinedButton(
                                onClick = { viewModel.clearInputs() },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { contentDescription = "Clear inputs and calculation" },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CLEAR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SegmentedRootTypeSelector(
    selectedType: RootType,
    onSelectType: (RootType) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RootType.entries.forEach { type ->
                val isSelected = type == selectedType
                val label = when (type) {
                    RootType.SQUARE -> "SQUARE"
                    RootType.CUBE -> "CUBE"
                    RootType.CUSTOM -> "CUSTOM"
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .clickable { onSelectType(type) }
                        .padding(vertical = 10.dp)
                        .semantics { contentDescription = "Select $label root mode" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ModeHeaderSection(rootType: RootType) {
    val (header, subtitle) = when (rootType) {
        RootType.SQUARE -> Pair("Square Root", "Find the square root of a number.")
        RootType.CUBE -> Pair("Cube Root", "Find the cube root of a number.")
        RootType.CUSTOM -> Pair("Custom Root", "Find any nth root of a number.")
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = header,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ResultCard(
    rootType: RootType,
    result: com.example.rootfind.model.RootFindResult,
    displayedResult: String
) {
    val symbol = RootFindFormatter.getRootSymbol(result.rootIndex)
    val valStr = RootFindFormatter.formatValue(result.value)
    val expression = if (result.value < java.math.BigDecimal.ZERO) {
        "$symbol($valStr)"
    } else {
        "$symbol$valStr"
    }

    val (badgeText, badgeBg, badgeTextColor) = when {
        !result.isReal -> Triple(
            "NO REAL RESULT",
            NoRealRedContainerLight,
            NoRealRed
        )
        result.isExact -> Triple(
            "EXACT RESULT",
            ExactGreenContainerLight,
            ExactGreen
        )
        else -> Triple(
            "APPROXIMATE RESULT",
            ApproximateAmberContainerLight,
            ApproximateAmber
        )
    }

    val a11yDescription = RootFindFormatter.getAccessibilityDescription(
        rootType = rootType,
        index = result.rootIndex,
        value = result.value,
        result = result,
        displayedResult = displayedResult
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = a11yDescription },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Root expression, e.g. √16 or ⁴√81
            Text(
                text = expression,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Result value (e.g. 4 or 1.4142135624 or "No real result")
            Text(
                text = displayedResult,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (!result.isReal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeBg,
                modifier = Modifier.semantics { contentDescription = badgeText }
            ) {
                Text(
                    text = badgeText,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun RootTypeSummaryCard(
    rootType: RootType,
    index: Int,
    isReal: Boolean,
    isExact: Boolean
) {
    val typeName = when (rootType) {
        RootType.SQUARE -> "Square Root"
        RootType.CUBE -> "Cube Root"
        RootType.CUSTOM -> "Custom Root"
    }

    val statusName = when {
        !isReal -> "No Real Result"
        isExact -> "Exact"
        else -> "Approximate"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryItem(title = "ROOT TYPE", value = typeName)
            SummaryItem(title = "INDEX", value = index.toString())
            SummaryItem(title = "STATUS", value = statusName)
        }
    }
}

@Composable
fun SummaryItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun VerificationCard(
    verificationText: String,
    isExact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VERIFICATION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                if (!isExact) {
                    Text(
                        text = "Approximate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = verificationText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CalculationStepsCard(
    steps: List<com.example.rootfind.model.CalculationStep>,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "HIDE CALCULATION" else "SHOW CALCULATION",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(250)) + fadeIn(tween(250)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    steps.forEachIndexed { index, step ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${index + 1}. ${step.label}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = step.expression,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = step.result,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
