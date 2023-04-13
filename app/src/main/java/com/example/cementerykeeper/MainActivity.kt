package com.example.cementerykeeper
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var googleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //carga el fragmento topImagenFragment
        val topImagenFragment = TopImagenFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, topImagenFragment)
            .commit()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)

        // Agregar la Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val mainIntent = Intent(this@MainActivity, MainActivity::class.java)
                    startActivity(mainIntent)
                    true
                }
                else -> false
            }
        }

        // Cargar el fragmento NewCementeryFragment
        val newCementeryFragment = NewCementeryFragment()
        val listFragment = ListFragment()


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                googleSignInClient.signOut().addOnCompleteListener{
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)

                }
                true

            }
            R.id.cementeryListButton -> {
                val listFragment = ListFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, listFragment)
                    .commit()
                true
            }
            R.id.newCemeteryButton -> {
                val newCementeryFragment = NewCementeryFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, newCementeryFragment)
                    .commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
