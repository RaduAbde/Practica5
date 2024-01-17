package ui

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import model.Tarea
import model.viewmodel.AppViewModel
import net.iessochoa.radwaneabdessamie.practica5.R
import net.iessochoa.radwaneabdessamie.practica5.databinding.FragmentTareaBinding
import ui.MainActivity.Companion.PREF_NOMBRE
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class TareaFragment : Fragment() {

    private var _binding: FragmentTareaBinding? = null
    val args: TareaFragmentArgs by navArgs()
    private val viewModel: AppViewModel by activityViewModels()
    //será una tarea nueva si no hay argumento
    val esNuevo by lazy { args.tarea==null }
    var uriFoto=""

    private val TAG = "Practica5"
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private fun saveBitmapImage(bitmap: Bitmap): Uri? {
        val timestamp = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        var uri: Uri? = null
        //Tell the media scanner about the new file so that it is immediately available to the user.
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, timestamp)
        //mayor o igual a version 29
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, timestamp)
            values.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/" + getString(R.string.app_name)
            )
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            uri = requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            if (uri != null) {
                try {
                    val outputStream = requireContext().contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        try {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.close()
                        } catch (e: Exception) {
                            Log.e(TAG, "saveBitmapImage: ", e)
                        }
                    }
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    requireContext().contentResolver.update(uri, values, null, null)
                    // Toast.makeText(requireContext(), "Saved...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(TAG, "saveBitmapImage: ", e)
                }
            }
        } else {//no me funciona bien en versiones inferiores a la 29(Android 10)
            val imageFileFolder = File(
                Environment.getExternalStorageDirectory()
                    .toString() + '/' + getString(R.string.app_name)
            )
            if (!imageFileFolder.exists()) {
                imageFileFolder.mkdirs()
            }
            val mImageName = "$timestamp.png"
            val imageFile = File(imageFileFolder, mImageName)
            try {
                val outputStream: OutputStream = FileOutputStream(imageFile)
                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    Log.e(TAG, "saveBitmapImage: ", e)
                }
                values.put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                requireContext().contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
                uri = imageFile.toUri()
                // Toast.makeText(requireContext(), "Saved...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "saveBitmapImage: ", e)
            }
        }
        return uri
    }

    fun loadFromUri(photoUri: Uri?): Bitmap? {
        var image: Bitmap? = null
        try {
            // check version of Android on device
            image = if (Build.VERSION.SDK_INT > 27) {
                // on newer versions of Android, use the new decodeBitmap method
                val source = ImageDecoder.createSource(
                    requireContext().contentResolver,
                    photoUri!!
                )
                ImageDecoder.decodeBitmap(source)
            } else {
                // support older versions of Android by using getBitmap
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver,
                    photoUri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }


    //petición de foto de la galería
    private val solicitudFotoGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //uri de la foto elegida
            val uri = result.data?.data
            //mostramos la foto
            binding.ivFoto.setImageURI(uri)
            //guardamos la uri
            uriFoto = uri.toString()
        }
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private fun iniciaTecnico(){
        //recuperamos las preferencias
        val sharedPreferences =

            PreferenceManager.getDefaultSharedPreferences(requireContext())
        //recuperamos el nombre del usuario
        val tecnico = sharedPreferences.getString(MainActivity.PREF_NOMBRE, "")
        //lo asignamos
        binding.etTecnico.setText(tecnico)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTareaBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //llamamos a las funciones que inician cada componente del fragment
        iniciaSpCategoria()
        iniciaSpnPrioridad()
        iniciaSwPagado()
        iniciaRgEstado()
        iniciaSbHoras()

        //si es nueva tarea o es una edicion
        if (esNuevo){//nueva tarea
        //cambiamos el título de la ventana
            (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.nueva_tarea)
            iniciaTecnico()
        }
        else
            iniciaTarea(args.tarea!!)

        iniciaFabGuardar()
        iniciaIvBuscarFoto()
        iniciafotoClick()

    }

    private fun iniciafotoClick(){
        binding.ivFoto.setOnClickListener{
            val action = TareaFragmentDirections.actionTareaFragmentToFragmentFoto(uriFoto)
            findNavController().navigate(action)
        }
    }

    /**
     * Carga los valores de la tarea a editar
     */
    private fun iniciaTarea(tarea: Tarea) {

        binding.spnCategoria.setSelection(tarea.categoria)
        binding.spnPrioridad.setSelection(tarea.prioridad)
        binding.swPagado.isChecked = tarea.pagado
        binding.rgEstado.check(
            when (tarea.estado) {
                0 -> R.id.rbAbierta
                1 -> R.id.rbEnCurso
                else -> R.id.rbCerrada
            }
        )
        binding.sbHoras.progress = tarea.horasTrabajo

        binding.rbValoracion.rating = tarea.valoracionCliente
        binding.etTecnico.setText(tarea.tecnico)
        binding.etDescripcion.setText(tarea.descripcion)
        if (!tarea.fotoUri.isNullOrEmpty())
            binding.ivFoto.setImageURI(tarea.fotoUri.toUri())
        uriFoto=tarea.fotoUri
        //cambiamos el título
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Tarea ${tarea.id}"
    }

    private fun iniciaFabGuardar(){
        binding.fabGuardar.setOnClickListener{
            if (binding.etTecnico.text.toString().isEmpty() || binding.etDescripcion.text.toString().isEmpty() )
                Snackbar.make(binding.root, getString(R.string.error_rellenar), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            else
                guardaTarea()
        }
    }

    private fun guardaTarea() {
        //recuperamos los datos
        val categoria=binding.spnCategoria.selectedItemPosition
        val prioridad=binding.spnPrioridad.selectedItemPosition
        val pagado=binding.swPagado.isChecked
        val estado=when (binding.rgEstado.checkedRadioButtonId) {
            R.id.rbAbierta -> 0
            R.id.rbEnCurso -> 1
            else -> 2
        }
        val horas=binding.sbHoras.progress
        val valoracion=binding.rbValoracion.rating
        val tecnico=binding.etTecnico.text.toString()
        val descripcion=binding.etDescripcion.text.toString()
        //creamos la tarea: si es nueva, generamos un id, en otro caso le  asignamos su id
        val tarea = if(esNuevo)

            Tarea(categoria,prioridad,pagado,estado,horas,valoracion,tecnico,descripcion,uriFoto)
        else

            Tarea(args.tarea!!.id,categoria,prioridad,pagado,estado,horas,valoracion,tecnico,descripcion,uriFoto)
        //guardamos la tarea desde el viewmodel
        viewModel.addTarea(tarea)
        //salimos de editarFragment
        findNavController().popBackStack()
    }


    private fun iniciaSbHoras() {
        //asignamos el evento
        binding.sbHoras.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, progreso: Int, p2: Boolean) {
                //Mostramos el progreso en el textview
                binding.tvHoras.text=getString(R.string.horas_trabajadas,progreso)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
        //inicio del progreso
        binding.sbHoras.progress=0
        binding.tvHoras.text=getString(R.string.horas_trabajadas,0)
    }

    private fun iniciaRgEstado() {
        //listener de radioGroup
        binding.rgEstado.setOnCheckedChangeListener { _, checkedId ->
            val imagen= when (checkedId){//el id del RadioButton seleccionado
                //id del cada RadioButon
                R.id.rbAbierta-> R.drawable.ic_abierta
                R.id.rbEnCurso->R.drawable.ic_en_curso
                else-> R.drawable.ic_cerrada
            }
            binding.ivEstado.setImageResource(imagen)
        }
        //iniciamos a abierto
        binding.rgEstado.check(R.id.rbAbierta)
    }



    private fun iniciaSwPagado() {
        binding.swPagado.setOnCheckedChangeListener { _, isChecked ->
            //cambiamos el icono si está marcado o no el switch
            val imagen=if (isChecked) R.drawable.ic_pagado
            else R.drawable.ic_no_pagado
            //asignamos la imagen desde recursos
            binding.ivPagado.setImageResource(imagen)
        }
        //iniciamos a valor false
        binding.swPagado.isChecked=false
        binding.ivPagado.setImageResource(R.drawable.ic_no_pagado)
    }

    private fun iniciaSpCategoria() {
        ArrayAdapter.createFromResource(
            //contexto suele ser la Activity
            requireContext(),
            //array de strings
            R.array.categoria,
            //layout para mostrar el elemento seleccionado
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Layout para mostrar la apariencia de la lista
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // asignamos el adaptador al spinner
            binding.spnCategoria.adapter = adapter
            binding.spnCategoria.onItemSelectedListener=object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, v: View?, posicion: Int, id: Long) {
                    //el array son 3 elementos y "alta" ocupa la tercera posición
                    //recuperamos el valor
                    val valor=binding.spnCategoria.getItemAtPosition(posicion)
                //creamos el mensaje desde el recurso string parametrizado
                    val mensaje=getString(R.string.mensaje_categoria,valor)
                //mostramos el mensaje donde "binding.root" es el ContrainLayout principal
                    Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()

                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}

            }
        }
    }





    fun explicarPermisos(){
        AlertDialog.Builder(activity as Context)
            .setTitle(android.R.string.dialog_alert_title)
            //TODO:recuerda: el texto en string.xml
            .setMessage(getString(R.string.mensaje_permisos))
        //acción si pulsa si
        .setPositiveButton(android.R.string.ok) { v, _ ->
            //Solicitamos los permisos de nuevo

            solicitudPermisosLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            //cerramos el dialogo
            v.dismiss()

        }
            //accion si pulsa no
            .setNegativeButton(android.R.string.cancel) { v, _ ->
                v.dismiss() }
            .setCancelable(false)
            .create()
            .show()
    }

    private val solicitudPermisosLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()

        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission has been granted.
                buscarFoto()
            } else {
                // Permission request was denied.
                explicarPermisos()
            }
        }

    private fun buscarFoto() {
        //Toast.makeText(requireContext(), "Buscando la foto...", Toast.LENGTH_SHORT).show()
        val intent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        solicitudFotoGallery.launch(intent)
    }

    fun iniciaIvBuscarFoto() {
        binding.ivBuscarFoto.setOnClickListener() {
            when {
                //si tenemos los permisos
                permisosAceptados() -> buscarFoto()
                //no tenemos los permisos y los solicitamos
                else ->
                    solicitudPermisosLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    fun permisosAceptados() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;


    private fun iniciaSpnPrioridad() {
        ArrayAdapter.createFromResource(
            //contexto suele ser la Activity
            requireContext(),
            //array de strings
            R.array.prioridad,
            //layout para mostrar el elemento seleccionado
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Layout para mostrar la apariencia de la lista
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // asignamos el adaptador al spinner
            binding.spnPrioridad.adapter = adapter
            binding.spnPrioridad.onItemSelectedListener=object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, v: View?, posicion: Int, id: Long) {
                    //el array son 3 elementos y "alta" ocupa la tercera posición
                    if(posicion==2){
                        //Cambiamos el color al seleccionado por el usuario
                        val colorPorDefecto=resources.getStringArray(R.array.color_values)[0]
                        val color= PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(MainActivity.PREF_COLOR_PRIORIDAD, colorPorDefecto)

                        binding.clytTarea.setBackgroundColor(Color.parseColor(color))
                        //binding.clytTarea.setBackgroundColor(requireContext().getColor(R.color.prioridad_alta))
                    }else{//si no es prioridad alta quitamos el color
                        binding.clytTarea.setBackgroundColor(Color.TRANSPARENT)
                    }

                }
                override fun onNothingSelected(p0: AdapterView<*>?) {
                    binding.clytTarea.setBackgroundColor(Color.TRANSPARENT)
                }

            }
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}