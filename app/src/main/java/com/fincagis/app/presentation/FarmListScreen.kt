package com.fincagis.app.presentation.farms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.fincagis.app.data.local.entity.FarmEntity

@Composable
fun FarmListScreen(
    title: String,
    userName: String,
    userEmail: String,
    farms: List<FarmEntity>,
    onFarmClick: (FarmEntity) -> Unit,
    onAddFarmClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = title,
                actionIcon = Icons.Default.Add,
                actionContentDescription = "Nueva finca",
                onActionClick = onAddFarmClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = userName,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = userEmail,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(farms) { farm ->
                    FarmItem(
                        farm = farm,
                        onClick = { onFarmClick(farm) }
                    )
                }
            }
        }
    }
}

@Composable
fun FarmItem(
    farm: FarmEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = farm.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = farm.description ?: "",
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Lat: ${farm.latitude ?: 0.0}  Lon: ${farm.longitude ?: 0.0}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "ID: ${farm.id}",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FarmListScreenPreview() {
    FincagisTheme {
        FarmListScreen(
            title = "Mis fincas",
            userName = "Usuario de prueba",
            userEmail = "usuario@fincagis.com",
            farms = listOf(
                FarmEntity(
                    id = "farm_001",
                    userId = "user_001",
                    name = "Finca San José",
                    description = "Finca de prueba para gestión predial",
                    latitude = -4.036,
                    longitude = -79.201,
                    createdAt = 0L
                ),
                FarmEntity(
                    id = "farm_002",
                    userId = "user_001",
                    name = "Finca El Mirador",
                    description = "Área destinada a cultivos permanentes",
                    latitude = -4.030,
                    longitude = -79.210,
                    createdAt = 0L
                )
            ),
            onFarmClick = {},
            onAddFarmClick = {}
        )
    }
}