package com.example.cementerykeeper
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide


class CementeryAdapter(private val cemeteryList: List<Cementery>) : RecyclerView.Adapter<CementeryAdapter.CemeteryViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CemeteryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        Log.i("CementeryAdapter", "onCreateViewHolder called")
        return CemeteryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CemeteryViewHolder, position: Int) {
        val cemetery = cemeteryList[position]
        holder.bind(cemetery)


        holder.itemView.setOnClickListener {
            val activity = holder.itemView.context as AppCompatActivity
            activity.supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentDetail(cemetery)).commit()
        }
    }

    override fun getItemCount(): Int {
        return cemeteryList.size
    }

    inner class CemeteryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cemeteryName: TextView = itemView.findViewById(R.id.cementery_name)
        private val cemeteryLocation: TextView = itemView.findViewById(R.id.cementery_location)
        private val cemeteryDescription: TextView = itemView.findViewById(R.id.cementery_description)
        private val deleteButton: Button = itemView.findViewById(R.id.delete_button)
        private val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        private val ratingBar = itemView.findViewById<RatingBar>(R.id.ratingBar)
        private var media: Int= 0
        private var avg: Int = 0

        fun bind(cemetery: Cementery) {
            val imageUrl = cemetery.imageUrl
            cemeteryName.text = cemetery.name
            cemeteryLocation.text = cemetery.location
            cemeteryDescription.text = cemetery.description
            ratingBar.rating = cemetery.score.toFloat()
            media = avg

            ratingBar.max = 5
            ratingBar.rating = 3.5f
            ratingBar.rating = 0.toFloat()

            Glide.with(itemView.context)
                .load(imageUrl)
                .override(imageView.width, imageView.height)
                .into(imageView);


            ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                // Se ejecuta cuando el usuario cambia el valor de la valoración
                val activityContext = itemView.context
                Toast.makeText(activityContext, "Valoración seleccionada: $rating", Toast.LENGTH_SHORT).show()
                cemetery.score = rating.toInt()
                val sum = cemeteryList.fold(0f) { acc, cemetery -> acc + cemetery.score } // Suma de todas las valoraciones
                val avg = sum / cemeteryList.size // Valoración media
                media = avg.toInt() // Actualizar el valor de media en el objeto del adaptador

                // Update the cemetery object in the Firebase Realtime Database
                val databaseRef = FirebaseDatabase.getInstance().getReference("Cementeries")
                val query = databaseRef.orderByChild("name").equalTo(cemetery.name)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val cemeteryId = snapshot.children.first().key
                            cemetery.id = cemeteryId.toString()
                            cemetery.media = media // Actualizar el valor de media en el objeto Firebase
                            databaseRef.child(cemeteryId!!).setValue(cemetery)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("CementeryAdapter", "Error while searching for cemetery", error.toException())
                    }
                })
            }


            deleteButton.setOnClickListener {
                val databaseRef = FirebaseDatabase.getInstance().getReference("Cementeries")
                val cemeteryName = cemetery.name ?: return@setOnClickListener
                // Buscar el cementerio por su nombre
                databaseRef.orderByChild("name").equalTo(cemeteryName)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            // Obtener el ID del cementerio encontrado
                            val cemeteryId = snapshot.children.firstOrNull()?.key ?: return

                            // Eliminar el cementerio de la base de datos de Firebase
                            databaseRef.child(cemeteryId).removeValue()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("CementeryAdapter", "Error while searching for cemetery", error.toException())
                        }
                    })
            }
        }
    }
}
