package com.example.event_validator_android

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.io.Serializable
import java.math.BigInteger

data class QRData constructor(
    var e                       :String                 = "",                       // eventName
    var d                       :String                 = "",                       // date
    var w                       :String                 = "",                       // website
    var v                       :Double                 = 0.0,                      // QR version
    var l                       :Int                    = 0,                        // Prime number length
    var g                       :Int                    = 0,                        // Generator Index
    var p                       :Int                    = 0,                        // Prime Number Index
    var y                       :String                 = "",                       // Public Key
    var k                       :Int                    = 0,                        // Private Key
    var h                       :String                 = ""                        // Hash of unencrypted data
):Serializable {

}

data class QRSecrets constructor(
    var p                       :BigInteger             = BigInteger.ZERO,          // Prime
    var g                       :BigInteger             = BigInteger.ZERO,          // Generator
    var y                       :BigInteger             = BigInteger.ZERO,          // Public key
    var k                       :BigInteger             = BigInteger.ZERO,          // Private Key
    var s                       :BigInteger             = BigInteger.ZERO           // Shared Key
):Serializable {
    init{
        if(k != BigInteger.ZERO && y != BigInteger.ZERO && p != BigInteger.ZERO){
            s                                           = calculateSharedKey(k, y, p);
        }
    }
}

// For UI
interface Item{
    val title                   :String
    val itemType                :cellType
}

data class SectionItem(override var title: String): Item{
    override val itemType       :cellType
        get()                                           = cellType.SECTION
}

data class BasicItem(override var title: String): Item{
    override val itemType       :cellType
        get()                                           = cellType.BASIC
}

data class LeftDetailItem(override var title: String, var detail: String): Item{
    override val itemType       :cellType
        get()                                           = cellType.LEFTDETAIL
}

class QRViewAdapter(private val context: Activity, private val items: MutableList<Item>): BaseAdapter() {

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Item? {
        return items.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent:ViewGroup): View{
        val inflater = context.layoutInflater
        var rowView             :View

        when (items[position].itemType){
            cellType.SECTION ->{
                rowView                             = inflater.inflate(R.layout.lv_section, null, true)
                rowView.findViewById<TextView>(R.id.lvSectionTxtSectionTitle).apply {
                    text                            = items[position].title
                }
            }
            cellType.BASIC ->{
                rowView                             = inflater.inflate(R.layout.lv_basic, null, true)
                rowView.findViewById<TextView>(R.id.lvBasicTxtItemTitle).apply {
                    text                            = items[position].title
                }
            }
            cellType.LEFTDETAIL ->{
                val item    :LeftDetailItem         = items[position] as LeftDetailItem
                rowView                             = inflater.inflate(R.layout.lv_left_detail, null, true)
                rowView.findViewById<TextView>(R.id.lvLeftDetailTxtDetail).apply {
                    text                            = item.detail
                }
                rowView.findViewById<TextView>(R.id.lvLeftDetailTxtTitle).apply {
                    text                            = item.title
                }
            }
        }


        return rowView
    }
}