package com.fincagis.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fincagis.app.core.database.AppDatabase
import com.fincagis.app.core.ui.theme.FincagisTheme
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeMapLibre()
        val db = createAppDatabase()
        setMainContent(db)
    }

    private fun initializeMapLibre() {
        MapLibre.getInstance(this)
    }

    private fun createAppDatabase(): AppDatabase {
        return Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "fincagis_db"
        )
            .addCallback(
                object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("PRAGMA foreign_keys=ON")
                    }
                }
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    private fun setMainContent(db: AppDatabase) {
        setContent {
            FincagisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(db = db)
                }
            }
        }
    }
}

