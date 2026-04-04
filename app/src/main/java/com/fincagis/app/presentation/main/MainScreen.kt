package com.fincagis.app.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fincagis.app.core.ui.theme.FincagisTheme

@Composable
fun MainScreen(
    title: String,
    userName: String,
    userEmail: String,
    farmName: String,
    farmDescription: String,
    projectName: String,
    projectDescription: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = userName,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = userEmail,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = farmName,
            modifier = Modifier.padding(top = 24.dp),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = farmDescription,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = projectName,
            modifier = Modifier.padding(top = 24.dp),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = projectDescription,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    FincagisTheme {
        MainScreen(
            title = "FincaGIS",
            userName = "Usuario de prueba",
            userEmail = "usuario@fincagis.com",
            farmName = "Finca San José",
            farmDescription = "Finca de prueba para gestión predial",
            projectName = "Proyecto Base",
            projectDescription = "Proyecto inicial de levantamiento predial"
        )
    }
}