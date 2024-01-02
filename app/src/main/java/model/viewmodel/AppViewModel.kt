package model.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.Tarea
import repository.Repository

class AppViewModel(application: Application) : AndroidViewModel(application) {
    //repositorio
    private val repositorio: Repository
    //liveData de lista de tareas
    val tareasLiveData : LiveData<List<Tarea>>
    //creamos el LiveData de tipo Booleano. Representa nuestro filtro
    //private val soloSinPagarLiveData= MutableLiveData<Boolean>(false)

    //private val estadoTarea=MutableLiveData<Int>(3)

    val SOLO_SIN_PAGAR="SOLO_SIN_PAGAR"
    val ESTADO="ESTADO"
    val PRIORIDAD="PRIORIDAD"
    private val filtrosLiveData by lazy {//inicio tardío
        val mutableMap = mutableMapOf<String, Any?>(
            SOLO_SIN_PAGAR to false,
            ESTADO to 3,
            PRIORIDAD to 3
        )
        MutableLiveData(mutableMap)
    }

    //inicio ViewModel
    init {
        //inicia repositorio
        Repository(getApplication<Application>().applicationContext)
        repositorio=Repository
        //tareasLiveData=estadoTarea.switchMap { estado -> Repository.getTareasEstadoFitro(estado) }
        //tareasLiveData=soloSinPagarLiveData.switchMap {soloSinPagar->
           // Repository.getTareasFiltroSinPagar(soloSinPagar)}
        tareasLiveData=filtrosLiveData.switchMap{ mapFiltro ->
            val aplicarSinPagar = mapFiltro!![SOLO_SIN_PAGAR] as Boolean
            val estado = mapFiltro!![ESTADO] as Int
            val prioridad = mapFiltro!![PRIORIDAD] as Int
            //Devuelve el resultado del when
            when {//trae toda la lista de tareas
                (!aplicarSinPagar && (estado == 3) && prioridad == 3) ->
                    repositorio.getAllTareas()
                //Sólo filtra por ESTADO
                (!aplicarSinPagar && (estado != 3) && prioridad == 3) ->
                    repositorio.getTareasEstadoFitro(estado)
                //Sólo filtra SINPAGAR
                (aplicarSinPagar && (estado == 3) && prioridad == 3) ->
                    repositorio.getTareasFiltroSinPagar(
                        aplicarSinPagar
                    ) //Filtra por estado y sin pagar
                (aplicarSinPagar && (estado != 3) && prioridad == 3) ->
                    repositorio.getTareasFiltroSinPagarEstado(aplicarSinPagar, estado)
                //Filtra solo prioridad
                (!aplicarSinPagar && estado == 3 && prioridad != 3) ->
                    repositorio.getTareasFiltroPrioridad(prioridad)
                //Filtra por prioridad y sin pagar
                (aplicarSinPagar && estado == 3 && prioridad != 3) ->
                    repositorio.getTareasFiltroSinPagarPrioridad(aplicarSinPagar,prioridad)
                //Filtra por estado y prioridad
                (!aplicarSinPagar && estado !=3 && prioridad !=3) ->
                    repositorio.getTareasFiltroEstadoPrioridad(estado, prioridad)
                else ->
                    repositorio.getTareasFiltroSinPagarEstadoPrioridad(aplicarSinPagar,estado, prioridad)
            }
        }
    }
    //Lanzamos el añadido por corrutina
    fun addTarea(tarea: Tarea) = viewModelScope.launch(Dispatchers.IO){
        Repository.addTarea(tarea)}
    //Lanzamos el borrado por corrutina
    fun delTarea(tarea: Tarea) = viewModelScope.launch(Dispatchers.IO){
        Repository.delTarea(tarea)}

    /**
     * activa el LiveData del filtro
     */
    //fun setSoloSinPagar(soloSinPagar:Boolean){soloSinPagarLiveData.value=soloSinPagar}

    //fun setEstadoFiltro(estado:Int){estadoTarea.value=estado}

    /**
     * Modifica el Map filtrosLiveData el elemento "SOLO_SIN_PAGAR"
     * que activará el Transformations de TareasLiveData
     */
    fun setSoloSinPagarExtendido(soloSinPagar: Boolean) {
        //recuperamos el map
        val mapa = filtrosLiveData.value
        //modificamos el filtro
        mapa!![SOLO_SIN_PAGAR] = soloSinPagar
        //activamos el LiveData
        filtrosLiveData.value = mapa
    }
    /**
     * Modifica el Map filtrosLiveData el elemento "ESTADO"
     * que activará el Transformations de TareasLiveData lo
     *llamamos cuando cambia el RadioButton
     */
    fun setEstado(estado: Int) {
        //recuperamos el map
        val mapa = filtrosLiveData.value
        //modificamos el filtro
        mapa!![ESTADO] = estado
        //activamos el LiveData
        filtrosLiveData.value = mapa
    }

    fun setPrioridad(prioridad: Int){
        //recuperamos el map
        val mapa = filtrosLiveData.value
        //modificamos el filtro
        mapa!![PRIORIDAD] = prioridad
        //activamos el LiveData
        filtrosLiveData.value = mapa
    }

}