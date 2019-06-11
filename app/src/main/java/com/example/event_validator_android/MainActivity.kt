package com.example. event_validator_android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

const val EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE"

class MainActivity : AppCompatActivity() {
    private val LOG_TAG = "Barcode Scanner API"
    private val PHOTO_REQUEST = 10
    private val SAVED_INSTANCE_URI = "uri"
    private val SAVED_INSTANCE_RESULT = "result"
    private val REQUEST_PERMISSIONS = 20

    private var scanResults: TextView? = null
    private var detector: BarcodeDetector? = null
    private var currImagePath: String? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanResults = findViewById<View>(R.id.txtMessage) as TextView

        if (savedInstanceState != null) {
            imageUri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI))
            scanResults!!.text = savedInstanceState.getString(SAVED_INSTANCE_RESULT)
        }

        detector = BarcodeDetector.Builder(applicationContext).setBarcodeFormats(Barcode.ALL_FORMATS).build()
        if (!detector!!.isOperational){
            scanResults!!.text = "Could not set up detector!"
            return
        }
    }

    fun requestPermission(view: View){
        val PERMISSIONS = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission_group.CAMERA
        )
        ActivityCompat.requestPermissions(this@MainActivity, PERMISSIONS, REQUEST_PERMISSIONS)
        scanResults!!.text = "Hello"
    }
    fun showQRCodeScanner(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also{takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also{
                val imageFile: File? = try{
                    createImageFile()
                }catch (ex: IOException){
                    ex.printStackTrace()
                    null
                }

                imageFile?.also{
                    imageUri = FileProvider.getUriForFile(this, "com.example.event_validator_android.fileProvider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivityForResult(takePictureIntent, PHOTO_REQUEST)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            REQUEST_PERMISSIONS -> if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showQRCodeScanner()
            }else{
                Toast.makeText(this@MainActivity, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PHOTO_REQUEST && resultCode == Activity.RESULT_OK){
            try {
                val bitmap = decodeBitmapURL(this, imageUri)
                if(detector!!.isOperational && bitmap != null){
                    val frame = Frame.Builder().setBitmap(bitmap).build()
                    val barcodes = detector!!.detect(frame)
                    Log.e("DEBUG", barcodes.size().toString());
                    Log.e("DEBUG", barcodes.toString());

                    for(index in 0 until barcodes.size()){
                        val code = barcodes.valueAt(index)
                        Log.e("DEBUG", "HERE");
                        scanResults!!.text = "${code.displayValue}"
                        Log.e("DEBUG", "HERE");
                        val type = barcodes.valueAt(index).valueFormat
                        Log.e("DEBUG", "HERE");
                        when(type){
                            Barcode.CONTACT_INFO -> Log.i(LOG_TAG, code.contactInfo.title)
                            Barcode.EMAIL -> Log.i(LOG_TAG, code.email.address)
                            Barcode.ISBN -> Log.i(LOG_TAG, code.rawValue)
                            Barcode.PHONE -> Log.i(LOG_TAG, code.phone.number)
                            Barcode.PRODUCT -> Log.i(LOG_TAG, code.rawValue)
                            Barcode.SMS -> Log.i(LOG_TAG, code.sms.message)
                            Barcode.TEXT -> Log.i(LOG_TAG, code.rawValue)
                            Barcode.URL -> Log.i(LOG_TAG, "url: " + code.url.url)
                            Barcode.WIFI -> Log.i(LOG_TAG, code.wifi.ssid)
                            Barcode.GEO -> Log.i(LOG_TAG, code.geoPoint.lat.toString() + ":" + code.geoPoint.lng)
                            Barcode.CALENDAR_EVENT -> Log.i(LOG_TAG, code.calendarEvent.description)
                            Barcode.DRIVER_LICENSE -> Log.i(LOG_TAG, code.driverLicense.licenseNumber)
                            else -> Log.i(LOG_TAG, code.rawValue)
                        }
                    }
                    Log.e("DEBUG", "HERE2");
                    if(barcodes.size() == 0) scanResults!!.text = "Scan Failed"
                }else{
                    scanResults?.text = "Could not setup the Barcode detector!"
                }
            }catch (e: Exception){
                Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT).show()
                Log.e(LOG_TAG, e.toString())
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        if(imageUri != null){
            outState!!.putString(SAVED_INSTANCE_URI, imageUri!!.toString())
            outState.putString(SAVED_INSTANCE_RESULT, scanResults!!.text.toString())
        }
        super.onSaveInstanceState(outState)
    }

    @Throws(IOException::class)
    private fun createImageFile():File{
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currImagePath = absolutePath
        }
    }
    @Throws(FileNotFoundException::class)
    private fun decodeBitmapURL(ctx: Context, uri: Uri?): Bitmap?{
        val targetW = 600
        val targetH = 600

        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(ctx.contentResolver.openInputStream(uri!!), null, this)
            val photoW = outWidth
            val photoH = outHeight

            val scaleFactory = Math.min(photoW / targetW, photoH / targetH)

            inJustDecodeBounds = false
            inSampleSize = scaleFactory
        }
        return BitmapFactory.decodeStream(ctx.contentResolver.openInputStream(uri!!), null, bmOptions)
    }

}
