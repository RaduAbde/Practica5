package model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.Tarea
import kotlin.random.Random


@Database(entities = arrayOf(Tarea::class), version = 1, exportSchema = false)
abstract class TareasDataBase : RoomDatabase() {
    abstract fun tareasDao(): TareasDao
    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: TareasDataBase? = null
        fun getDatabase(context: Context): TareasDataBase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TareasDataBase::class.java,
                    "tareas_database"
                ).addCallback(InicioDbCallback())
                    .build()
                INSTANCE = instance
// return instance
                instance
            }
        }
    }

    //***************CallBack******************************
    /**
     * Permite iniciar la base de datos con Tareas
     */
    private class InicioDbCallback() : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                GlobalScope.launch {
                    cargarDatabase(database.tareasDao())
                }
            }
        }
        //Iniciamos la base de datos con Tareas de ejemplo
        suspend fun cargarDatabase(tareasDao: TareasDao) {
            val tecnicos = listOf(
                "Pepe Gotero",
                "Sacarino Pómez",
                "Mortadelo Fernández",
                "Filemón López",
                "Zipi Climent",
                "Zape Gómez"
            )
            lateinit var tarea: Tarea
            (1..15).forEach{

                tarea = Tarea(
                    (0..4).random(), (0..2).random(),
                    Random.nextBoolean(), (0..2).random(), (0..30).random(),
                    (0..5).random().toFloat(), tecnicos.random(),
                    "tarea $it realizada por el técnico \nLorem ipsum dolor sit amet, " +
                            "consectetur adipiscing elit. Mauris consequat ligula et vehicula " +
                            "mattis. Etiam tristique ornare lacinia. Vestibulum lacus magna, " +
                            "dignissim et tempor id, convallis sed augue"
                ,"")
                tareasDao.addTarea(tarea)
            }
        }
    }


}