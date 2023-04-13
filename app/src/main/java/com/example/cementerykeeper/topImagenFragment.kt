package com.example.cementerykeeper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class TopImagenFragment : Fragment() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var cemeteryName: TextView
    private lateinit var cemeteryLocation: TextView
    private lateinit var cemeteryDescription: TextView
    private lateinit var imageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_top_imagen, container, false)

        databaseRef = FirebaseDatabase.getInstance().getReference("Cementeries")
        cemeteryName = view.findViewById(R.id.cemeteryName)
        cemeteryLocation = view.findViewById(R.id.cemeteryLocation)
        cemeteryDescription = view.findViewById(R.id.cemeteryDescription)
        imageView = view.findViewById(R.id.imageView)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar3)

        // Consulta para obtener el objeto con la media más alta
        val query = databaseRef.orderByChild("media").limitToLast(1)

        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (child in dataSnapshot.children) {
                    val cemetery = child.getValue(Cementery::class.java)
                    // Actualizar los valores en la vista con los datos del cementerio obtenido
                    cemeteryName.text = cemetery?.name
                    cemeteryLocation.text = cemetery?.location
                    cemeteryDescription.text = cemetery?.description

                    Glide.with(requireContext())
                        .load(cemetery?.imageUrl)
                        .override(imageView.width, imageView.height)
                        .fitCenter()
                        .into(imageView)
                }
                progressBar.visibility = View.GONE
            }


            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar el cementerio", Toast.LENGTH_SHORT).show()
            }
        }

        query.addListenerForSingleValueEvent(listener)

        return view
    }
}
