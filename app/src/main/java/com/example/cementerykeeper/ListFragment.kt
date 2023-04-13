package com.example.cementerykeeper
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cementerykeeper.ListFragment.companion.cementeryList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase


class ListFragment : Fragment() {

    object companion {
        var cementeryList: MutableList<Cementery> = mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = Firebase.database
        val recycler: RecyclerView = view.findViewById(R.id.recyclerList)

        database.getReference("Cementeries").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isAdded) {
                    // Limpiar la lista antes de agregar nuevos cementerios
                    cementeryList.clear()

                    for (snapshot:DataSnapshot in snapshot.getChildren()) {
                        val cementery: Cementery? = snapshot.getValue<Cementery>()
                        Log.i("FirebaseTest", cementery?.name.toString());

                        cementeryList.add(cementery!!)
                    }

                    val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
                    progressBar.visibility = View.GONE
                    val cementeryListAdapter = CementeryAdapter(companion.cementeryList)
                    recycler.adapter = cementeryListAdapter
                    recycler.layoutManager = LinearLayoutManager(context)
                    recycler.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseTest", "Fallo al leer el valor.", error.toException())
            }
        })
    }
}
