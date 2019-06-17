package com.example.event_validator_android

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class CorrectQR : AppCompatActivity() {
    var listData                                :MutableList<Item>          = listOf<Item>().toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_correct_qr)

        val language                            :String                     = Locale.getDefault().language
        val qrData                                                          = intent.getSerializableExtra(INT_QRDATA) as QRData
        var qrSecrets                                                       = intent.getSerializableExtra(INT_QRSECRETS) as QRSecrets
        val shortSDF                                                        = SimpleDateFormat(SDF_INITIAL_FORMAT);
        var fullSDF                             :SimpleDateFormat
        var eventToday                                                      = false


        supportActionBar.apply {
            title                                                           = resources.getText(R.string.event_details)
        }
        when (language){
            "ja", "zh" -> fullSDF                                           = SimpleDateFormat(SDF_PRINTING_FORMAT_JA)
            else -> fullSDF                                                 = SimpleDateFormat(SDF_PRINTING_FORMAT_DEFAULT)
        }

        findViewById<TextView>(R.id.txtEventName).apply {
            text                                                            = decryptData(qrData.e, qrSecrets, qrData.l)
        }
        decryptData(qrData.d, qrSecrets, qrData.l).also{ date ->
            val now                             :String                     = shortSDF.format(Date()).toString()
            val dateIsList                                                  = date.indexOf("-") == -1
            val tmpDates                                                    = if (dateIsList) date.split(",") else date.split("-")

            listData.add(SectionItem(if (dateIsList) resources.getText(R.string.date_list).toString() else resources.getText(R.string.date_duration).toString()))
            if(dateIsList){
                for (currentDate in tmpDates){
                    if (currentDate == now) eventToday                      = true
                    listData.add(BasicItem(fullSDF.format(shortSDF.parse(currentDate)!!).toString()))
                }
            } else {
                val tmpNow                                                  = Date()
                eventToday                                                  = tmpNow.compareTo(shortSDF.parse(tmpDates[0])) > 0 && tmpNow.compareTo(shortSDF.parse(tmpDates[1])) < 1
                listData.add(LeftDetailItem(fullSDF.format(shortSDF.parse(tmpDates[0])!!), resources.getText(R.string.from).toString()))
                listData.add(LeftDetailItem(fullSDF.format(shortSDF.parse(tmpDates[1])!!), resources.getText(R.string.to).toString()))
            }
        }
        findViewById<TextView>(R.id.txtEventStatus).apply {
            if(eventToday){
                text                                                        = resources.getText(R.string.event_happening_now)
                setTextColor(ContextCompat.getColor(context, R.color.colorEventNow))
            } else {
                text                                                        = resources.getText(R.string.event_not_happening_now)
                setTextColor(ContextCompat.getColor(context, R.color.colorEventNotNow))
            }
        }
        if (qrData.w != ""){
            decryptData(qrData.w, qrSecrets, qrData.l).also{ website ->
                if(website != ""){
                    listData.add(SectionItem(resources.getText(R.string.website).toString()))
                    listData.add(BasicItem(website))
                }
            }
        }


        findViewById<RecyclerView>(R.id.lstEventDetails).apply {
            layoutManager                                                   = LinearLayoutManager(context)
            adapter                                                         = QRDataAdapter(listData, onClickList())
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager(context).orientation))
        }
    }

    private fun onClickList(): View.OnClickListener{
        return View.OnClickListener {
            var data                                                        = ""

            try{
                data = it.findViewById<TextView>(R.id.lvBasicTxtItemTitle).text.toString()
            }catch (e: Exception) {
                Log.e(LOG_KEY, "Clicked Item not Basic Item")
            }
            try{
                data = it.findViewById<TextView>(R.id.lvLeftDetailTxtTitle).text.toString()
            }catch (e: Exception) {
                Log.e(LOG_KEY, "Clicked Item not Left Detail Item")
            }
            if(data != ""){
                val websiteRegex                                            = RGX_WEBSITE.toRegex(RegexOption.IGNORE_CASE)

                websiteRegex.matches(data).also { matches ->
                    if (matches){
                        Intent(android.content.Intent.ACTION_VIEW).apply {
                            this.data                                       = Uri.parse(data)
                        }.also {
                            startActivity(it)
                        }
                    }
                }
            }
        }
    }
}
