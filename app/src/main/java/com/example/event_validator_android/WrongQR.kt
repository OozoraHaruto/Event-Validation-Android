package com.example.event_validator_android

import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiNetworkSpecifier
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.vision.barcode.Barcode
import android.provider.CalendarContract
import android.view.View
import android.widget.ListAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class WrongQR : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wrong_qr)

        val language                            :String                     = Locale.getDefault().language
        val code                                                            = intent.getParcelableExtra(INT_BARCODE) as Barcode
        var listData                            :MutableList<Item>          = listOf<Item>().toMutableList()
        var fullSDF                             :SimpleDateFormat

        supportActionBar.apply {
            title                                                           = resources.getText(R.string.qr_data)
        }
        when (language){
            "ja", "zh" -> fullSDF                                           = SimpleDateFormat("${SDF_PRINTING_FORMAT_JA} hh:mm:ss")
            else -> fullSDF                                                 = SimpleDateFormat("${SDF_PRINTING_FORMAT_DEFAULT} hh:mm:ss")
        }

        Log.e(LOG_KEY, code.rawValue)

        when(code.valueFormat){
            Barcode.TEXT ->{
                listData.add(BasicItem(code.displayValue))
                setSnideComment(R.string.snide_comment_text, R.string.supported)
            }
            Barcode.URL -> {
                listData.add(BasicItem(code.displayValue))
                addButton(R.string.act_website, Intent.ACTION_VIEW, Uri.parse(code.displayValue))
                setSnideComment(R.string.snide_comment_website, R.string.supported)
            }
            Barcode.PHONE ->{
                listData.add(BasicItem(code.displayValue))
                addButton(R.string.act_phone, Intent.ACTION_VIEW, Uri.parse(code.rawValue))
                addButton(R.string.act_sms, Intent.ACTION_SENDTO, Uri.parse("smsto:${code.rawValue}"))
                setSnideComment(R.string.snide_comment_phone, R.string.supported)
            }
            Barcode.GEO ->{
                listData.add(LeftDetailItem(code.geoPoint.lat.toString(), resources.getText(R.string.detail_latitude).toString()))
                listData.add(LeftDetailItem(code.geoPoint.lng.toString(), resources.getText(R.string.detail_longitude).toString()))
                addButton(R.string.act_geo, Intent.ACTION_VIEW, Uri.parse(code.rawValue))
                setSnideComment(R.string.snide_comment_geo, R.string.supported)
            }
            Barcode.SMS ->{
                if (code.sms.phoneNumber.toString() != "")listData.add(LeftDetailItem(code.sms.phoneNumber.toString(), resources.getText(R.string.detail_phone_number).toString()))
                if (code.sms.message != "")listData.add(LeftDetailItem(code.sms.message, resources.getText(R.string.detail_message).toString()))
                val extraData = listOf<IntentData>(IntentData(Intent.EXTRA_TEXT, code.sms.message))
                addButton(R.string.act_phone, Intent.ACTION_SENDTO, Uri.parse("smsto:${code.sms.phoneNumber}"), extraData)
                setSnideComment(R.string.snide_comment_sms, R.string.supported)
            }
            Barcode.EMAIL ->{
                listData.add(LeftDetailItem(code.email.address, resources.getText(R.string.detail_email_address).toString()))
                if (code.email.subject != "")listData.add(LeftDetailItem(code.email.subject, resources.getText(R.string.detail_subject).toString()))
                if (code.email.body != "") listData.add(LeftDetailItem(code.email.body, resources.getText(R.string.detail_message).toString()))
                val extraData = listOf<IntentData>(
                    IntentData(Intent.EXTRA_SUBJECT, code.email.subject),
                    IntentData(Intent.EXTRA_TEXT, code.email.body)
                )
                addButton(R.string.act_email, Intent.ACTION_SENDTO, Uri.parse("mailto:${code.email.address}"), extraData)
                setSnideComment(R.string.snide_comment_email, R.string.supported)
            }
            Barcode.CALENDAR_EVENT ->{
                val start                                                   = code.calendarEvent.start
                val end                                                     = code.calendarEvent.end
                val startCal                                                = if (start.toString() != "") GregorianCalendar(start.year, (start.month - 1), start.day, start.hours, start.minutes, start.seconds) else GregorianCalendar()
                val endCal                                                  = if (end.toString() != "") GregorianCalendar(end.year, (end.month - 1), end.day, end.hours, end.minutes, end.seconds) else GregorianCalendar()

                if (code.calendarEvent.summary != "") listData.add(LeftDetailItem(code.calendarEvent.summary, resources.getText(R.string.detail_event_title).toString()))
                if (code.calendarEvent.location != "") listData.add(LeftDetailItem(code.calendarEvent.location, resources.getText(R.string.detail_location).toString()))
                if (start.toString() != "") listData.add(LeftDetailItem(fullSDF.format(Date(startCal.timeInMillis)), resources.getText(R.string.from).toString()))
                if (end.toString() != "") listData.add(LeftDetailItem(fullSDF.format(Date(endCal.timeInMillis)), resources.getText(R.string.to).toString()))
                val extraData = listOf<IntentData>(
                    IntentData(CalendarContract.Events.TITLE, code.calendarEvent.summary),
                    IntentData(CalendarContract.Events.EVENT_LOCATION, code.calendarEvent.location),
                    IntentData(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCal.timeInMillis, "long"),
                    IntentData(CalendarContract.EXTRA_EVENT_END_TIME, endCal.timeInMillis, "long")
                )
                Log.e(LOG_KEY, startCal.toString())
                addButton(R.string.act_event, Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI, extraData)
                setSnideComment(R.string.snide_comment_vevent, R.string.supported)
            }
            Barcode.CONTACT_INFO->{
                listData.add(LeftDetailItem("${code.contactInfo.title} ${code.contactInfo.name.formattedName}", resources.getText(R.string.detail_name).toString()))
                if (code.contactInfo.organization != ""){
                    listData.add(LeftDetailItem(code.contactInfo.organization, resources.getText(R.string.detail_company).toString()))
                    addButton(R.string.act_google_search, Intent.ACTION_VIEW, Uri.parse(createGoogleSearchLink(code.contactInfo.organization)), listOf<IntentData>(), code.contactInfo.organization)
                }
                if(code.contactInfo.phones.size != 0){
                    listData.add(SectionItem(resources.getText(R.string.detail_phone_number).toString()))
                    for (phone in code.contactInfo.phones){
                        listData.add(LeftDetailItem(phone.number, getPhoneType(phone.type)))
                        addButton(R.string.act_phone, Intent.ACTION_VIEW, Uri.parse("tel:${phone.number}"), listOf<IntentData>(), "${phone.number}, ${getPhoneType(phone.type)}")
                        if(phone.type == Barcode.Phone.MOBILE || phone.type == Barcode.Phone.WORK)
                            addButton(R.string.act_sms, Intent.ACTION_SENDTO, Uri.parse("smsto:${phone.number}"), listOf<IntentData>(), "${phone.number}, ${getPhoneType(phone.type)}")
                    }
                }
                if(code.contactInfo.emails.size != 0){
                    listData.add(SectionItem(resources.getText(R.string.detail_email_address).toString()))
                    for (email in code.contactInfo.emails){
                        listData.add(BasicItem(email.address))
                        addButton(R.string.act_email, Intent.ACTION_SENDTO, Uri.parse("mailto:${email.address}"), listOf<IntentData>(), email.address)
                    }
                }
                if(code.contactInfo.urls.size != 0){
                    listData.add(SectionItem(resources.getText(R.string.website).toString()))
                    for (url in code.contactInfo.urls){
                        listData.add(BasicItem(url))
                        addButton(R.string.act_website, Intent.ACTION_VIEW, Uri.parse(url), listOf<IntentData>(), url)
                    }
                }
                if(code.contactInfo.addresses.size != 0){
                    listData.add(SectionItem(resources.getText(R.string.detail_address).toString()))
                    for (address in code.contactInfo.addresses){
                        var addressFull                                     = ""
                        for (part in address.addressLines)
                            addressFull                                    += part
                        addButton(R.string.act_google_map_search, Intent.ACTION_VIEW, Uri.parse(createGoogleMapsLink(addressFull)), listOf<IntentData>(), getAddressType(address.type))
                        listData.add(LeftDetailItem(addressFull, getAddressType(address.type)))
                    }
                }
                setSnideComment(R.string.snide_comment_vcard, R.string.supported)
            }
            Barcode.WIFI ->{
                if (code.wifi.ssid != "") listData.add(LeftDetailItem(code.wifi.ssid, resources.getText(R.string.detail_ssid).toString()))
                if (code.wifi.password != "") listData.add(LeftDetailItem(code.wifi.password, resources.getText(R.string.detail_password).toString()))
                listData.add(LeftDetailItem(getEncryptionType(code.wifi.encryptionType), resources.getText(R.string.detail_encryption).toString()))
                setSnideComment(R.string.snide_comment_vcard, R.string.not_suported)
            }
            else ->{
                findViewById<TextView>(R.id.txtQRData).apply {
                    setVisible(true)
                    text                                                    = code.displayValue
                }
                setSnideComment(R.string.snide_comment_dunno, R.string.not_suported)
            }
        }

        findViewById<RecyclerView>(R.id.lstWrongQRData).apply {
            layoutManager                                                   = LinearLayoutManager(context)
            adapter                                                         = QRDataAdapter(listData)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager(context).orientation))
        }
    }

    fun setSnideComment(comment:Int, support: Int){
        findViewById<TextView>(R.id.txtSnideComment ).apply {
            text                                                            = "${resources.getText(comment)}\n${resources.getText(support)}"
        }
    }

    fun addButton(title: Int, intent: String, data: Uri, extraData: List<IntentData> = listOf<IntentData>(), additionalInfo: String = ""){
        findViewById<LinearLayout>(R.id.layAdditionalButton).apply {  ->
            val button                                                      = Button(context).apply{
                layoutParams                                                = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                text                                                        = if (additionalInfo == "") resources.getText(title) else "${resources.getText(title)} (${additionalInfo})"
                setOnClickListener {
                    Intent(intent).apply {
                        this.data                                           = data
                        if(extraData.size != 0){
                            for (intentData in extraData) {
                                when(intentData.type){
                                    "long"                                 -> this.putExtra(intentData.name, intentData.value as Long)
                                    "string"                               -> this.putExtra(intentData.name, intentData.value as String)
                                }
                            }
                        }
                    }.also {
                        startActivity(it)
                    }
                }
            }
            addView(button)
        }
    }

    private fun getPhoneType(type: Int): String{
        return when (type){
            Barcode.Phone.FAX                                              -> resources.getText(R.string.phone_fax).toString()
            Barcode.Phone.HOME                                             -> resources.getText(R.string.phone_home).toString()
            Barcode.Phone.MOBILE                                           -> resources.getText(R.string.phone_mobile).toString()
            Barcode.Phone.WORK                                             -> resources.getText(R.string.phone_work).toString()
            else                                                           -> resources.getText(R.string.phone_dunno).toString()
        }
    }

    private fun getAddressType(type: Int): String{
        return when (type){
            Barcode.Address.HOME                                           -> resources.getText(R.string.phone_home).toString()
            Barcode.Address.WORK                                           -> resources.getText(R.string.phone_work).toString()
            else                                                           -> resources.getText(R.string.phone_dunno).toString()
        }
    }

    private fun getEncryptionType(type: Int):String{
        return when (type){
            Barcode.WiFi.OPEN                                              -> resources.getText(R.string.detail_wifi_open).toString()
            Barcode.WiFi.WEP                                               -> resources.getText(R.string.detail_wifi_wep).toString()
            Barcode.WiFi.WPA                                               -> resources.getText(R.string.detail_wifi_wpa).toString()
            else                                                           -> resources.getText(R.string.phone_dunno).toString()
        }
    }
}
