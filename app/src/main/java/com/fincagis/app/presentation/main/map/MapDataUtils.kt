package com.fincagis.app.presentation.main.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.fincagis.app.core.database.AppDatabase
import com.fincagis.app.data.local.entity.MapPointEntity
import com.fincagis.app.data.local.entity.PolygonEntity
import com.fincagis.app.data.local.entity.PolygonVertexEntity
import com.fincagis.app.data.local.entity.PolylineEntity
import com.fincagis.app.data.local.entity.PolylineVertexEntity
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng

private const val POINT_PHOTOS_DIR_NAME = "point_photos"
private const val POINT_PHOTO_EXTENSION = ".jpg"
private const val KML_EXPORTS_DIR_NAME = "exports_kml"
private const val KML_FILE_EXTENSION = ".kml"
private const val KML_TEMP_EXPORTS_DIR_NAME = "exports_kml_temp"
private const val KMZ_EXPORTS_DIR_NAME = "exports_kmz"
private const val KMZ_FILE_EXTENSION = ".kmz"

data class ExportablePointPhotoDescriptor(
    val photoPath: String,
    val photoName: String,
    val mimeType: String?,
    val sizeBytes: Long?,
    val capturedAt: Long?
)

data class KmlExportablePointDescriptor(
    val pointId: String,
    val name: String,
    val description: String?,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val photo: ExportablePointPhotoDescriptor?
)

data class KmlExportFileInfo(
    val absolutePath: String,
    val fileName: String,
    val sizeBytes: Long
)

data class KmlCopiedPhotoFileInfo(
    val sourcePath: String,
    val targetFileName: String,
    val targetAbsolutePath: String,
    val sizeBytes: Long
)

data class TemporaryKmlExportPackageInfo(
    val rootDirectoryPath: String,
    val kmlFilePath: String,
    val kmlFileName: String,
    val copiedPhotos: List<KmlCopiedPhotoFileInfo>,
    val missingPhotoReferences: List<String>
)

data class KmzExportFileInfo(
    val absolutePath: String,
    val fileName: String,
    val sizeBytes: Long,
    val temporaryPackageDirectoryPath: String?,
    val copiedPhotos: List<KmlCopiedPhotoFileInfo>,
    val missingPhotoReferences: List<String>
)

fun hasExportablePointPhoto(point: MapPointEntity): Boolean {
    return !point.photoPath.isNullOrBlank() && !point.photoName.isNullOrBlank()
}

fun getExportablePointPhotoDescriptor(point: MapPointEntity): ExportablePointPhotoDescriptor? {
    if (!hasExportablePointPhoto(point)) {
        return null
    }

    val normalizedPhotoPath = point.photoPath?.trim().orEmpty()
    val normalizedPhotoName = point.photoName?.trim().orEmpty()
    if (normalizedPhotoPath.isBlank() || normalizedPhotoName.isBlank()) {
        return null
    }

    return ExportablePointPhotoDescriptor(
        photoPath = normalizedPhotoPath,
        photoName = normalizedPhotoName,
        mimeType = point.photoMimeType,
        sizeBytes = point.photoSizeBytes,
        capturedAt = point.photoCapturedAt
    )
}

fun toKmlExportablePointDescriptor(point: MapPointEntity): KmlExportablePointDescriptor {
    return KmlExportablePointDescriptor(
        pointId = point.id,
        name = point.name,
        description = point.description,
        category = point.category,
        latitude = point.latitude,
        longitude = point.longitude,
        photo = getExportablePointPhotoDescriptor(point)
    )
}

fun toKmlExportablePointDescriptors(points: List<MapPointEntity>): List<KmlExportablePointDescriptor> {
    return points.map { point -> toKmlExportablePointDescriptor(point) }
}

fun escapeXmlText(value: String): String {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

fun buildKmlPointDescription(point: KmlExportablePointDescriptor): String {
    val descriptionLines = mutableListOf<String>()
    descriptionLines += "Categoria: ${point.category}"
    if (!point.description.isNullOrBlank()) {
        descriptionLines += "Descripcion: ${point.description}"
    }
    point.photo?.photoName?.takeIf { it.isNotBlank() }?.let { photoName ->
        descriptionLines += "Foto: $photoName"
    }
    return descriptionLines.joinToString("\n")
}

fun buildKmlPlacemark(point: KmlExportablePointDescriptor): String {
    val safeName = escapeXmlText(point.name)
    val safeDescription = escapeXmlText(buildKmlPointDescription(point))
    val coordinates = "${point.longitude},${point.latitude}"
    return """
        <Placemark>
          <name>$safeName</name>
          <description>$safeDescription</description>
          <Point>
            <coordinates>$coordinates</coordinates>
          </Point>
        </Placemark>
    """.trimIndent()
}

fun buildPointsKmlDocument(points: List<KmlExportablePointDescriptor>, documentName: String = "FincaGIS Puntos"): String {
    val placemarks = points.joinToString(separator = "\n") { point ->
        buildKmlPlacemark(point)
    }
    val safeDocumentName = escapeXmlText(documentName)
    return """
        <?xml version="1.0" encoding="UTF-8"?>
        <kml xmlns="http://www.opengis.net/kml/2.2">
          <Document>
            <name>$safeDocumentName</name>
        $placemarks
          </Document>
        </kml>
    """.trimIndent()
}

fun buildPointsKmlDocumentFromEntities(points: List<MapPointEntity>, documentName: String = "FincaGIS Puntos"): String {
    val exportablePoints = toKmlExportablePointDescriptors(points)
    return buildPointsKmlDocument(
        points = exportablePoints,
        documentName = documentName
    )
}

fun buildKmlFileName(baseName: String = "fincagis_puntos", timestamp: Long = System.currentTimeMillis()): String {
    val safeBaseName = baseName
        .trim()
        .ifBlank { "fincagis_puntos" }
        .replace(Regex("[^a-zA-Z0-9_-]"), "_")
    return "${safeBaseName}_$timestamp$KML_FILE_EXTENSION"
}

fun getOrCreateKmlExportDirectory(context: Context): File {
    val exportDir = File(context.filesDir, KML_EXPORTS_DIR_NAME)
    if (!exportDir.exists()) {
        exportDir.mkdirs()
    }
    return exportDir
}

suspend fun writeKmlStringToPrivateFile(
    context: Context,
    kmlContent: String,
    baseFileName: String = "fincagis_puntos"
): KmlExportFileInfo? {
    return withContext(Dispatchers.IO) {
        try {
            val exportDir = getOrCreateKmlExportDirectory(context)
            val fileName = buildKmlFileName(baseName = baseFileName)
            val targetFile = File(exportDir, fileName)
            targetFile.writeText(kmlContent, Charsets.UTF_8)

            KmlExportFileInfo(
                absolutePath = targetFile.absolutePath,
                fileName = fileName,
                sizeBytes = targetFile.length()
            )
        } catch (_: Exception) {
            null
        }
    }
}

suspend fun exportPointsToKmlFile(
    context: Context,
    points: List<MapPointEntity>,
    documentName: String = "FincaGIS Puntos",
    baseFileName: String = "fincagis_puntos"
): KmlExportFileInfo? {
    val kmlContent = buildPointsKmlDocumentFromEntities(
        points = points,
        documentName = documentName
    )
    return writeKmlStringToPrivateFile(
        context = context,
        kmlContent = kmlContent,
        baseFileName = baseFileName
    )
}

fun buildTemporaryKmlExportFolderName(
    baseName: String = "fincagis_export_temp",
    timestamp: Long = System.currentTimeMillis()
): String {
    val safeBaseName = baseName
        .trim()
        .ifBlank { "fincagis_export_temp" }
        .replace(Regex("[^a-zA-Z0-9_-]"), "_")
    return "${safeBaseName}_$timestamp"
}

fun getOrCreateTemporaryKmlExportRootDirectory(context: Context): File {
    val tempRoot = File(context.filesDir, KML_TEMP_EXPORTS_DIR_NAME)
    if (!tempRoot.exists()) {
        tempRoot.mkdirs()
    }
    return tempRoot
}

fun sanitizeExportPhotoName(photoName: String): String {
    val trimmed = photoName.trim()
    val withoutPath = trimmed.replace("\\", "/").substringAfterLast("/")
    return withoutPath.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "photo.jpg" }
}

suspend fun exportPointsToTemporaryKmlFolder(
    context: Context,
    points: List<MapPointEntity>,
    documentName: String = "FincaGIS Puntos",
    packageBaseName: String = "fincagis_export_temp",
    kmlBaseFileName: String = "fincagis_puntos",
    kmlFileNameOverride: String? = null
): TemporaryKmlExportPackageInfo? {
    return withContext(Dispatchers.IO) {
        try {
            val exportablePoints = toKmlExportablePointDescriptors(points)
            val tempRoot = getOrCreateTemporaryKmlExportRootDirectory(context)
            val packageDir = File(
                tempRoot,
                buildTemporaryKmlExportFolderName(baseName = packageBaseName)
            ).apply { mkdirs() }

            val copiedPhotos = mutableListOf<KmlCopiedPhotoFileInfo>()
            val missingPhotoReferences = mutableListOf<String>()
            val usedNames = mutableSetOf<String>()
            val finalPhotoNameByPointId = mutableMapOf<String, String>()

            exportablePoints.forEach { point ->
                val photo = point.photo ?: return@forEach
                val sourceFile = File(photo.photoPath)
                if (!sourceFile.exists() || !sourceFile.isFile) {
                    missingPhotoReferences += "${point.pointId}:${photo.photoName}"
                    return@forEach
                }

                val baseName = sanitizeExportPhotoName(photo.photoName)
                var targetName = baseName
                var index = 1
                while (targetName in usedNames || File(packageDir, targetName).exists()) {
                    val dotIndex = baseName.lastIndexOf('.')
                    targetName = if (dotIndex > 0) {
                        "${baseName.substring(0, dotIndex)}_$index${baseName.substring(dotIndex)}"
                    } else {
                        "${baseName}_$index"
                    }
                    index += 1
                }
                usedNames += targetName

                val targetFile = File(packageDir, targetName)
                sourceFile.copyTo(target = targetFile, overwrite = true)
                copiedPhotos += KmlCopiedPhotoFileInfo(
                    sourcePath = sourceFile.absolutePath,
                    targetFileName = targetName,
                    targetAbsolutePath = targetFile.absolutePath,
                    sizeBytes = targetFile.length()
                )
                finalPhotoNameByPointId[point.pointId] = targetName
            }

            val pointsForKml = exportablePoints.map { point ->
                val currentPhoto = point.photo ?: return@map point
                val finalPhotoName = finalPhotoNameByPointId[point.pointId]
                if (finalPhotoName == null) {
                    point.copy(photo = null)
                } else {
                    point.copy(photo = currentPhoto.copy(photoName = finalPhotoName))
                }
            }

            val normalizedKmlFileName = kmlFileNameOverride
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: buildKmlFileName(baseName = kmlBaseFileName)
            val safeKmlFileName = if (
                normalizedKmlFileName.endsWith(KML_FILE_EXTENSION, ignoreCase = true)
            ) {
                normalizedKmlFileName
            } else {
                "$normalizedKmlFileName$KML_FILE_EXTENSION"
            }

            val kmlFile = File(packageDir, safeKmlFileName)
            val kmlContent = buildPointsKmlDocument(
                points = pointsForKml,
                documentName = documentName
            )
            kmlFile.writeText(kmlContent, Charsets.UTF_8)

            TemporaryKmlExportPackageInfo(
                rootDirectoryPath = packageDir.absolutePath,
                kmlFilePath = kmlFile.absolutePath,
                kmlFileName = safeKmlFileName,
                copiedPhotos = copiedPhotos,
                missingPhotoReferences = missingPhotoReferences
            )
        } catch (_: Exception) {
            null
        }
    }
}

fun buildKmzFileName(baseName: String = "fincagis_puntos", timestamp: Long = System.currentTimeMillis()): String {
    val safeBaseName = baseName
        .trim()
        .ifBlank { "fincagis_puntos" }
        .replace(Regex("[^a-zA-Z0-9_-]"), "_")
    return "${safeBaseName}_$timestamp$KMZ_FILE_EXTENSION"
}

fun getOrCreateKmzExportDirectory(context: Context): File {
    val kmzDir = File(context.filesDir, KMZ_EXPORTS_DIR_NAME)
    if (!kmzDir.exists()) {
        kmzDir.mkdirs()
    }
    return kmzDir
}

suspend fun compressDirectoryToKmz(sourceDirectory: File, kmzFile: File): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            if (!sourceDirectory.exists() || !sourceDirectory.isDirectory) {
                return@withContext false
            }
            if (kmzFile.exists()) {
                kmzFile.delete()
            }
            kmzFile.parentFile?.mkdirs()

            ZipOutputStream(FileOutputStream(kmzFile)).use { zipOut ->
                sourceDirectory.walkTopDown()
                    .filter { it.isFile }
                    .forEach { file ->
                        val relativePath = file
                            .relativeTo(sourceDirectory)
                            .path
                            .replace("\\", "/")
                        if (relativePath.isBlank()) return@forEach

                        zipOut.putNextEntry(ZipEntry(relativePath))
                        file.inputStream().use { input ->
                            input.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                    }
            }

            true
        } catch (_: Exception) {
            false
        }
    }
}

suspend fun exportPointsToKmzFile(
    context: Context,
    points: List<MapPointEntity>,
    documentName: String = "FincaGIS Puntos",
    packageBaseName: String = "fincagis_export_temp",
    kmlBaseFileName: String = "fincagis_puntos",
    kmzBaseFileName: String = "fincagis_puntos",
    deleteTemporaryFolderAfterPackaging: Boolean = false
): KmzExportFileInfo? {
    val temporaryPackage = exportPointsToTemporaryKmlFolder(
        context = context,
        points = points,
        documentName = documentName,
        packageBaseName = packageBaseName,
        kmlBaseFileName = kmlBaseFileName,
        kmlFileNameOverride = "doc.kml"
    ) ?: return null

    val packageDir = File(temporaryPackage.rootDirectoryPath)
    val kmzDir = getOrCreateKmzExportDirectory(context)
    val kmzFileName = buildKmzFileName(baseName = kmzBaseFileName)
    val kmzFile = File(kmzDir, kmzFileName)

    val compressionOk = compressDirectoryToKmz(
        sourceDirectory = packageDir,
        kmzFile = kmzFile
    )
    if (!compressionOk) {
        return null
    }

    val keepTemporaryFolder = !deleteTemporaryFolderAfterPackaging
    if (!keepTemporaryFolder) {
        packageDir.deleteRecursively()
    }

    return KmzExportFileInfo(
        absolutePath = kmzFile.absolutePath,
        fileName = kmzFileName,
        sizeBytes = kmzFile.length(),
        temporaryPackageDirectoryPath = if (keepTemporaryFolder) packageDir.absolutePath else null,
        copiedPhotos = temporaryPackage.copiedPhotos,
        missingPhotoReferences = temporaryPackage.missingPhotoReferences
    )
}

fun resolvePointPhotoMimeType(photoPath: String, photoFileName: String): String? {
    val detectedMimeType = BitmapFactory.Options().let { options ->
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(photoPath, options)
        options.outMimeType
    }
    if (!detectedMimeType.isNullOrBlank()) {
        return detectedMimeType
    }

    return when {
        photoFileName.endsWith(".jpg", ignoreCase = true) ||
            photoFileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
        photoFileName.endsWith(".png", ignoreCase = true) -> "image/png"
        else -> null
    }
}

fun getVerticesOfSelectedPolygon(
    savedPolygons: List<Pair<PolygonEntity, List<PolygonVertexEntity>>>,
    selectedPolygonId: String?
): List<PolygonVertexEntity> {
    return savedPolygons
        .find { it.first.id == selectedPolygonId }
        ?.second
        ?: emptyList()
}

suspend fun deleteSelectedVertexFromPolygon(
    db: AppDatabase,
    farmId: String,
    polygonId: String,
    vertexId: String,
    currentVertices: List<PolygonVertexEntity>
): List<Pair<PolygonEntity, List<PolygonVertexEntity>>> {
    val updatedVertices = currentVertices
        .sortedBy { it.vertexOrder }
        .filterNot { it.id == vertexId }
        .mapIndexed { index, vertex ->
            vertex.copy(vertexOrder = index)
        }

    withContext(Dispatchers.IO) {
        db.polygonDao().deleteVerticesByPolygonId(polygonId)
        db.polygonDao().insertVertices(updatedVertices)
    }

    return loadMapData(db, farmId).second
}

suspend fun persistPolygonVertices(
    db: AppDatabase,
    farmId: String,
    vertices: List<PolygonVertexEntity>
): List<Pair<PolygonEntity, List<PolygonVertexEntity>>> {
    withContext(Dispatchers.IO) {
        db.polygonDao().insertVertices(vertices.sortedBy { it.vertexOrder })
    }

    return loadMapData(db, farmId).second
}

suspend fun createPointAtLocation(
    db: AppDatabase,
    farmId: String,
    captureCategory: String,
    latitude: Double,
    longitude: Double
): Triple<String, String, List<MapPointEntity>> {
    return withContext(Dispatchers.IO) {
        val currentPoints = db.mapPointDao().getPointsByFarmId(farmId)
        val timestamp = System.currentTimeMillis()
        val pointId = "point_$timestamp"

        val nextPointName = buildNextPointName(
            category = captureCategory,
            existingPoints = currentPoints
        )

        db.mapPointDao().insert(
            MapPointEntity(
                id = pointId,
                farmId = farmId,
                name = nextPointName,
                description = null,
                category = captureCategory,
                latitude = latitude,
                longitude = longitude,
                createdAt = timestamp
            )
        )

        Triple(
            pointId,
            nextPointName,
            db.mapPointDao().getPointsByFarmId(farmId)
        )
    }
}

suspend fun savePolygon(
    db: AppDatabase,
    farmId: String,
    polygonName: String,
    polygonDescription: String,
    polygonVertices: List<LatLng>
): List<Pair<PolygonEntity, List<PolygonVertexEntity>>> {
    return withContext(Dispatchers.IO) {
        val polygonId = "polygon_${System.currentTimeMillis()}"
        val createdAt = System.currentTimeMillis()

        db.polygonDao().insertPolygon(
            PolygonEntity(
                id = polygonId,
                farmId = farmId,
                name = polygonName,
                description = polygonDescription.ifBlank { null },
                category = "General",
                createdAt = createdAt
            )
        )

        val vertices = polygonVertices.mapIndexed { index, vertex ->
            PolygonVertexEntity(
                id = "${polygonId}_vertex_$index",
                polygonId = polygonId,
                vertexOrder = index,
                latitude = vertex.latitude,
                longitude = vertex.longitude
            )
        }

        db.polygonDao().insertVertices(vertices)

        db.polygonDao().getPolygonsByFarmId(farmId).map { polygon ->
            val loadedVertices = db.polygonDao().getVerticesByPolygonId(polygon.id)
            polygon to loadedVertices
        }
    }
}

suspend fun updatePointAttributes(
    db: AppDatabase,
    farmId: String,
    pointId: String,
    name: String,
    description: String,
    category: String
): List<MapPointEntity> {
    return withContext(Dispatchers.IO) {
        db.mapPointDao().updatePointAttributes(
            pointId = pointId,
            name = name,
            description = description.ifBlank { null },
            category = category
        )
        db.mapPointDao().getPointsByFarmId(farmId)
    }
}

suspend fun updatePointPosition(
    db: AppDatabase,
    farmId: String,
    pointId: String,
    latitude: Double,
    longitude: Double
): List<MapPointEntity> {
    return withContext(Dispatchers.IO) {
        db.mapPointDao().updatePointPosition(
            pointId = pointId,
            latitude = latitude,
            longitude = longitude
        )
        db.mapPointDao().getPointsByFarmId(farmId)
    }
}

data class PointPhotoFileInfo(
    // Absolute local path (private app storage).
    val absolutePath: String,
    // Stable exportable file name to be referenced later in KMZ/KML export.
    val fileName: String,
    val capturedAt: Long
)

fun buildPointPhotoFileName(pointId: String, timestamp: Long): String {
    val safePointId = pointId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    return "point_${safePointId}_$timestamp$POINT_PHOTO_EXTENSION"
}

suspend fun preparePointPhotoFileForCapture(
    context: Context,
    pointId: String
): PointPhotoFileInfo? {
    return withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis()
            val fileName = buildPointPhotoFileName(pointId = pointId, timestamp = timestamp)
            val targetDir = File(context.filesDir, POINT_PHOTOS_DIR_NAME)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val targetFile = File(targetDir, fileName)
            if (targetFile.exists()) {
                targetFile.delete()
            }
            targetFile.createNewFile()

            PointPhotoFileInfo(
                absolutePath = targetFile.absolutePath,
                fileName = fileName,
                capturedAt = timestamp
            )
        } catch (_: Exception) {
            null
        }
    }
}

@Deprecated(
    message = "Legacy preview/bitmap flow. Keep only for compatibility; full-size capture uses preparePointPhotoFileForCapture + TakePicture()."
)
suspend fun savePointPhotoToAppStorage(
    context: Context,
    pointId: String,
    bitmap: Bitmap
): PointPhotoFileInfo? {
    return withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis()
            val fileName = buildPointPhotoFileName(pointId = pointId, timestamp = timestamp)
            val targetDir = File(context.filesDir, POINT_PHOTOS_DIR_NAME)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val targetFile = File(targetDir, fileName)
            FileOutputStream(targetFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                output.flush()
            }

            PointPhotoFileInfo(
                absolutePath = targetFile.absolutePath,
                fileName = fileName,
                capturedAt = timestamp
            )
        } catch (_: Exception) {
            null
        }
    }
}

suspend fun deletePointPhotoFile(photoPath: String?): Boolean {
    val normalizedPath = photoPath?.trim()
    if (normalizedPath.isNullOrBlank()) {
        return false
    }
    return withContext(Dispatchers.IO) {
        try {
            val file = File(normalizedPath)
            if (!file.exists()) {
                return@withContext true
            }
            file.delete()
        } catch (_: Exception) {
            false
        }
    }
}

fun decodePointPhotoBitmapForPreview(photoPath: String): Bitmap? {
    val sourceBitmap = BitmapFactory.decodeFile(photoPath) ?: return null
    return try {
        val exif = ExifInterface(photoPath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
        }

        if (matrix.isIdentity) {
            sourceBitmap
        } else {
            Bitmap.createBitmap(
                sourceBitmap,
                0,
                0,
                sourceBitmap.width,
                sourceBitmap.height,
                matrix,
                true
            ).also { rotated ->
                if (rotated !== sourceBitmap) {
                    sourceBitmap.recycle()
                }
            }
        }
    } catch (_: Exception) {
        sourceBitmap
    }
}

suspend fun updatePointPhotoReference(
    db: AppDatabase,
    farmId: String,
    pointId: String,
    photoPath: String,
    photoName: String,
    photoCapturedAt: Long,
    photoMimeType: String?,
    photoSizeBytes: Long?
): List<MapPointEntity> {
    return withContext(Dispatchers.IO) {
        db.mapPointDao().updatePointPhotoReference(
            pointId = pointId,
            photoPath = photoPath,
            photoName = photoName,
            photoCapturedAt = photoCapturedAt,
            photoMimeType = photoMimeType,
            photoSizeBytes = photoSizeBytes
        )
        db.mapPointDao().getPointsByFarmId(farmId)
    }
}

suspend fun deletePointById(
    db: AppDatabase,
    farmId: String,
    pointId: String
): List<MapPointEntity> {
    return withContext(Dispatchers.IO) {
        db.mapPointDao().deleteById(pointId)
        db.mapPointDao().getPointsByFarmId(farmId)
    }
}

suspend fun deletePolygonById(
    db: AppDatabase,
    farmId: String,
    polygonId: String
): List<Pair<PolygonEntity, List<PolygonVertexEntity>>> {
    return withContext(Dispatchers.IO) {
        db.polygonDao().deletePolygonById(polygonId)

        db.polygonDao().getPolygonsByFarmId(farmId).map { polygon ->
            val vertices = db.polygonDao().getVerticesByPolygonId(polygon.id)
            polygon to vertices
        }
    }
}

suspend fun updatePolygonAttributes(
    db: AppDatabase,
    farmId: String,
    polygonId: String,
    name: String,
    description: String,
    category: String
): List<Pair<PolygonEntity, List<PolygonVertexEntity>>> {
    return withContext(Dispatchers.IO) {
        db.polygonDao().updatePolygonAttributes(
            polygonId = polygonId,
            name = name,
            description = description.ifBlank { null },
            category = category
        )

        db.polygonDao().getPolygonsByFarmId(farmId).map { polygon ->
            val vertices = db.polygonDao().getVerticesByPolygonId(polygon.id)
            polygon to vertices
        }
    }
}

suspend fun savePolyline(
    db: AppDatabase,
    farmId: String,
    polylineName: String,
    polylineDescription: String,
    polylineVertices: List<LatLng>
): List<Pair<PolylineEntity, List<PolylineVertexEntity>>> {
    return withContext(Dispatchers.IO) {
        val polylineId = "polyline_${System.currentTimeMillis()}"
        val createdAt = System.currentTimeMillis()

        db.polylineDao().insertPolyline(
            PolylineEntity(
                id = polylineId,
                farmId = farmId,
                name = polylineName,
                description = polylineDescription.ifBlank { null },
                category = "General",
                createdAt = createdAt
            )
        )

        val vertices = polylineVertices.mapIndexed { index, vertex ->
            PolylineVertexEntity(
                id = "${polylineId}_vertex_$index",
                polylineId = polylineId,
                vertexOrder = index,
                latitude = vertex.latitude,
                longitude = vertex.longitude
            )
        }

        db.polylineDao().insertVertices(vertices)

        db.polylineDao().getPolylinesByFarmId(farmId).map { polyline ->
            val loadedVertices = db.polylineDao().getVerticesByPolylineId(polyline.id)
            polyline to loadedVertices
        }
    }
}

suspend fun deletePolylineById(
    db: AppDatabase,
    farmId: String,
    polylineId: String
): List<Pair<PolylineEntity, List<PolylineVertexEntity>>> {
    return withContext(Dispatchers.IO) {
        db.polylineDao().deletePolylineById(polylineId)

        db.polylineDao().getPolylinesByFarmId(farmId).map { polyline ->
            val vertices = db.polylineDao().getVerticesByPolylineId(polyline.id)
            polyline to vertices
        }
    }
}

suspend fun updatePolylineAttributes(
    db: AppDatabase,
    farmId: String,
    polylineId: String,
    name: String,
    description: String,
    category: String
): List<Pair<PolylineEntity, List<PolylineVertexEntity>>> {
    return withContext(Dispatchers.IO) {
        db.polylineDao().updatePolylineAttributes(
            polylineId = polylineId,
            name = name,
            description = description.ifBlank { null },
            category = category
        )

        db.polylineDao().getPolylinesByFarmId(farmId).map { polyline ->
            val vertices = db.polylineDao().getVerticesByPolylineId(polyline.id)
            polyline to vertices
        }
    }
}

fun getVerticesOfSelectedPolyline(
    savedPolylines: List<Pair<PolylineEntity, List<PolylineVertexEntity>>>,
    selectedPolylineId: String?
): List<PolylineVertexEntity> {
    return savedPolylines
        .find { it.first.id == selectedPolylineId }
        ?.second
        ?: emptyList()
}

suspend fun persistPolylineVertices(
    db: AppDatabase,
    farmId: String,
    polylineId: String,
    vertices: List<PolylineVertexEntity>
): List<Pair<PolylineEntity, List<PolylineVertexEntity>>> {
    withContext(Dispatchers.IO) {
        db.polylineDao().deleteVerticesByPolylineId(polylineId)
        db.polylineDao().insertVertices(vertices.sortedBy { it.vertexOrder })
    }

    return loadPolylineData(db, farmId)
}

suspend fun deleteSelectedVertexFromPolyline(
    db: AppDatabase,
    farmId: String,
    polylineId: String,
    vertexId: String,
    currentVertices: List<PolylineVertexEntity>
): List<Pair<PolylineEntity, List<PolylineVertexEntity>>> {
    val updatedVertices = currentVertices
        .sortedBy { it.vertexOrder }
        .filterNot { it.id == vertexId }
        .mapIndexed { index, vertex ->
            vertex.copy(vertexOrder = index)
        }

    withContext(Dispatchers.IO) {
        db.polylineDao().deleteVerticesByPolylineId(polylineId)
        db.polylineDao().insertVertices(updatedVertices)
    }

    return loadPolylineData(db, farmId)
}

