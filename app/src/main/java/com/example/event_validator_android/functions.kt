package com.example.event_validator_android

import android.util.Log
import com.beust.klaxon.Klaxon
import java.lang.Exception
import java.math.BigInteger
import java.net.URLDecoder

fun textToQR(data: String): QRData{
    var qrData                              :QRData                     = QRData()
    try{
        qrData                                                          = Klaxon().parse<QRData>(data)!!
    }catch (e: Exception){
        Log.e(LOG_KEY, "Not our QR Code")
    }
    return qrData
}

fun base64ToNumber(base64: String):String{
    val rixits                                                          = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz+/"
    var result                                                          = ""

    for (rixit: Char in base64){
        result                                                         += rixits.indexOf(rixit).toString()
    }

    return result
}

// For Decryption

fun loadSecrets(data: QRData): QRSecrets{
    var prime                               :String
    var generator                           :String
    var privateKey                          :String
    var startIndex                          :Int

    when(data.l){
        10 -> prime                                                     = BuildConfig.P_10.split(";")[data.p]
        else -> prime                                                   = BuildConfig.P_10.split(";")[data.p]
    }

    startIndex                                                          = prime.indexOf("[")
    generator                                                           = prime.substring((startIndex+1), (prime.length-1))
    generator                                                           = generator.split(")")[data.g]
    prime                                                               = prime.substring(0, startIndex)

    startIndex                                                          = generator.indexOf("(")
    privateKey                                                          = generator.substring((startIndex+1))
    privateKey                                                          = privateKey.split(",")[data.k]
    generator                                                           = generator.substring(0, startIndex)

    return QRSecrets(prime.toBigInteger(), generator.toBigInteger(), data.y.toBigInteger(), privateKey.toBigInteger())
}

fun calculateSharedKey(privateKey: BigInteger, publicKey: BigInteger, prime: BigInteger): BigInteger{
    return publicKey.modPow(privateKey, prime)
}

fun decryptData(cipherText: String, secrets: QRSecrets, primeLength: Int): String{
    var encryptedParts                      :MutableList<String>        = cipherText.split(",").toMutableList()
    var decryptedString                     :String                     = ""
    val mLength                             :Int                        = primeLength - 1
    val lastIndex                           :Int                        = encryptedParts.count() - 1
    val lastPartLength                      :Int                        = encryptedParts[lastIndex].substring(encryptedParts[lastIndex].indexOf(";")+1).toInt()
    encryptedParts[lastIndex]                                           = encryptedParts[lastIndex].substring(0, encryptedParts[lastIndex].indexOf(";"))

    for ((index, encryptedPart) in encryptedParts.withIndex()){
        var tmp                                                         = base64ToNumber(encryptedPart)
        var tmpNo                                                       = tmp.toBigInteger()
        tmpNo                                                           = tmpNo * secrets.s.modInverse(secrets.p)
        tmpNo                                                           = tmpNo % secrets.p
        tmp                                                             = tmpNo.toString()

        val stringLength                                                = if (index == lastIndex) lastPartLength else mLength

        while (tmp.length < stringLength) tmp                           = "0${tmp}"
        decryptedString                                                 = "${decryptedString}${tmp}"
    }

    return decodeText(decryptedString)
}

fun decodeText(data: String):String{
    var str                                 :String                     = ""

    for (i in 0 until data.length step 4){
        val text                            :String                     = "${data[i]}${data[i+1]}".toInt().toString(16) + "${data[i+2]}${data[i+3]}".toInt().toString(16)
        str                                                             = "${str}${text.toInt(16).toChar()}"
    }

    return URLDecoder.decode(str, "UTF-8")
}