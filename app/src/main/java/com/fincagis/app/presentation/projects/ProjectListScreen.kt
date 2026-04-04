package com.fincagis.app.presentation.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.fincagis.app.data.local.entity.ProjectEntity

@Composable
fun ProjectListScreen(
    farmName: String,
    projects: List<ProjectEntity>,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Proyectos",
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = farmName,
                style = MaterialTheme.typography.titleLarge
            )

            LazyColumn(
                modifier = Modifier.padding(top = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(projects) { project ->
                    ProjectItem(project = project)
                }
            }
        }
    }
}

@Composable
fun ProjectItem(project: ProjectEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = project.description ?: "",
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "ID: ${project.id}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectListScreenPreview() {
    FincagisTheme {
        ProjectListScreen(
            farmName = "Finca San José",
            projects = listOf(
                ProjectEntity(
                    id = "project_001",
                    farmId = "farm_001",
                    name = "Proyecto Base",
                    description = "Levantamiento predial",
                    createdAt = 0L
                ),
                ProjectEntity(
                    id = "project_002",
                    farmId = "farm_001",
                    name = "Proyecto Riego",
                    description = "Inventario de infraestructura de riego",
                    createdAt = 0L
                )
            ),
            onBackClick = {}
        )
    }
}