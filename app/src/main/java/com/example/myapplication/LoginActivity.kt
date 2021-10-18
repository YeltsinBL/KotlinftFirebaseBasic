package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.model.User
import com.example.myapplication.network.Callback
import com.example.myapplication.network.FirestoreService
import com.example.myapplication.network.USER_COLLECTION_NAME
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

const val USERNAME_KEY = "username_key"

class LoginActivity : AppCompatActivity() {


    private val TAG = "LoginActivity"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    //referencia al servicio creado
    lateinit var firestoreService: FirestoreService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Instanciar el firestoreService
        firestoreService = FirestoreService(FirebaseFirestore.getInstance())

    }


    fun onStartClicked(view: View) {
        //Bloquear para que no le de varias veces al boton
        view.isEnabled = false
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val editText = findViewById<EditText>(R.id.txtName)
                    val a = editText.text.toString()
                    //Encontrar los usuarios si existen o no
                    firestoreService.findUserById(a, object: Callback<User>{
                        override fun onSuccess(result: User?) {
                            if (result== null){
                                val user = User()
                                user.username = a
                                saveUserAndStartMainActivity(user, view)
                            }else{
                                startMainActivity(a)
                            }
                        }

                        override fun onFailed(exception: Exception) {
                            TODO("Not yet implemented")
                        }

                    })

                }else{
                    showErrorMessage(view)
                    //Desbloquear para que no le de varias veces al boton
                    view.isEnabled = true
                }
            }

    }

    private fun saveUserAndStartMainActivity(user: User, view: View) {
        firestoreService.setDocument(user, USER_COLLECTION_NAME, user.username, object : Callback<Void>{
            override fun onSuccess(result: Void?) {
                startMainActivity(user.username)
            }

            override fun onFailed(exception: Exception) {
                showErrorMessage(view)
                Log.e(TAG,"error", exception)
            }

        })
    }

    private fun showErrorMessage(view: View) {
        Snackbar.make(view, getString(R.string.error_while_connecting_to_the_server), Snackbar.LENGTH_LONG)
            .setAction("Info", null).show()
    }

    private fun startMainActivity(username: String) {
        val intent = Intent(this, TraderActivity::class.java)
            .apply { putExtra(USERNAME_KEY, username) }
        //intent.
        startActivity(intent)
        finish()
    }

}
