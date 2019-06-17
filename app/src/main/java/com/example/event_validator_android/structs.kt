package com.example.event_validator_android

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
):Serializable

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

data class IntentData(
    var name                    :String                 = "",
    var value                   :Any                    = "",
    var type                    :String                 = "string"
)

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

class QRDataAdapter(private val items:MutableList<Item>, private val onClick: View.OnClickListener = View.OnClickListener {}): RecyclerView.Adapter<QRDataAdapter.QRDataViewHolder>(){

    class QRDataViewHolder(val rowView: View): RecyclerView.ViewHolder(rowView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QRDataViewHolder {
        var rowView             :View                   = View(parent.context)

        when (viewType){
            0 -> rowView                                = LayoutInflater.from(parent.context).inflate(R.layout.lv_section, parent, false) as View
            1 -> rowView                                = LayoutInflater.from(parent.context).inflate(R.layout.lv_basic, parent, false) as View
            2 -> rowView                                = LayoutInflater.from(parent.context).inflate(R.layout.lv_left_detail, parent, false) as View
        }

        return QRDataViewHolder(rowView)
    }

    override fun onBindViewHolder(holder: QRDataViewHolder, position: Int) {
        val rowView                                     = holder.rowView

        when (items[position].itemType){
            cellType.SECTION ->{
                rowView.findViewById<TextView>(R.id.lvSectionTxtSectionTitle).apply {
                    text                                = items[position].title
                }
            }
            cellType.BASIC ->{
                rowView.findViewById<TextView>(R.id.lvBasicTxtItemTitle).apply {
                    text                                = items[position].title
                }
            }
            cellType.LEFTDETAIL ->{
                val item        :LeftDetailItem         = items[position] as LeftDetailItem
                rowView.findViewById<TextView>(R.id.lvLeftDetailTxtDetail).apply {
                    text                                = item.detail
                }
                rowView.findViewById<TextView>(R.id.lvLeftDetailTxtTitle).apply {
                    text                                = item.title
                }
            }
        }
        rowView.setOnClickListener(onClick)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].itemType){
            cellType.SECTION                           -> 0
            cellType.BASIC                             -> 1
            cellType.LEFTDETAIL                        -> 2
        }
    }

    override fun getItemCount() = items.size

}