package com.example.event_validator_android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class CorrectQR : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_correct_qr)

        val qrData                                                          = intent.getSerializableExtra(INT_QRDATA) as QRData
        var qrSecrets                                                       = intent.getSerializableExtra(INT_QRSECRETS) as QRSecrets
        val shortSDF                                                        = SimpleDateFormat("yyyy/MM/dd");
        val fullSDF                                                         = SimpleDateFormat("EEEE, dd MMMM yyyy");
        var eventToday                                                      = false
        var listData                            :MutableList<Item>          = listOf<Item>().toMutableList()

        findViewById<TextView>(R.id.txtEventName).apply {
            text                                                            = decryptData(qrData.e, qrSecrets, qrData.l)
        }
        decryptData(qrData.d, qrSecrets, qrData.l).also{ date ->
            val now                             :String                     = shortSDF.format(Date()).toString()
            val dateIsList                                                  = date.indexOf("-") == -1
            val tmpDates                                                    = if (dateIsList) date.split(",") else date.split("-")

            listData.add(SectionItem(if (dateIsList) "List of Dates" else "Duration"))
            if(dateIsList){
                for (currentDate in tmpDates){
                    if (currentDate == now) eventToday                      = true
                    listData.add(EntryItem(fullSDF.format(shortSDF.parse(currentDate)!!).toString()))
                }
            } else {
                val tmpNow                                                  = Date()
                eventToday                                                  = tmpNow.compareTo(shortSDF.parse(tmpDates[0])) > 0 && tmpNow.compareTo(shortSDF.parse(tmpDates[1])) < 1
                listData.add(EntryItem("${"From"} ${fullSDF.format(shortSDF.parse(tmpDates[0])!!)}"))
                listData.add(EntryItem("${"To"} ${fullSDF.format(shortSDF.parse(tmpDates[1])!!)}"))
            }
        }
        findViewById<TextView>(R.id.txtEventStatus).apply {
            if(eventToday){
                text                                                        = "Event is ongoing"
                setTextColor(ContextCompat.getColor(context, R.color.colorEventNow))
            } else {
                text                                                        = "Event is not ongoing"
                setTextColor(ContextCompat.getColor(context, R.color.colorEventNotNow))
            }
        }
        if (qrData.w != ""){
            decryptData(qrData.w, qrSecrets, qrData.l).also{ website ->
                Log.e(LOG_KEY, "website ${website}")
                if(website != ""){
                    listData.add(SectionItem("Website"))
                    listData.add(EntryItem(website))
                }
            }
        }

        val listAdapter                                                     = QRViewAdapter(this, listData)
        findViewById<ListView>(R.id.lstEventDetails).apply {
            adapter                                                         = listAdapter
        }

    }
}
