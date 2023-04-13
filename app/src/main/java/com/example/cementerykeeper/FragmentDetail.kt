package com.example.cementerykeeper

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class FragmentDetail(private val cemetery: Cementery) : Fragment() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var cemeteryName: TextView
    private lateinit var cemeteryLocation: TextView
    private lateinit var cemeteryDescription: TextView
    private lateinit var deleteButton: Button
    private lateinit var modifyButton: Button
    private lateinit var imageView: ImageView
    private lateinit var cemetery_scoreDetail: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currentUserEmail: String
    private lateinit var database: DatabaseReference



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_detail, container, false)

        databaseRef = FirebaseDatabase.getInstance().getReference("Cementeries")

        cemeteryName = view.findViewById(R.id.cementery_nameDetail)
        cemeteryLocation = view.findViewById(R.id.cementery_locationDetail)
        cemeteryDescription = view.findViewById(R.id.cementery_descriptionDetail)
        imageView = view.findViewById(R.id.cementery_imageDetail)
        cemetery_scoreDetail = view.findViewById(R.id.cemetery_scoreDetail)
        val imageUrl = cemetery.imageUrl


        val ratingBar = view.findViewById<RatingBar>(R.id.rating_barDetail)

        ratingBar.numStars = 5 // máximo valor posible de la puntuación

        val scoreListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val media = dataSnapshot.getValue(Int::class.java) ?: 0
                ratingBar.rating = media.toFloat() // establece el número de estrellas iluminadas
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar la puntuación", Toast.LENGTH_SHORT).show()
            }
        }

        databaseRef.child(cemetery.id).child("score").addValueEventListener(scoreListener)

        Glide.with(this)
            .load(imageUrl)
            .override(imageView.width, imageView.height)
            .fitCenter()
            .into(imageView)

        cemeteryName.text = cemetery.name
        cemeteryLocation.text = cemetery.location
        cemeteryDescription.text = cemetery.description
        cemetery_scoreDetail.text = cemetery.media.toString()

        deleteButton = view.findViewById(R.id.delete_buttonDetail)
        deleteButton.setOnClickListener {
            deleteCemetery()
        }

        modifyButton = view.findViewById(R.id.modify_buttonDetail)
        modifyButton.setOnClickListener {
            modifyCemetery()
        }

        sharedPreferences = requireContext().getSharedPreferences("user", Context.MODE_PRIVATE)
        currentUserEmail = sharedPreferences.getString("google_email", null) ?: ""

        return view
    }

    private fun deleteCemetery() {
        val cemeteryName = cemetery.name ?: return
        val user = cemetery.user
        if (user != currentUserEmail) {
            Toast.makeText(requireContext(), "No tienes permiso para eliminar esta imagen", Toast.LENGTH_SHORT).show()
            return
        }
        databaseRef.orderByChild("name").equalTo(cemeteryName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val cemeteryId = snapshot.children.firstOrNull()?.key ?: return
                    databaseRef.child(cemeteryId).removeValue()
                    //inicia la activity main
                    val mainActivity = activity as MainActivity
                    val intent = Intent(mainActivity, MainActivity::class.java)
                    mainActivity.startActivity(intent)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "loadCemetery:onCancelled", error.toException())
                    Toast.makeText(requireContext(), "Fallo al borrar el cementerio: ${error.message}", Toast.LENGTH_SHORT).show()

                }
            })
    }

    private fun modifyCemetery() {
        val name = cemeteryName.text.toString().trim()
        val location = cemeteryLocation.text.toString().trim()
        val description = cemeteryDescription.text.toString().trim()
        val imageUrl = cemetery.imageUrl
        val user = cemetery.user
        val id = cemetery.id

        if (user != currentUserEmail) {
            Toast.makeText(requireContext(), "No tienes permiso para modificar esta imagen", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isEmpty()) {
            cemeteryName.error = "Por favor ingresa un nombre"
            return
        }

        if (location.isEmpty()) {
            cemeteryLocation.error = "Por favor introduce una localización"
            return
        }

        if (description.isEmpty()) {
            cemeteryDescription.error = "Por favor ingrese una descripción"
            return
        }
        // Verificar si el nuevo nombre ya está en uso
        val query = databaseRef.orderByChild("name").equalTo(name)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Si existe un cementerio con el mismo nombre, mostrar un mensaje de error
                    cemeteryName.error = "Ya existe un cementerio con este nombre"
                    return
                } else {
                    // Si no hay cementerios con el mismo nombre, actualizar el cementerio actual
                    val updatedCemetery = Cementery(id, name, location, description, user, imageUrl, cemetery.score, cemetery.media)
                    Toast.makeText(requireContext(), "Cementerio modificado correctamente", Toast.LENGTH_SHORT).show()
                    databaseRef.child(id).setValue(updatedCemetery).addOnSuccessListener {
                        val fragmentManager = activity?.supportFragmentManager
                        val fragmentTransaction = fragmentManager?.beginTransaction()
                        val fragment = FragmentDetail(updatedCemetery)
                        fragmentTransaction?.replace(R.id.fragment_container, fragment)
                        fragmentTransaction?.commit()
                    }.addOnFailureListener {
                        Log.w(TAG, "loadCemetery:onCancelled", it)
                        Toast.makeText(requireContext(), "Fallo al modificar el cementerio: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "loadCemetery:onCancelled", error.toException())
                Toast.makeText(requireContext(), "Fallo al modificar el cementerio: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

}
