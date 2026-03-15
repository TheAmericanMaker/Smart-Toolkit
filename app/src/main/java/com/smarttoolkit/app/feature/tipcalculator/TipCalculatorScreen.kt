package com.smarttoolkit.app.feature.tipcalculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.feature.tipcalculator.RoundingMode
import com.smarttoolkit.app.ui.components.UtilityTopBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TipCalculatorScreen(
    onBack: () -> Unit,
    viewModel: TipCalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { UtilityTopBar(title = "Tip Calculator", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = state.billAmount,
                onValueChange = viewModel::onBillAmountChanged,
                label = { Text("Bill Amount") },
                prefix = { Text("$") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.taxAmount,
                onValueChange = viewModel::onTaxAmountChanged,
                label = { Text("Tax (optional)") },
                prefix = { Text("$") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Tip Percentage", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10, 15, 18, 20).forEach { percent ->
                    FilterChip(
                        selected = !state.isCustomTip && state.tipPercentage == percent,
                        onClick = { viewModel.onTipPercentageSelected(percent) },
                        label = { Text("$percent%") }
                    )
                }
                FilterChip(
                    selected = state.isCustomTip,
                    onClick = { viewModel.onCustomTipChanged(state.customTipText) },
                    label = { Text("Custom") }
                )
            }

            if (state.isCustomTip) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.customTipText,
                    onValueChange = viewModel::onCustomTipChanged,
                    label = { Text("Custom %") },
                    suffix = { Text("%") },
                    modifier = Modifier.width(120.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Split Between", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = viewModel::onPeopleDecrement) {
                    Icon(Icons.Filled.Remove, contentDescription = "Fewer people")
                }
                Text(
                    text = "${state.numberOfPeople}",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = viewModel::onPeopleIncrement) {
                    Icon(Icons.Filled.Add, contentDescription = "More people")
                }
                Text(
                    if (state.numberOfPeople == 1) "person" else "people",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Rounding", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.roundingMode == RoundingMode.NONE,
                    onClick = { viewModel.setRoundingMode(RoundingMode.NONE) },
                    label = { Text("None") }
                )
                FilterChip(
                    selected = state.roundingMode == RoundingMode.ROUND_TOTAL,
                    onClick = { viewModel.setRoundingMode(RoundingMode.ROUND_TOTAL) },
                    label = { Text("Round Total") }
                )
                FilterChip(
                    selected = state.roundingMode == RoundingMode.ROUND_PER_PERSON,
                    onClick = { viewModel.setRoundingMode(RoundingMode.ROUND_PER_PERSON) },
                    label = { Text("Round Per Person") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ResultRow("Tip Amount", state.tipAmount)
                    Spacer(modifier = Modifier.height(8.dp))
                    ResultRow("Total", state.totalAmount)
                    if (state.numberOfPeople > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        ResultRow("Per Person", state.perPersonAmount)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            "$%.2f".format(amount),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
