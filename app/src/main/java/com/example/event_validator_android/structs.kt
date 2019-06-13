package com.example.event_validator_android

import android.app.Activity
import android.graphics.Typeface
import android.icu.text.CaseMap
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import java.io.Serializable
import java.math.BigInteger
import java.text.FieldPosition


data class QRData constructor(
    var e                       :String                 = "",                        // eventName
    var d                       :String                 = "",                        // date
    var w                       :String                 = "",                        // website
    var v                       :Double                 = 0.0,
    var l                       :Int                    = 0,
    var g                       :Int                    = 0,
    var p                       :Int                    = 0,
    var y                       :String                 = "",
    var k                       :Int                    = 0,
    var h                       :String                 = ""
):Serializable {

}

data class QRSecrets constructor(
    var p                       :BigInteger             = BigInteger.ZERO,
    var g                       :BigInteger             = BigInteger.ZERO,
    var y                       :BigInteger             = BigInteger.ZERO,
    var k                       :BigInteger             = BigInteger.ZERO,
    var s                       :BigInteger             = BigInteger.ZERO
):Serializable {
    init{
        if(k != BigInteger.ZERO && y != BigInteger.ZERO && p != BigInteger.ZERO){
            s                                           = calculateSharedKey(k, y, p);
        }
    }
}

// For UI
interface Item{
    val title: String
    val isSection: Boolean
}

data class SectionItem(var data: String): Item{
    override val title          :String                 = data
    override val isSection: Boolean
        get() = true
}

data class EntryItem(var data: String): Item{
    override val title          :String                 = data
    override val isSection: Boolean
        get() = false
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

        if(items[position].isSection){
            rowView                                     = inflater.inflate(R.layout.lv_section, null, true)
            rowView.findViewById<TextView>(R.id.txtSectionTitle).apply {
                text                                    = items[position].title
            }
        }else{
            rowView                                     = inflater.inflate(R.layout.lv_item, null, true)
            rowView.findViewById<TextView>(R.id.txtItemTitle).apply {
                text                                    = items[position].title
            }
        }


        return rowView
    }
}