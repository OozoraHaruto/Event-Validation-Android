package com.example.event_validator_android

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.vision.barcode.Barcode
import android.provider.CalendarContract
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*


class WrongQR : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wrong_qr)

        val code                                                            = intent.getParcelableExtra(INT_BARCODE) as Barcode

        findViewById<TextView>(R.id.txtQRData).apply {
            text                                                            = code.displayValue
        }

        Log.e(LOG_KEY, code.rawValue)

        when(code.valueFormat){
            Barcode.TEXT ->{
                setSnideComment(R.string.snide_comment_text, R.string.supported)
            }
            Barcode.URL -> {
                addButton(R.string.act_website, Intent.ACTION_VIEW, Uri.parse(code.displayValue))
                setSnideComment(R.string.snide_comment_website, R.string.supported)
            }
            Barcode.PHONE ->{
                addButton(R.string.act_phone, Intent.ACTION_VIEW, Uri.parse(code.rawValue))
                addButton(R.string.act_sms, Intent.ACTION_SENDTO, Uri.parse("smsto:${code.rawValue}"))
                setSnideComment(R.string.snide_comment_phone, R.string.supported)
            }
            Barcode.GEO ->{
                addButton(R.string.act_geo, Intent.ACTION_VIEW, Uri.parse(code.rawValue))
                setSnideComment(R.string.snide_comment_geo, R.string.supported)
            }
            Barcode.SMS ->{
                val extraData = listOf<IntentData>(IntentData(Intent.EXTRA_TEXT, code.sms.message))
                addButton(R.string.act_phone, Intent.ACTION_SENDTO, Uri.parse("smsto:${code.sms.phoneNumber}"), extraData)
                setSnideComment(R.string.snide_comment_sms, R.string.supported)
            }
            Barcode.EMAIL ->{
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
                val startCal                                                = GregorianCalendar(start.year, start.month, start.day, start.hours, start.minutes, start.seconds)
                val endCal                                                  = GregorianCalendar(end.year, end.month, end.day, end.hours, end.minutes, end.seconds)
                val extraData = listOf<IntentData>(
                    IntentData(CalendarContract.Events.TITLE, code.calendarEvent.summary),
                    IntentData(CalendarContract.Events.EVENT_LOCATION, code.calendarEvent.location),
                    IntentData(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCal.timeInMillis, "long"),
                    IntentData(CalendarContract.EXTRA_EVENT_BEGIN_TIME, endCal.timeInMillis, "long")
                )
                Log.e(LOG_KEY, startCal.timeInMillis.toString())
                addButton(R.string.act_event, Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI, extraData)
                setSnideComment(R.string.snide_comment_vevent, R.string.supported)
            }
            Barcode.CONTACT_INFO->{
                if(code.contactInfo.emails.size != 0){
                    for (email in code.contactInfo.emails){
                        addButton(R.string.act_email, Intent.ACTION_SENDTO, Uri.parse("mailto:${email.address}"), listOf<IntentData>(), email.address)
                    }
                }
                if(code.contactInfo.urls.size != 0){
                    for (url in code.contactInfo.urls)
                        addButton(R.string.act_website, Intent.ACTION_VIEW, Uri.parse(url), listOf<IntentData>(), url)
                }
                if(code.contactInfo.phones.size != 0){
                    for (phone in code.contactInfo.phones){
                        addButton(R.string.act_phone, Intent.ACTION_VIEW, Uri.parse("tel:${phone.number}"), listOf<IntentData>(), "${phone.number}, ${getPhoneType(phone.type)}")
                        if(phone.type == Barcode.Phone.MOBILE || phone.type == Barcode.Phone.WORK)
                            addButton(R.string.act_sms, Intent.ACTION_SENDTO, Uri.parse("smsto:${phone.number}"), listOf<IntentData>(), "${phone.number}, ${getPhoneType(phone.type)}")
                    }
                }
                if (code.contactInfo.organization != ""){
                    addButton(R.string.act_google_search, Intent.ACTION_VIEW, Uri.parse(createGoogleSearchLink(code.contactInfo.organization)), listOf<IntentData>(), code.contactInfo.organization)
                }
                if(code.contactInfo.addresses.size != 0){
                    for (address in code.contactInfo.addresses){
                        var addressFull                                     = ""
                        for (part in address.addressLines)
                            addressFull                                    += part
                        addButton(R.string.act_google_map_search, Intent.ACTION_VIEW, Uri.parse(createGoogleMapsLink(addressFull)), listOf<IntentData>(), addressFull)
                    }
                }
                setSnideComment(R.string.snide_comment_vcard, R.string.supported)
            }

            else ->{
                setSnideComment(R.string.snide_comment_dunno, R.string.not_suported)
            }
        }
    }

    fun setSnideComment(comment:Int, support: Int){
        findViewById<TextView>(R.id.txtSnideComment).apply {
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

    fun getPhoneType(type: Int): String{
        return when (type){
            Barcode.Phone.FAX                                              -> resources.getText(R.string.phone_fax).toString()
            Barcode.Phone.HOME                                             -> resources.getText(R.string.phone_home).toString()
            Barcode.Phone.MOBILE                                           -> resources.getText(R.string.phone_mobile).toString()
            Barcode.Phone.WORK                                             -> resources.getText(R.string.phone_work).toString()
            else                                                           -> resources.getText(R.string.phone_dunno).toString()
        }
    }

//    fun buttonImplementFunction(title:Int, clickEvent: View.OnClickListener){
//        findViewById<Button>(R.id.btnPerformAction).apply {
//            text                                                            = resources.getText(title)
//            isEnabled                                                       = true
//            isVisible                                                       = true
//            setOnClickListener(clickEvent)
//        }
//    }
//
//    fun sendSMS(number: String, message: String): View.OnClickListener{
//        return View.OnClickListener {
//
//        }
//    }
}
