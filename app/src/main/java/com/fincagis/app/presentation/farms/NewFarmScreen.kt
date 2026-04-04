package com.fincagis.app.presentation.farms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fincagis.app.core.ui.AppTopBar
import com.fincagis.app.core.ui.theme.FincagisTheme

@Composable
fun NewFarmScreen(
    farmName: String,
    farmDescription: String,
    latitude: String,
    longitude: String,
    onFarmNameChange: (String) -> Unit,
    onFarmDescriptionChange: (String) -> Unit,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    isSaveEnabled: Boolean
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Nueva finca",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationContentDescription = "Volver",
                onNavigationClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Registrar nueva finca",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = farmName,
                onValueChange = onFarmNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre de la finca") },
                singleLine = true
            )

            OutlinedTextField(
                value = farmDescription,
                onValueChange = onFarmDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descripción") },
                minLines = 3
            )

            OutlinedTextField(
                value = latitude,
                onValueChange = onLatitudeChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Latitud") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = longitude,
                onValueChange = onLongitudeChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Longitud") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Text(
                text = "Ejemplo Loja: latitud -4.036, longitud -79.201",
                style = MaterialTheme.typography.bodySmall
            )

            Button(
                onClick = onSaveClick,
                enabled = isSaveEnabled
            ) {
                Text("Guardar finca")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewFarmScreenPreview() {
    FincagisTheme {
        NewFarmScreen(
            farmName = "Finca Los Laureles",
            farmDescription = "Predio dedicado a cultivos y levantamiento predial",
            latitude = "-4.036",
            longitude = "-79.201",
            onFarmNameChange = {},
            onFarmDescriptionChange = {},
            onLatitudeChange = {},
            onLongitudeChange = {},
            onSaveClick = {},
            onBackClick = {},
            isSaveEnabled = true
        )
    }
}