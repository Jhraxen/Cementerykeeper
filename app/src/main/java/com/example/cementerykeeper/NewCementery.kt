    package com.example.cementerykeeper

    import android.Manifest
    import android.app.Activity
    import android.content.Context
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.graphics.Bitmap
    import android.location.Address
    import android.location.Geocoder
    import android.os.Bundle
    import android.provider.MediaStore.ACTION_IMAGE_CAPTURE
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.*
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat
    import androidx.fragment.app.Fragment
    import com.google.android.gms.location.FusedLocationProviderClient
    import com.google.android.gms.location.LocationServices
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseError
    import com.google.firebase.database.DatabaseReference
    import com.google.firebase.database.ValueEventListener
    import com.google.firebase.database.ktx.database
    import com.google.firebase.ktx.Firebase
    import com.google.firebase.storage.FirebaseStorage
    import java.io.ByteArrayOutputStream
    import java.util.*


    // TODO: Rename parameter arguments, choose names that match

    private const val ARG_PARAM1 = "param1"
    private const val ARG_PARAM2 = "param2"

    /**
     * A simple [Fragment] subclass.
     * Use the [NewCementery.newInstance] factory method to
     * create an instance of this fragment.
     */
    @Suppress("DEPRECATION")
    class NewCementeryFragment : Fragment() {

        private lateinit var storage: FirebaseStorage
        private lateinit var cementeryNameEditText: EditText
        private lateinit var locationTextView: TextView
        private lateinit var cementeryDescriptionEditText: EditText
        private val imageView: ImageView? = null
        private lateinit var sendButton: Button
        private lateinit var activityContext: Context
        private lateinit var database: DatabaseReference
        private val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private val EXTERNAL_PERMISSION_REQUEST_CODE = 1002
        private lateinit var capturedImage: Bitmap
        private var imageUrl: String = ""
        private var longitude: Double = 0.0
        private var latitude: Double = 0.0
        private val LOCATION_PERMISSION_REQUEST_CODE = 1

        private var fusedLocationClient: FusedLocationProviderClient? = null
        private var user: String = ""

        companion object {
            private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
            private const val EXTERNAL_PERMISSION_REQUEST_CODE = 1002
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            // Almacenar referencia al contexto
            activityContext = context
        }


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            storage = FirebaseStorage.getInstance()

            // Obtener referencia de la base de datos
            database = Firebase.database.reference
        }


        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                val view = inflater.inflate(R.layout.newcementery_fragment, container, false)

            // Obtener referencias a los elementos de la vista
            cementeryNameEditText = view.findViewById(R.id.cementeryName)
            locationTextView = view.findViewById(R.id.location)
            cementeryDescriptionEditText = view.findViewById(R.id.cementeryDescription)
            sendButton = view.findViewById(R.id.send)
            val btnCamera: Button = view.findViewById(R.id.subir)
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val progressBar: ProgressBar = view.findViewById(R.id.progressBar3)
            val sharedPreferences = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE)
            val user = sharedPreferences.getString("google_email", "")

            progressBar.visibility = View.GONE


            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), LOCATION_PERMISSION_REQUEST_CODE
                )
                return view
            }

            fusedLocationClient!!.lastLocation
                .addOnSuccessListener(requireActivity()) { location ->
                    if (location != null) {
                        // definimos longitud y latitud
                        val latitude = location.latitude
                        val longitude = location.longitude

                        // Obtiene la dirección a partir de las coordenadas
                        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                        val address = addresses!![0].thoroughfare ?: ""
                        val streetNumber = addresses!![0].subThoroughfare ?: ""
                        val city = addresses!![0].locality
                        val state = addresses!![0].adminArea
                        val country = addresses!![0].countryName
                        val postalCode = addresses!![0].postalCode

                        // Show the location in the TextView
                        locationTextView.setText("$address, $streetNumber, $city ,$state ,$country")
                    } else {
                        locationTextView.setText("Ubicación no encontrada.")
                    }
                }


            sendButton.setOnClickListener {

                progressBar.visibility = View.VISIBLE
                val cementery = Cementery(
                    id = "",
                    name = cementeryNameEditText.text.toString(),
                    location = locationTextView.text.toString(),
                    description = cementeryDescriptionEditText.text.toString(),
                    user = user.toString(),
                    imageUrl = imageUrl,
                    score = 0,
                    media = 0
                )
                val filename = "${UUID.randomUUID()}.jpg"
                val ref = storage.reference.child("images/$filename")


                val baos = ByteArrayOutputStream()
                capturedImage.compress(Bitmap.CompressFormat.PNG, 100, baos)


                val data = baos.toByteArray()
                ref.putBytes(data).addOnSuccessListener {
                    // obtenemose la url de la imagen
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        cementery.imageUrl = uri.toString()

                        // Añadimos el cementerio a la base de datos
                        val query = database.child("Cementeries").orderByChild("name").equalTo(cementeryNameEditText.text.toString())

                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    // muestra el mensaje de que ya existe una imagen con ese nombre
                                    Toast.makeText(requireContext(), "Ya existe una imagen con ese nombre", Toast.LENGTH_SHORT).show()
                                    progressBar.visibility = View.GONE
                                } else {
                                    database.child("Cementeries").push().setValue(cementery)
                                        .addOnSuccessListener {
                                            // muestra el mensaje con éxito
                                            Toast.makeText(requireContext(), "Se ha añadido el cementerio con éxito", Toast.LENGTH_SHORT).show()
                                            progressBar.visibility = View.GONE
                                        }
                                        .addOnFailureListener {
                                            // muestra el error
                                            Toast.makeText(requireContext(), "No se ha podido añadir el cementerio", Toast.LENGTH_SHORT).show()
                                            progressBar.visibility = View.GONE
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(requireContext(), "Error al comprobar el nombre en la base de datos", Toast.LENGTH_SHORT).show()
                                progressBar.visibility = View.GONE
                            }
                        })


                        // limpia los campos
                        cementeryNameEditText.text.clear()
                        cementeryDescriptionEditText.text.clear()
                        this.imageView?.setImageDrawable(null)

                    }
                }
            }


            btnCamera.setOnClickListener {
                Toast.makeText(context, "Se ha añadido la imagen con éxito", Toast.LENGTH_SHORT).show()
                // Si els permisos de càmera no estan validats
                if (!isCameraPermissionGranted()) {
                    // Farem una petició de permisos
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(android.Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_REQUEST_CODE
                    )

                } else {
                    // Sinó farem l'intent de mostrar la càmera
                    cameraResult.launch(Intent(ACTION_IMAGE_CAPTURE));
                }
            }

            return view
        }
        private val cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                capturedImage = result.data?.extras?.get("data") as Bitmap

                // Mostrar la imagen en el ImageView
                val imageView = view?.findViewById<ImageView>(R.id.imageView)
                imageView?.setImageBitmap(capturedImage)

            }
        }

        // Permisos camera photo
        private fun isCameraPermissionGranted(): Boolean {
            return ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        }


        // Resposta a l'acció de l'usuari en validar o no els permisos
        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with opening camera
                } else {
                    // Permission denied, handle accordingly
                }
            } else if(requestCode == EXTERNAL_PERMISSION_REQUEST_CODE){
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with opening camera
                } else {
                    // Permission denied, handle accordingly
                }
            } else if(requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso concedido, obtener la ubicación actual
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationClient!!.lastLocation
                            .addOnSuccessListener(
                                requireActivity()
                            ) { location ->
                                if (location != null) {
                                    // Mostrar la ubicación en la TextView
                                    //guarda latitude, longitude en dos variables
                                        latitude = location.latitude
                                        longitude = location.longitude

                                } else {
                                    locationTextView.setText("Ubicación no encontrada.")
                                }
                            }
                    } else {
                        locationTextView.setText("Permiso de ubicación denegado.")
                    }
                } else {
                    // Permiso denegado, mostrar mensaje de error
                    locationTextView.setText("Permiso de ubicación denegado.")
                }
            }
        }
    }
