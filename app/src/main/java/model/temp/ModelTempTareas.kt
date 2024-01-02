package model.temp

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.Tarea
import kotlin.random.Random

object ModelTempTareas {

    //lista de tareas
    private val tareas = ArrayList<Tarea>()
    //LiveData para observar en la vista los cambios en la lista
    private val tareasLiveData = MutableLiveData<List<Tarea>>(tareas)
    //el context que suele ser necesario en acceso a datos
    private lateinit var application: Application

    //Permite iniciar el objeto Singleton
    operator fun invoke(context: Context){
        this.application= context.applicationContext as Application
        //lanzamos una corrutina
        GlobalScope.launch{iniciaPruebaTareas()}
    }

    /**
     * devuelve un LiveData en vez de MutableLiveData
    para evitar su modificación en las capas superiores
     */
    fun getAllTareas(): LiveData<List<Tarea>> {
        tareasLiveData.value= tareas
        return tareasLiveData
    }

    /**
     * añade una tarea, si existe(id iguales) la sustituye
     * y si no la añade. Posteriormente actualiza el LiveData
     * que permitirá avisar a quien esté observando
     */
    suspend fun addTarea(tarea: Tarea) {
        val pos = tareas.indexOf(tarea)
        if (pos < 0) {//si no existe
            tareas.add(tarea)
        } else {
            //si existe se sustituye
            tareas.set(pos, tarea)
        }
        //actualiza el LiveData
        //tareasLiveData.value = tareas
        tareasLiveData.postValue(tareas)
    }

    /**
     * Borra una tarea y actualiza el LiveData
     * para avisar a los observadores
     */
    suspend fun delTarea(tarea: Tarea) {
        //Thread.sleep(10000)
        tareas.remove(tarea)
        //tareasLiveData.value = tareas
        tareasLiveData.postValue(tareas)
    }

    fun getTareasFiltroSinPagar(soloSinPagar:Boolean): LiveData<List<Tarea>> {
        //devuelve el LiveData con la lista filtrada o entera
        tareasLiveData.value=if(soloSinPagar)
            tareas.filter { !it.pagado } as ArrayList<Tarea>
        else
            tareas
        return tareasLiveData
    }

    //Funcion para filtrar por estado de tareas
    //0 Abierta
    //1 En Curso
    //2 Cerrada
    //else Todas
    fun getTareasFiltroEstado(estado:Int): LiveData<List<Tarea>>{
        if(estado == 0)
                tareasLiveData.value = tareas.filter { it.estado == 0 } as ArrayList<Tarea>
        else if (estado == 1)
            tareasLiveData.value = tareas.filter { it.estado == 1 } as ArrayList<Tarea>
        else if (estado == 2)
            tareasLiveData.value = tareas.filter { it.estado == 2 } as ArrayList<Tarea>
        else
            tareasLiveData.value = tareas
        return tareasLiveData

    }

    /**
     * Varios filtros:
     */
    fun getTareasFiltroSinPagarEstado(soloSinPagar:Boolean, estado:Int):
            LiveData<List<Tarea>> {
        //devuelve el LiveData con la lista filtrada
        tareasLiveData.value=tareas.filter { !it.pagado && it.estado==estado } as ArrayList<Tarea>
        return tareasLiveData
    }



    /**
     * Crea unos Tareas de prueba de forma aleatoria.
     */
    suspend fun iniciaPruebaTareas() {
        val tecnicos = listOf(
            "Pepe Gotero",
            "Sacarino Pómez",
            "Mortadelo Fernández",
            "Filemón López",
            "Zipi Climent",
            "Zape Gómez"
        )
        lateinit var tarea: Tarea
        (1..10).forEach({
            tarea = Tarea(
                (0..4).random(),
                (0..2).random(),
                Random.nextBoolean(),
                (0..2).random(),
                (0..30).random(),
                (0..5).random().toFloat(),
                tecnicos.random(),
                "tarea $it realizada por el técnico \nLorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                        "Mauris consequat ligula et vehicula mattis."
            )
            tareas.add(tarea)
        })
        //actualizamos el LiveData
        //tareasLiveData.value = tareas
        tareasLiveData.postValue(tareas)
    }

}