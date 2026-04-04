package com.fincagis.app.presentation.farms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fincagis.app.core.ui.AppTopBar
import com.fincagis.app.core.ui.theme.FincagisTheme

@Composable
fun FarmDetailScreen(
    farmName: String,
    farmDescription: String,
    farmId: String,
    latitude: Double?,
    longitude: Double?,
    onBackClick: () -> Unit,
    onProjectsClick: () -> Unit,
    onOpenMapClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Detalle de finca",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = farmName,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = farmDescription,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "ID: $farmId",
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "Lat: ${latitude ?: 0.0}  Lon: ${longitude ?: 0.0}",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Button(
                onClick = onProjectsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver proyectos")
            }

            Button(
                onClick = onOpenMapClick,
                enabled = latitude != null && longitude != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Abrir mapa")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FarmDetailScreenPreview() {
    FincagisTheme {
        FarmDetailScreen(
            farmName = "Finca San José",
            farmDescription = "Finca de prueba para gestión predial",
            farmId = "farm_001",
            latitude = -4.036,
            longitude = -79.201,
            onBackClick = {},
            onProjectsClick = {},
            onOpenMapClick = {}
        )
    }
}