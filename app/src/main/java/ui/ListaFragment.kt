package ui

import adapters.TareaAdapter
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import model.Tarea
import model.viewmodel.AppViewModel
import net.iessochoa.radwaneabdessamie.practica5.R
import net.iessochoa.radwaneabdessamie.practica5.databinding.FragmentListaBinding
import net.iessochoa.radwaneabdessamie.practica5.databinding.ItemTareaBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ListaFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener
{

    private var _binding: FragmentListaBinding? = null
    private val viewModel: AppViewModel by activityViewModels()
    lateinit var tareasAdapter: TareaAdapter

    /**
     * Funcion para recuperar el color que a seleccionado el usuario
     */

      fun obtenColorPreferencias():Int{
        //cogemos el primer color si no hay ninguno seleccionado
        val colorPorDefecto=resources.getStringArray(R.array.color_values)[0]
        //recuperamos el color actual
        val color= PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(MainActivity.PREF_COLOR_PRIORIDAD, colorPorDefecto)
        return Color.parseColor(color)
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentListaBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iniciaRecyclerView()

        viewModel.tareasLiveData.observe(viewLifecycleOwner, Observer<List<Tarea>> { lista ->
            //actualizaLista(lista)
            tareasAdapter.setLista(lista)
        })




        //para prueba, editamos una tarea aleatoria
        /*binding.btPruebaEdicion.setOnClickListener{
        //cogemos la lista actual de Tareas que tenemos en el ViewModel. No es lo más correcto
            val lista= viewModel.tareasLiveData.value
            //buscamos una tarea aleatoriamente
            val tarea=lista?.get((0..lista.lastIndex).random())
            //se la enviamos a TareaFragment para su edición
            val action=ListaFragmentDirections.actionEditar(tarea)
            findNavController().navigate(action)
        }*/
        iniciaFiltros()
        iniciaFiltroEstado()
        iniciaCRUD()
        //Incicio el Spìnner de Prioridad
        iniciaSpPrioridad()
        binding.root.setOnApplyWindowInsetsListener { view, insets ->
            view.updatePadding(bottom = insets.systemWindowInsetBottom)
            insets
        }

    }

    private fun actualizaLista(lista: List<Tarea>?) {
        var listaString=""
        lista?.forEach(){
            listaString="$listaString ${it.id}-${it.tecnico}-${it.descripcion}-${if(it.pagado) "pagado" else
                "no pagado"}\n"
        }
        //binding.tvLista.setText(listaString)
    }

    //Funcion para inicar el filtro por pagado/sinpagar de tarea
    private fun iniciaFiltros(){
        binding.swSinPagar.setOnCheckedChangeListener( ) { _,isChecked->
            //actualiza el LiveData SoloSinPagarLiveData que a su vez modifica tareasLiveData
            //mediante el Transformation
            viewModel.setSoloSinPagarExtendido(isChecked)}
    }

    //Funcion para inicar el filtro por estado de tarea
    private fun iniciaFiltroEstado(){
        binding.rbFAbierta.setOnCheckedChangeListener(){_,isChecked ->
            if (isChecked)viewModel.setEstado(0)
        }
        binding.rbFEnCurso.setOnCheckedChangeListener(){_,isChecked ->
            if (isChecked) viewModel.setEstado(1)
        }
        binding.rbFCerrada.setOnCheckedChangeListener(){_,isChecked ->
            if (isChecked) viewModel.setEstado(2)
        }
        binding.rbFTodas.setOnCheckedChangeListener(){_,isChecked ->
            if (isChecked) viewModel.setEstado(3)
        }
    }

    private fun iniciaRecyclerView() {
        //creamos el adaptador
        tareasAdapter = TareaAdapter()
        //asignamos el color actual del item
        tareasAdapter.colorPrioridadAlta=obtenColorPreferencias()

        with(binding.rvTareas) {
            //Creamos el layoutManager
            //layoutManager = LinearLayoutManager(activity)
            val orientation=resources.configuration.orientation
            layoutManager =if(orientation== Configuration.ORIENTATION_PORTRAIT)
            //Vertical: lista con una colummna
                LinearLayoutManager(activity)
            else//Horizontal: lista con dos columnas
                GridLayoutManager(activity,2)
            //le asignamos el adaptador
            adapter = tareasAdapter
        }
        iniciaSwiped()
    }

    private fun iniciaCRUD(){
        binding.fabNuevo.setOnClickListener {
            //creamos acción enviamos argumento nulo porque queremos crear NuevaTarea
            val action=ListaFragmentDirections.actionEditar(null)
            findNavController().navigate(action)
        }

        tareasAdapter.onTareaClickListener = object :
            TareaAdapter.OnTareaClickListener {
            //**************Editar Tarea*************
            override fun onTareaClick(tarea: Tarea?) {
                //creamos la acción y enviamos como argumento la tarea para editarla
                val action = ListaFragmentDirections.actionEditar(tarea)
                findNavController().navigate(action)
            }
            //***********Borrar Tarea************
            override fun onTareaBorrarClick(tarea: Tarea?) {
                //borramos directamente la tarea
                //viewModel.delTarea(tarea!!)
                borrarTarea(tarea!!)
            }

            //Cambiar el estado de la tarea haciendo click en el icono
            override fun onTareaEstadoClick(tarea: Tarea?) {
                //estado(tarea!!)
                //Cambio el estado de la tarea al siguiente
                //abierta -> en curso -> cerrada -> abierta
                /*when (tarea?.estado){
                    0 -> tarea.estado= 1
                    1 -> tarea.estado= 2
                    else -> tarea?.estado=0
                }*/
                if (tarea != null){
                    //cambiamos el estado
                    var tareaActual = tarea.copy(estado = (tarea.estado + 1) % 3)
                    //la sustituyo
                    viewModel.addTarea(tareaActual)
                }
            }
        }

    }

    fun borrarTarea(tarea:Tarea){
        AlertDialog.Builder(activity as Context)
            .setTitle(android.R.string.dialog_alert_title)
            //recuerda: todo el texto en string.xml
            .setMessage(getString(R.string.desea_borrar) + "  ${tarea.id}?")
            //acción si pulsa si
            .setPositiveButton(android.R.string.ok){v,_->
                //borramos la tarea
                viewModel.delTarea(tarea)
                //cerramos el dialogo
                v.dismiss()
            }
            //accion si pulsa no
            .setNegativeButton(android.R.string.cancel){v,_->v.dismiss()}
            .setCancelable(false)
            .create()
            .show()
    }

    fun iniciaSwiped(){
        //creamos el evento del Swiper para detectar cuando el usuario desliza un item
        val itemTouchHelperCallback =
            object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or
                        ItemTouchHelper.RIGHT) {
                //si tenemos que actuar cuando se mueve un item

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                      direction: Int) {
                    //recuperamos la posicion del item que a sido deslizado
                    val posicion=viewHolder.adapterPosition

                    //obtenemos la posición de la tarea a partir del viewholder
                    val tareaDelete=tareasAdapter.listaTareas?.get(posicion)

                    //borramos la tarea. Falta preguntar al usuario si desea borrarla
                            if (tareaDelete != null) {
                                AlertDialog.Builder(activity as Context)
                                    .setTitle(android.R.string.dialog_alert_title)
                                    //recuerda: todo el texto en string.xml
                                    .setMessage(getString(R.string.desea_borrar) + "  ${tareaDelete.id}?")
                                    //acción si pulsa si
                                    .setPositiveButton(android.R.string.ok){v,_->
                                        //borramos la tarea
                                        viewModel.delTarea(tareaDelete)
                                        //cerramos el dialogo
                                        v.dismiss()
                                    }
                                    //accion si pulsa no
                                    .setNegativeButton(android.R.string.cancel){v,_->
                                        v.dismiss()
                                        //rellenamos el hueco en blanco que se queda al hacer swipe, dibujando el elemento con la propiedad posicion
                                        //recogida anteriormente
                                        tareasAdapter.notifyItemChanged(posicion)
                                    }
                                    .setCancelable(false)
                                    .create()
                                    .show()


                            }
                }
            }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        //asignamos el evento al RecyclerView
        itemTouchHelper.attachToRecyclerView(binding.rvTareas)
    }


    /**
     * Funcin iniciar el Spiner de ListaFragment
     */
    private fun iniciaSpPrioridad() {
        ArrayAdapter.createFromResource(
            //contexto suele ser la Activity
            requireContext(),
            //array de strings
            R.array.prioridadSpin,
            //layout para mostrar el elemento seleccionado
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Layout para mostrar la apariencia de la lista
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // asignamos el adaptador al spinner
            binding.spPrioridad.adapter = adapter

            binding.spPrioridad.onItemSelectedListener=object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, v: View?, posicion: Int, id: Long) {
                    //el array son 3 elementos y "alta" ocupa la tercera posición
                    if(posicion==0){
                        viewModel.setPrioridad(0)
                    }else if (posicion == 1){
                        viewModel.setPrioridad(1)
                    }else if (posicion == 2){
                        viewModel.setPrioridad(2)
                    }else{
                        viewModel.setPrioridad(3)
                    }
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {
                    viewModel.setPrioridad(3)
                }
            }
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Metodos de la interfaz SharedPreferences.OnSharedPreferenceChangeListener
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (key == MainActivity.PREF_COLOR_PRIORIDAD) {
                //si cambia el color, actualizamos la lista
                tareasAdapter.actualizaRecyclerColor(obtenColorPreferencias())
            }
    }

    override fun onResume() {
        super.onResume()

        PreferenceManager.getDefaultSharedPreferences(requireContext()).registerOnSharedPreferenceChangeListener(this)
    }
    override fun onPause() {
        super.onPause()

        PreferenceManager.getDefaultSharedPreferences(requireContext()).unregisterOnSharedPreferenceChangeListener(this)
    }

}