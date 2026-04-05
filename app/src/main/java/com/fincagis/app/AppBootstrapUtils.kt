package com.fincagis.app

import com.fincagis.app.core.database.AppDatabase
import com.fincagis.app.data.local.entity.FarmEntity
import com.fincagis.app.data.local.entity.ProjectEntity
import com.fincagis.app.data.local.entity.UserEntity

private const val DEFAULT_USER_ID = "user_001"
private const val DEFAULT_USER_EMAIL = "usuario@fincagis.com"
private const val DEFAULT_USER_NAME = "Usuario de prueba"

suspend fun ensureDefaultUser(db: AppDatabase): UserEntity {
    val userDao = db.userDao()

    val existingUser = userDao.getUser()
    if (existingUser != null) return existingUser

    val user = UserEntity(
        id = DEFAULT_USER_ID,
        email = DEFAULT_USER_EMAIL,
        name = DEFAULT_USER_NAME,
        createdAt = System.currentTimeMillis()
    )
    userDao.insert(user)
    return user
}

suspend fun seedDemoDataIfNeeded(
    db: AppDatabase,
    user: UserEntity
) {
    val farmDao = db.farmDao()
    val projectDao = db.projectDao()

    val existingFarms = farmDao.getAllFarms()
    if (existingFarms.isEmpty()) {
        farmDao.insertAll(
            listOf(
                FarmEntity(
                    id = "farm_001",
                    userId = user.id,
                    name = "Finca San José",
                    description = "Finca de prueba para gestión predial",
                    latitude = -4.036,
                    longitude = -79.201,
                    createdAt = System.currentTimeMillis()
                ),
                FarmEntity(
                    id = "farm_002",
                    userId = user.id,
                    name = "Finca El Mirador",
                    description = "Área destinada a cultivos permanentes",
                    latitude = -4.036,
                    longitude = -79.201,
                    createdAt = System.currentTimeMillis()
                ),
                FarmEntity(
                    id = "farm_003",
                    userId = user.id,
                    name = "Finca La Esperanza",
                    description = "Predio con zonas de riego e infraestructura",
                    latitude = -4.036,
                    longitude = -79.201,
                    createdAt = System.currentTimeMillis()
                )
            )
        )
    }

    val existingProjectsFarm1 = projectDao.getProjectsByFarmId("farm_001")
    if (existingProjectsFarm1.isEmpty()) {
        projectDao.insertAll(
            listOf(
                ProjectEntity(
                    id = "project_001",
                    farmId = "farm_001",
                    name = "Proyecto Base",
                    description = "Levantamiento predial",
                    createdAt = System.currentTimeMillis()
                ),
                ProjectEntity(
                    id = "project_002",
                    farmId = "farm_001",
                    name = "Proyecto Riego",
                    description = "Inventario de infraestructura de riego",
                    createdAt = System.currentTimeMillis()
                ),
                ProjectEntity(
                    id = "project_003",
                    farmId = "farm_002",
                    name = "Proyecto Banano",
                    description = "Registro de lotes y drenajes",
                    createdAt = System.currentTimeMillis()
                ),
                ProjectEntity(
                    id = "project_004",
                    farmId = "farm_003",
                    name = "Proyecto Cacao",
                    description = "Inventario de parcelas productivas",
                    createdAt = System.currentTimeMillis()
                )
            )
        )
    }
}

suspend fun loadCurrentUserAndFarms(
    db: AppDatabase
): Pair<UserEntity?, List<FarmEntity>> {
    val user = db.userDao().getUser()
    val farms = db.farmDao().getAllFarms()
    return user to farms
}

data class FarmsScreenData(
    val user: UserEntity?,
    val farms: List<FarmEntity>
)

suspend fun loadFarmsScreenData(
    db: AppDatabase
): FarmsScreenData {
    val user = ensureDefaultUser(db)
    seedDemoDataIfNeeded(db, user)
    val (savedUser, savedFarms) = loadCurrentUserAndFarms(db)
    return FarmsScreenData(user = savedUser, farms = savedFarms)
}

data class FarmUiModel(
    val id: String,
    val name: String,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?
)

suspend fun loadFarmById(
    db: AppDatabase,
    farmId: String
): FarmUiModel? {
    val farm = db.farmDao().getFarmById(farmId) ?: return null

    return FarmUiModel(
        id = farm.id,
        name = farm.name,
        description = farm.description,
        latitude = farm.latitude,
        longitude = farm.longitude
    )
}

