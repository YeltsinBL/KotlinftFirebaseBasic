package com.example.myapplication
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.adapter.CryptosAdapter
import com.example.myapplication.adapter.CryptosAdapterListener
import com.example.myapplication.model.Crypto
import com.example.myapplication.network.Callback
import com.example.myapplication.network.FirestoreService
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.model.User
import com.example.myapplication.network.RealtimeDataListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso

class TraderActivity : AppCompatActivity(), CryptosAdapterListener {

    //Definimos una instancia de FirestoreService
    lateinit var firestoreService: FirestoreService

    private val cryptoAdapter: CryptosAdapter = CryptosAdapter(this)

    //Definir una variable con el username
    private var username:String?=null
    //Variable para asignar el usuario que traemos del servidor
    private var user :User?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trader)

        val btnFab = findViewById<FloatingActionButton>(R.id.fab)

        //Le pasamos una instancia para la lectura de las cryptos
        firestoreService = FirestoreService(FirebaseFirestore.getInstance())

        username = intent.extras?.get(USERNAME_KEY)?.toString()

        val recyclerView: RecyclerView=findViewById(R.id.recyclerView)
        //configiramos el layout para las cryptos
        configureRecycleView(recyclerView)
        //Cargar las cryptos
        loadCryptos(btnFab)

        btnFab.setOnClickListener { view ->
            Snackbar.make(view, getString(R.string.generating_new_cryptos), Snackbar.LENGTH_SHORT)
                .setAction("Info", null).show()
            //Generar Cryptos Random
            generateCryptoCurrenciesRandom()

        }


        //val mensaje = intent.getStringExtra(USERNAME_KEY)
        val textView = findViewById<TextView>(R.id.usernameTextView)
        textView.text = username

    }

    private fun generateCryptoCurrenciesRandom() {
        for(crypto in cryptoAdapter.cryptoList){
            //generar una cantidad aleatoria para cada crypto
            val amount = (1..10).random()
            crypto.available += amount
            firestoreService.updateCrypto(crypto)
            //sin el notifyDataSetChanged solo me actualizar el primer registro del recyclerview
            cryptoAdapter.notifyDataSetChanged()
        }
    }

    private fun loadCryptos(btnFab: FloatingActionButton) {
        firestoreService.getCryptos(object : Callback<List<Crypto>>{
            override fun onSuccess(listCrypto: List<Crypto>?) {

                //buscar si existe el usuario
                username?.let { name ->
                    firestoreService.findUserById(name , object: Callback<User>{
                        override fun onSuccess(result: User?) {
                            user = result
                            //Verificamos si el usuario tiene listas de cryptos
                            if(user?.cryptosList == null){
                                //Lista mutable porque se va a modificar
                                //val userCryptoList = mutableListOf<Crypto>()
                                user?.cryptosList=listCrypto
                                user?.let { firestoreService.updateUser(it, null) }
                            }
                            //Actualizamos las cryptos del usuario
                            loadUserCryptos()
                            user?.let { user ->
                                listCrypto?.let { cryptoList ->
                                    addRealtimeDatebaseListener(user, cryptoList, btnFab)
                                }
                            }

                        }

                        override fun onFailed(exception: Exception) {
                            showGeneralServerErrorMessage(btnFab)
                        }

                    })
                }

                //Actualizamos la interfaz grafica
                this@TraderActivity.runOnUiThread {
                    if (listCrypto != null) {
                        cryptoAdapter.cryptoList = listCrypto
                        //Actualiza la vista
                        cryptoAdapter.notifyDataSetChanged()
                    }
                }

            }

            override fun onFailed(exception: Exception) {
                Log.e("TraderActivity","Error carga crypto", exception)
                showGeneralServerErrorMessage(btnFab)
            }

        })
    }

    private fun addRealtimeDatebaseListener(user: User, cryptosList: List<Crypto>, btnFab: FloatingActionButton) {
        firestoreService.ListenerForUpdate(user, object : RealtimeDataListener<User>{
            override fun onDataChange(updatedData: User) {
                this@TraderActivity.user = updatedData
                loadUserCryptos()
            }

            override fun onError(exception: Exception) {
                showGeneralServerErrorMessage(btnFab)
            }

        })
        firestoreService.listenForUpdates(cryptosList, object : RealtimeDataListener<Crypto>{
            override fun onDataChange(updatedData: Crypto) {
                //buscamos y actualizamos la crypto
                var pos =0
                for (crypto in cryptoAdapter.cryptoList){
                    if(crypto.name == updatedData.name){
                        //Actualizar la cantidad disponible
                        crypto.available = updatedData.available
                        //notificamos al adaptador que actualice el item para la posicion específica
                        cryptoAdapter.notifyItemChanged(pos)
                        pos++
                    }
                }

            }

            override fun onError(exception: Exception) {
                showGeneralServerErrorMessage(btnFab)
            }

        })
    }

    private fun loadUserCryptos() {
        runOnUiThread {
            if (user != null && user?.cryptosList != null ){

                //Limpiamos la vista
                val infoPanelRow = findViewById<LinearLayout>(R.id.infoPanel)
                infoPanelRow.removeAllViews()
                for (crypto in user?.cryptosList!!){
                    addUserCryptoInfoRow(crypto, infoPanelRow)
                }

            }
        }
    }

    private fun addUserCryptoInfoRow(crypto: Crypto, infoPanelRow: LinearLayout) {
        //Inflamos el Layout creado
        val view = LayoutInflater.from(this).inflate(R.layout.coin_info, infoPanelRow, false)
        view.findViewById<TextView>(R.id.coinLabel).text =
            getString(R.string.coin_info, crypto.name, crypto.available.toString())
        Picasso.get().load(crypto.imageUrl).into(view.findViewById<ImageView>(R.id.coinIcon))
        infoPanelRow.addView(view)
    }

    private fun configureRecycleView(recyclerView: RecyclerView) {
        recyclerView.setHasFixedSize(true)
        val layoutManager=LinearLayoutManager(this)
        recyclerView.layoutManager= layoutManager
        recyclerView.adapter = cryptoAdapter
    }

    fun showGeneralServerErrorMessage(btnFab: FloatingActionButton) {
        Snackbar.make(btnFab, getString(R.string.error_while_connecting_to_the_server), Snackbar.LENGTH_LONG)
            .setAction("Info", null).show()
    }

    //Cuando le demos clic a la compra de cryptos
    override fun onBuyCryptoClicked(crypto: Crypto) {
        if (crypto.available >0){
            for(userCrypto in user?.cryptosList!!){
                if(userCrypto.name == crypto.name){
                    userCrypto.available+=1
                    break
                }
            }
            crypto.available--
            firestoreService.updateUser(user!!, null)
            firestoreService.updateCrypto(crypto)

        }
    }
}