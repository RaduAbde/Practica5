package adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import model.Tarea
import net.iessochoa.radwaneabdessamie.practica5.R
import net.iessochoa.radwaneabdessamie.practica5.databinding.ItemTareaBinding

class TareaAdapter () :RecyclerView.Adapter<TareaAdapter.TareaViewHolder>(){
    var listaTareas: List<Tarea> ?= null
    var onTareaClickListener:OnTareaClickListener?=null

    fun setLista(lista:List<Tarea>){
        listaTareas=lista
        //notifica al adaptador que hay cambios y tiene que redibujar el ReciclerView
        notifyDataSetChanged()
    }

    inner class TareaViewHolder(val binding: ItemTareaBinding)
        : RecyclerView.ViewHolder(binding.root){
        init {
            //inicio del click de icono borrar
            binding.ivBorrarItem.setOnClickListener(){
                //recuperamos la tarea de la lista
                val tarea= listaTareas?.get(this.adapterPosition)
                //llamamos al evento borrar que estará definido en el fragment
                onTareaClickListener?.onTareaBorrarClick(tarea)
            }

            //inicio del click de icono estado
            binding.ivEstadoItem.setOnClickListener(){
                val tarea= listaTareas?.get(this.adapterPosition)
                //llamamos al evento que controla el estado que esta definido en el fragment
                onTareaClickListener?.onTareaEstadoClick(tarea)
                //Ponemos la imagen correspondiente a la tarea despues de cambiar el estado

                if (tarea != null) {
                    binding.ivEstadoItem.setImageResource(
                        when (tarea.estado- 1) {
                            0 -> R.drawable.ic_en_curso
                            1 -> R.drawable.ic_cerrada
                            else -> R.drawable.ic_abierta
                        }
                    )
                }



            }
            //inicio del click sobre el Layout(constraintlayout)
            binding.root.setOnClickListener(){
                val tarea= listaTareas?.get(this.adapterPosition)
                onTareaClickListener?.onTareaClick(tarea)
            }
        }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        //utilizamos binding, en otro caso hay que indicar el item.xml.
        // Para más detalles puedes verlo en la documentación
        val binding = ItemTareaBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return TareaViewHolder(binding)

    }

    override fun getItemCount(): Int = listaTareas?.size?:0

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        //Nos pasan la posición del item a mostrar en el viewHolder

        with(holder) {
            //cogemos la tarea a mostrar y rellenamos los campos del ViewHolder
            with(listaTareas!!.get(position)) {
                binding.tvIdItem.text = id.toString()
                binding.tvDescripcion.text = descripcion
                binding.tvTecnicoItem.text = tecnico
                binding.rbValoracionItem.rating = valoracionCliente
                //mostramos el icono en función del estado
                binding.ivEstadoItem.setImageResource(
                    when (estado) {
                        0 -> R.drawable.ic_abierta
                        1 -> R.drawable.ic_en_curso
                        else -> R.drawable.ic_cerrada
                    }
                )
                //cambiamos el color de fondo si la prioridad es alta
                binding.cvItem.setBackgroundResource(
                    if (prioridad == 2)//prioridad alta
                        R.color.prioridad_alta
                    else
                        Color.TRANSPARENT
                )
            }
        }
    }

    interface OnTareaClickListener{
        //editar tarea que contiene el ViewHolder
        fun onTareaClick(tarea:Tarea?)
        //borrar tarea que contiene el ViewHolder
        fun onTareaBorrarClick(tarea:Tarea?)
        //cambiar estado de tarea al hacer clic en el icono del estado
        fun onTareaEstadoClick(tarea: Tarea?)
    }

}