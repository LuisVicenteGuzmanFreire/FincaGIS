package com.fincagis.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fincagis.app.core.database.AppDatabase
import com.fincagis.app.core.ui.AppTopBar
import com.fincagis.app.data.local.entity.FarmEntity
import com.fincagis.app.data.local.entity.ProjectEntity
import com.fincagis.app.presentation.farms.FarmDetailScreen
import com.fincagis.app.presentation.farms.FarmListScreen
import com.fincagis.app.presentation.farms.NewFarmScreen
import com.fincagis.app.presentation.main.MapPlaceholderScreen
import com.fincagis.app.presentation.projects.ProjectListScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
private fun rememberFarmById(
    db: AppDatabase,
    farmId: String
): FarmUiModel? {
    var farm by remember { mutableStateOf<FarmUiModel?>(null) }

    LaunchedEffect(farmId) {
        withContext(Dispatchers.IO) {
            val loadedFarm = loadFarmById(db, farmId)
            withContext(Dispatchers.Main) {
                farm = loadedFarm
            }
        }
    }

    return farm
}

@Composable
fun AppNavigation(db: AppDatabase) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "farms"
    ) {
        composable("farms") {
            FarmsRoute(
                db = db,
                onFarmClick = { farm ->
                    navController.navigate("farm_detail/${farm.id}")
                },
                onAddFarmClick = {
                    navController.navigate("new_farm")
                }
            )
        }

        composable("new_farm") {
            NewFarmRoute(
                db = db,
                onBackClick = { navController.popBackStack() },
                onFarmSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = "farm_detail/{farmId}",
            arguments = listOf(
                navArgument("farmId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId").orEmpty()

            FarmDetailRoute(
                db = db,
                farmId = farmId,
                onBackClick = { navController.popBackStack() },
                onProjectsClick = { selectedFarmId, selectedFarmName ->
                    navController.navigate("projects/$selectedFarmId/$selectedFarmName")
                },
                onOpenMapClick = { selectedFarmId ->
                    navController.navigate("farm_map/$selectedFarmId")
                }
            )
        }

        composable(
            route = "projects/{farmId}/{farmName}",
            arguments = listOf(
                navArgument("farmId") { type = NavType.StringType },
                navArgument("farmName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId").orEmpty()
            val farmName = backStackEntry.arguments?.getString("farmName").orEmpty()

            ProjectsRoute(
                db = db,
                farmId = farmId,
                farmName = farmName,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "farm_map/{farmId}",
            arguments = listOf(
                navArgument("farmId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId").orEmpty()

            MapRoute(
                db = db,
                farmId = farmId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun FarmsRoute(
    db: AppDatabase,
    onFarmClick: (FarmEntity) -> Unit,
    onAddFarmClick: () -> Unit
) {
    var userName by remember { mutableStateOf("Cargando usuario...") }
    var userEmail by remember { mutableStateOf("") }
    var farms by remember { mutableStateOf<List<FarmEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val data = loadFarmsScreenData(db)

            withContext(Dispatchers.Main) {
                userName = data.user?.name ?: "Sin usuario"
                userEmail = data.user?.email ?: ""
                farms = data.farms
            }
        }
    }

    FarmListScreen(
        title = "Mis fincas",
        userName = userName,
        userEmail = userEmail,
        farms = farms,
        onFarmClick = onFarmClick,
        onAddFarmClick = onAddFarmClick
    )
}

@Composable
fun NewFarmRoute(
    db: AppDatabase,
    onBackClick: () -> Unit,
    onFarmSaved: () -> Unit
) {
    var farmName by remember { mutableStateOf("") }
    var farmDescription by remember { mutableStateOf("") }
    var latitudeText by remember { mutableStateOf("") }
    var longitudeText by remember { mutableStateOf("") }
    var pendingFarm by remember { mutableStateOf<FarmEntity?>(null) }
    var currentUserId by remember { mutableStateOf<String?>(null) }

    val latitude = latitudeText.trim().replace(",", ".").toDoubleOrNull()
    val longitude = longitudeText.trim().replace(",", ".").toDoubleOrNull()

    val isSaveEnabled = farmName.trim().isNotEmpty() &&
            latitude != null &&
            longitude != null &&
            currentUserId != null

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val user = ensureDefaultUser(db)
            withContext(Dispatchers.Main) {
                currentUserId = user.id
            }
        }
    }

    pendingFarm?.let { farm ->
        LaunchedEffect(farm.id) {
            withContext(Dispatchers.IO) {
                db.farmDao().insert(farm)
            }
            pendingFarm = null
            onFarmSaved()
        }
    }

    NewFarmScreen(
        farmName = farmName,
        farmDescription = farmDescription,
        latitude = latitudeText,
        longitude = longitudeText,
        onFarmNameChange = { farmName = it },
        onFarmDescriptionChange = { farmDescription = it },
        onLatitudeChange = { latitudeText = it },
        onLongitudeChange = { longitudeText = it },
        onSaveClick = {
            val name = farmName.trim()
            val description = farmDescription.trim()
            val parsedLatitude = latitudeText.trim().replace(",", ".").toDoubleOrNull()
            val parsedLongitude = longitudeText.trim().replace(",", ".").toDoubleOrNull()

            val resolvedUserId = currentUserId

            if (
                name.isNotEmpty() &&
                parsedLatitude != null &&
                parsedLongitude != null &&
                resolvedUserId != null
            ) {
                val timestamp = System.currentTimeMillis()
                pendingFarm = FarmEntity(
                    id = "farm_$timestamp",
                    userId = resolvedUserId,
                    name = name,
                    description = description.ifBlank { null },
                    latitude = parsedLatitude,
                    longitude = parsedLongitude,
                    createdAt = timestamp
                )
            }
        },
        onBackClick = onBackClick,
        isSaveEnabled = isSaveEnabled
    )
}

@Composable
fun FarmDetailRoute(
    db: AppDatabase,
    farmId: String,
    onBackClick: () -> Unit,
    onProjectsClick: (String, String) -> Unit,
    onOpenMapClick: (String) -> Unit
) {
    val farm = rememberFarmById(db = db, farmId = farmId)

    FarmDetailScreen(
        farmName = farm?.name ?: "Sin finca",
        farmDescription = farm?.description ?: "",
        farmId = farmId,
        latitude = farm?.latitude,
        longitude = farm?.longitude,
        onBackClick = onBackClick,
        onProjectsClick = {
            onProjectsClick(farmId, farm?.name ?: "Sin finca")
        },
        onOpenMapClick = {
            onOpenMapClick(farmId)
        }
    )
}

@Composable
fun ProjectsRoute(
    db: AppDatabase,
    farmId: String,
    farmName: String,
    onBackClick: () -> Unit
) {
    var projects by remember { mutableStateOf<List<ProjectEntity>>(emptyList()) }

    LaunchedEffect(farmId) {
        withContext(Dispatchers.IO) {
            val savedProjects = db.projectDao().getProjectsByFarmId(farmId)

            withContext(Dispatchers.Main) {
                projects = savedProjects
            }
        }
    }

    ProjectListScreen(
        farmName = farmName,
        projects = projects,
        onBackClick = onBackClick
    )
}

@Composable
fun MapRoute(
    db: AppDatabase,
    farmId: String,
    onBackClick: () -> Unit
) {
    val farm = rememberFarmById(db = db, farmId = farmId)

    val latitude = farm?.latitude
    val longitude = farm?.longitude

    if (farm != null && latitude != null && longitude != null) {
        MapPlaceholderScreen(
            db = db,
            farmId = farmId,
            farmName = farm.name,
            latitude = latitude,
            longitude = longitude,
            onBackClick = onBackClick
        )
    } else {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "Mapa",
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
                Text(
                    text = "No se pudo cargar la ubicaciÃ³n de la finca.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

