package com.example.myapplication.network

import com.example.myapplication.model.Crypto
import com.example.myapplication.model.User
import com.google.firebase.firestore.FirebaseFirestore

const val  CRYPTO_COLLECTION_NAME = "cryptos"
const val  USER_COLLECTION_NAME = "users"

class FirestoreService (val firebaseFirestore: FirebaseFirestore){
    //Guarda el documento con: parámetro de cualquier dato, nombre de la coleccion,
    //el id y el callback para saber si fue exitosa o no
    fun setDocument(data:Any, collectionName:String, id: String, callback: Callback<Void>){
        firebaseFirestore.collection(collectionName).document(id).set(data)
            .addOnSuccessListener { callback.onSuccess(null) }
            .addOnFailureListener{ exception -> callback.onFailed(exception)}
    }

    //Actualizar el usuario
    fun updateUser(user: User, callback: Callback<User>?){
        firebaseFirestore.collection(USER_COLLECTION_NAME).document(user.username)
            .update("cryptosList", user.cryptosList)
            .addOnSuccessListener {
                callback?.onSuccess(user)
            }
            .addOnFailureListener { exception -> callback?.onFailed(exception) }
    }

    //Actualizar la cantidad de cryptos
    fun updateCrypto(crypto: Crypto){
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME).document(crypto.getDocumentId())
            .update("available",  crypto.available)
    }

    //Listar Cryptos - Lectura
    fun getCryptos(callback: Callback<List<Crypto>>?){
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME)
            .get()
            .addOnSuccessListener { result ->
                for (document in result){
                    //Convertimos el resultado al objeto Crypto
                    //Lista de objetos
                    val cryptoList = result.toObjects(Crypto::class.java)
                    callback?.onSuccess(cryptoList)
                    break
                }
            }
            .addOnFailureListener { exception -> callback?.onFailed(exception) }
    }

    //Encontrar usuario de acuerdo al Id
    fun findUserById(id:String, callback: Callback<User>){
        firebaseFirestore.collection(USER_COLLECTION_NAME).document(id)
            .get()
            .addOnSuccessListener { result ->
                if(result.data != null){
                    callback.onSuccess(result.toObject(User::class.java))
                }else{
                    callback.onSuccess(null)
                }
            }
            .addOnFailureListener { exception -> callback.onFailed(exception) }
    }

    //Actualizamos la lista de las Cryptos
    fun listenForUpdates(cryptos: List<Crypto>, listener: RealtimeDataListener<Crypto>){
        val cryptoReference = firebaseFirestore.collection(CRYPTO_COLLECTION_NAME)
        for (crypto in cryptos){
            //Este metodo nos permite realizar lecturas suscribiendo a cambios en el nodo
            //retorno 2 parametros: la instancia de la data y un error
            cryptoReference.document(crypto.getDocumentId()).addSnapshotListener { snapshot, error ->
                if (error != null){
                    listener.onError(error)
                }
                //Verificamos que contenga datos
                if (snapshot != null &&  snapshot.exists()){
                    //Actualizamos la lista
                    snapshot.toObject(Crypto::class.java)?.let { listener.onDataChange(it) }
                }
            }
        }
    }
    //Actualizamos la lista por Usuario
    fun ListenerForUpdate(user: User, listener: RealtimeDataListener<User>){
        val userReference = firebaseFirestore.collection(USER_COLLECTION_NAME)

        userReference.document(user.username).addSnapshotListener { snapshot, error ->
            if (error != null){
                listener.onError(error)
            }
            //Verificamos que contenga datos
            if (snapshot != null &&  snapshot.exists()){
                //Actualizamos la lista
                snapshot.toObject(User::class.java)?.let { listener.onDataChange(it) }
            }
        }

    }


}