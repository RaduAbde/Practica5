package ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import net.iessochoa.radwaneabdessamie.practica5.R
import net.iessochoa.radwaneabdessamie.practica5.databinding.FragmentFotoBinding
import net.iessochoa.radwaneabdessamie.practica5.databinding.FragmentTareaBinding


class fragment_foto : Fragment() {
    private var _binding: FragmentFotoBinding? = null
    val args: fragment_fotoArgs by navArgs()
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Foto"
        ponerImagen()


    }

    private fun ponerImagen(){
        /*Toast.makeText(activity, args.uriFoto, Toast.LENGTH_SHORT).show()
        val uri = Uri.parse(args.uriFoto)
        println(args.uriFoto)*/
        binding.ivFoto.setImageURI(args.uriFoto.toUri());
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFotoBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_foto, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}