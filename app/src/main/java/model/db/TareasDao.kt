package model.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import model.Tarea

@Dao
interface TareasDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTarea(tarea: Tarea)
    @Delete
    suspend fun delTarea(tarea: Tarea)

    @Query("SELECT * FROM tareas ")
    fun getAllTareas(): LiveData<List<Tarea>>
    @Query("SELECT * FROM tareas WHERE pagado= :soloSinPagar")
    fun getTareasFiltroSinPagar(soloSinPagar:Boolean):LiveData<List<Tarea>>
    @Query("SELECT * FROM tareas WHERE estado= :estado")
    fun getTareasFiltroEstado(estado:Int):LiveData<List<Tarea>>
    @Query("SELECT * FROM tareas WHERE (pagado= :soloSinPagar) AND (estado= :estado)")
    fun getTareasFiltroSinPagarEstado(soloSinPagar:Boolean, estado:Int):LiveData<List<Tarea>>

    //Query para Solo por prioridad
    @Query("SELECT * FROM tareas WHERE prioridad= :prioridad")
    fun getTareasFiltroPrioridad(prioridad:Int):LiveData<List<Tarea>>

    //Query para Prioridad y Pago
    @Query("SELECT * FROM tareas WHERE (pagado= :soloSinPagar) AND (prioridad= :prioridad)")
    fun getTareasFiltroSinPagarPrioridad(soloSinPagar: Boolean,prioridad: Int):LiveData<List<Tarea>>

    //Query para Prioridad y Estado
    @Query("SELECT * FROM tareas WHERE (estado= :estado) AND (prioridad= :prioridad)")
    fun getTareasFiltroEstadoPrioridad(estado: Int ,prioridad: Int):LiveData<List<Tarea>>

    //Query para Prioridad, Pago y Estado
    @Query("SELECT * FROM tareas WHERE (pagado= :soloSinPagar) AND (estado= :estado) AND (prioridad= :prioridad)")
    fun getTareasFiltroSinPagarEstadoPrioridad(soloSinPagar: Boolean, estado: Int ,prioridad: Int):LiveData<List<Tarea>>




}