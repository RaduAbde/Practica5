package repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import model.Tarea
import model.db.TareasDao
import model.db.TareasDataBase
import model.temp.ModelTempTareas

object Repository {

    //instancia al modelo
    //private lateinit var modelTareas: ModelTempTareas
    private lateinit var modelTareas:TareasDao
    //el context suele ser necesario para recuperar datos
    private lateinit var application: Application
    //inicio del objeto singleton
    operator fun invoke(context: Context){
        this.application= context.applicationContext as Application
        //iniciamos el modelo
        //ModelTempTareas(application)
        //modelTareas=ModelTempTareas
        //iniciamos la Base de Datos

        modelTareas=TareasDataBase.getDatabase(application).tareasDao()
    }

    //Creamos los métodos de añadir, borrar y recuperar la lista como llamadas a los
    //equivalentes en el modelo
    suspend fun addTarea(tarea: Tarea)= modelTareas.addTarea(tarea)
    suspend fun delTarea(tarea: Tarea)= modelTareas.delTarea(tarea)
    fun getAllTareas()=modelTareas.getAllTareas()
    fun getTareasFiltroSinPagar (soloSinPagar:Boolean)= modelTareas.getTareasFiltroSinPagar(soloSinPagar)

    fun getTareasEstadoFitro (estado:Int)= modelTareas.getTareasFiltroEstado(estado)

    fun getTareasFiltroSinPagarEstado(soloSinPagar:Boolean, estado:Int)= modelTareas.getTareasFiltroSinPagarEstado(soloSinPagar,estado)

    fun getTareasFiltroPrioridad(prioridad:Int) = modelTareas.getTareasFiltroPrioridad(prioridad)

    fun getTareasFiltroSinPagarPrioridad(soloSinPagar: Boolean,prioridad: Int) = modelTareas.getTareasFiltroSinPagarPrioridad(soloSinPagar, prioridad)

    fun getTareasFiltroEstadoPrioridad(estado: Int ,prioridad: Int) = modelTareas.getTareasFiltroEstadoPrioridad(estado, prioridad)

    fun getTareasFiltroSinPagarEstadoPrioridad(soloSinPagar: Boolean, estado: Int ,prioridad: Int) = modelTareas.getTareasFiltroSinPagarEstadoPrioridad(soloSinPagar, estado, prioridad)

}