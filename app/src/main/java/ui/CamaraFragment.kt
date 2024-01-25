package ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import net.iessochoa.radwaneabdessamie.practica5.databinding.FragmentCamaraBinding
import java.text.SimpleDateFormat
import java.util.Locale

class CamaraFragment : Fragment() {

    companion object{
        //creamos etiqueta para el Logcat
        private const val TAG = "Practica5_CameraX"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    }

    private var _binding: FragmentCamaraBinding? = null
    private val binding get() = _binding!!

    private var uriFoto: Uri?=null

    //camara
    private var imageCapture: ImageCapture? = null



    //Array con los permisos necesarios
    private val PERMISOS_REQUERIDOS =
        mutableListOf (
            Manifest.permission.CAMERA
        ).apply {
            //si la versión de Android es menor o igual a la 9 pedimos el permiso de escritura
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

    //Permite lanzar la solicitud de permisos al sistema operativo y actuar según el usuario
    //los acepte o no
    val solicitudPermisosLauncher = registerForActivityResult(
        //realizamos una solicitud de multiples permisos
        ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted: Map<String, Boolean> ->
        if (allPermissionsGranted()) {
            //Si tenemos los permisos, iniciamos la cámara
            startCamera()
        } else {
            // Si no tenemos los permisos. Explicamos al usuario
            explicarPermisos()
        }
    }

    fun explicarPermisos() {
        AlertDialog.Builder(requireContext())
            .setTitle(android.R.string.dialog_alert_title)
            //TODO:recuerda: el texto en string.xml
            .setMessage("Son necesarios los permisos para hacer una foto.\nDesea aceptar los permisos?")
        //acción si pulsa si
        .setPositiveButton(android.R.string.ok) { v, _ ->
            //Solicitamos los permisos de nuevo
            solicitudPermisosLauncher.launch(PERMISOS_REQUERIDOS)
            //cerramos el dialogo
            v.dismiss()
        }
            //accion si pulsa no
            .setNegativeButton(android.R.string.cancel) { v, _ ->
                v.dismiss()
                //cerramos el fragment

                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            .setCancelable(false)
            .create()
            .show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCamaraBinding.inflate(inflater,container,false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        if (allPermissionsGranted()) {
            startCamera()
            inicarBotonCamara()
        } else {
            solicitudPermisosLauncher.launch(PERMISOS_REQUERIDOS)
        }

    }

    private fun inicarBotonCamara(){
        binding.btCapturaFoto.setOnClickListener{
            takePhoto();
        }
    }

    //Metodo para comprobar si tenenmos los permisos necesarios
    private fun allPermissionsGranted() = PERMISOS_REQUERIDOS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) ==
                PackageManager.PERMISSION_GRANTED
    }

    //Metodo para iniciar la camara
    private fun startCamera() {
        //Se usa para vincular el ciclo de vida de las cámaras al propietario del ciclo de vida.
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        //Agrega un elemento Runnable como primer argumento
        cameraProviderFuture.addListener({
            // Esto se usa para vincular el ciclo de vida de nuestra cámara al LifecycleOwner dentro del proceso de la aplicación
            val cameraProvider: ProcessCameraProvider =
                cameraProviderFuture.get()
            //Inicializa nuestro objeto Preview,
            // llama a su compilación, obtén un proveedor de plataforma desde el visor y,
            // luego, configúralo en la vista previa.
            val preview = Preview.Builder()
                .build()
                .also {

                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().build()
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
//segundo argumento
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return
        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        // val name = "practica5_1"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            //funiona en versiones superiores a Android 9
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/”+ getString(R.string.app_name))")
            }
        }
        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireActivity().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()
        // Set up image capture listener, which is triggered after photo has
                // been taken
                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(requireContext()),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                        }
                        override fun onImageSaved(output:
                                                  ImageCapture.OutputFileResults) {

                            val msg = "Photo capture succeeded:${output.savedUri}"
                            Toast.makeText(requireContext(), msg,
                                Toast.LENGTH_SHORT).show()
                            Log.d(TAG, msg)
                            binding.ivMuestra.setImageURI(output.savedUri)
                            uriFoto=output.savedUri
                        }
                    }
                )
    }




}