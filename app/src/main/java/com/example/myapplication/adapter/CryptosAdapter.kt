package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Crypto
import com.squareup.picasso.Picasso

class CryptosAdapter(val cryptosAdapterListener: CryptosAdapterListener):RecyclerView.Adapter<CryptosAdapter.ViewHolder>() {

    //Reutilizar cada una de las vistas o filas que tiene un componente
    class  ViewHolder(view: View):RecyclerView.ViewHolder(view){
        var image = view.findViewById<ImageView>(R.id.image)
        var name = view.findViewById<TextView>(R.id.nameTextView)
        var available = view.findViewById<TextView>(R.id.availableTextView)
        var buyBottom = view.findViewById<TextView>(R.id.buyButton)


    }
    var cryptoList:List<Crypto> = ArrayList()

    //Creamos el view holder basado en la lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Seleccionamos el valor de la vista que queremos inflar: se envia el recurso, la vista del padre y la variable
        //para que no lo agregue al padre
        val view = LayoutInflater.from(parent.context).inflate(R.layout.crypto_row, parent,false)
        return  ViewHolder(view)
    }
    //Hace la actualización de cada uno de los valores de la lista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Obtener la crypto en la posición que pasa el parámetro
        val crypto = cryptoList[position]
        //Obtenemos la imagen y lo cargamos
        Picasso.get().load(crypto.imageUrl).into(holder.image)
        holder.name.text = crypto.name
        //Asignamos el valor de una etiqueta del proyecto
        //Obtenemos el string del contexto que recibe como parametro: el ID del string y la cantidad
        holder.available.text = holder.itemView.context.getString(R.string.available_message, crypto.available.toString())
        holder.buyBottom.setOnClickListener {
            cryptosAdapterListener.onBuyCryptoClicked(crypto)
        }
    }
    //Retornamos cantidad
    override fun getItemCount(): Int {
        return cryptoList.size
    }
}