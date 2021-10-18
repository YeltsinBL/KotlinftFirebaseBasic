package com.example.myapplication.adapter

import com.example.myapplication.model.Crypto

interface CryptosAdapterListener {
    fun onBuyCryptoClicked(crypto: Crypto)
}