package com.droidnova.myschedule.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.droidnova.myschedule.activities.databinding.ActivityMyScheduleBinding
import com.droidnova.myschedule.database.DatabaseHandler
import com.droidnova.myschedule.models.HappyPlaceModel
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

import com.karumi.dexter.listener.single.PermissionListener
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class MyScheduleActivity : AppCompatActivity() ,View.OnClickListener{
    private var binding : ActivityMyScheduleBinding?=null
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    var saveImageToInternalStorage : Uri?=null
    private var mLatitude : Double = 0.0
    private var mLongitude : Double = 0.0

    private var mHappyPlaceDetails : HappyPlaceModel? = null


    val imagePicker :ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == RESULT_OK){
                val data:Intent? = result.data
                if (result.data != null){
                    val filUri:Uri? =data!!.data
                    try {
                      // val thumbNail :Bitmap =data!!.extras!!.get("data") as Bitmap
                       val thumbNail1 : Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,filUri)

                         saveImageToInternalStorage = saveImageToInternalStorage(thumbNail1)
                        Log.e("Saved image: ","path :: $saveImageToInternalStorage")
                        Toast.makeText(this@MyScheduleActivity,"$saveImageToInternalStorage",Toast.LENGTH_SHORT)
                       binding?.ivPlaceImage?.setImageBitmap(thumbNail1)
                    }catch (e : IOException){
                        e.printStackTrace()
                        Toast.makeText(this@MyScheduleActivity,"something wrong",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    val imageCapture :ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == RESULT_OK){
                if (result.data != null){
                    try {

                        val data = result.data
                        val thumbNail2 =data!!.extras!!.get("data") as Bitmap
                         saveImageToInternalStorage = saveImageToInternalStorage(thumbNail2)
                        Log.e("Saved image: ","path :: $saveImageToInternalStorage")
                        Toast.makeText(this@MyScheduleActivity,"$saveImageToInternalStorage",Toast.LENGTH_SHORT)
                        binding?.ivPlaceImage?.setImageBitmap(thumbNail2)

                    }
                    catch (e : IOException){
                        e.printStackTrace()
                        Toast.makeText(this@MyScheduleActivity,"something wrong",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMyScheduleBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }



        if (intent.hasExtra(MainActivity.EXTRA_PLACES_DETAILS)){
            mHappyPlaceDetails=intent.getSerializableExtra(
                MainActivity.EXTRA_PLACES_DETAILS
            ) as HappyPlaceModel

        }

        dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)

                         cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

        updateDateInView()

        if(mHappyPlaceDetails!=null){

            supportActionBar?.title = "Edit Plan"

            binding?.etTitle?.setText(mHappyPlaceDetails!!.title)
            binding?.etDescription?.setText(mHappyPlaceDetails!!.description)
            binding?.etDate?.setText(mHappyPlaceDetails!!.date)

            saveImageToInternalStorage = Uri.parse(
                mHappyPlaceDetails!!.image)

            binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorage)

            binding?.btnSave?.text="UPDATE"
        }

        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)



    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date ->{
                DatePickerDialog(this@MyScheduleActivity,
                    dateSetListener,cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()

            }
            R.id.tvAddImage ->{
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("select action")
                val pictureDialogItems = arrayOf("Select from Gallery","Capture from camera")
                pictureDialog.setItems(pictureDialogItems){
                    dialog,which->
                    when(which){
                        0 -> choosePhotoFromGallery()
                        1 -> capturePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.btn_save ->{
                when {
                    binding?.etTitle?.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                    }
                   else ->{
                        val happyPlaceModel = HappyPlaceModel(
                            if(mHappyPlaceDetails ==null) 0 else mHappyPlaceDetails!!.id,
                            binding?.etTitle?.text.toString(),
                            saveImageToInternalStorage.toString(),
                            binding?.etDescription?.text.toString(),
                            binding?.etDate?.text.toString(),

                        )
                        val dbHolder = DatabaseHandler(this)
                        if(mHappyPlaceDetails == null){
                            val addHappyPlace = dbHolder.addHappyPlace(happyPlaceModel)
                            if(addHappyPlace>0){
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(this, " Task saved " , Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }else{
                            val updateHappyPlace = dbHolder.updateHappyPlace(happyPlaceModel)
                            if(updateHappyPlace>0){
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(this, " Task saved " , Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    }
                }

            }

        }
    }
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if(resultCode == Activity.RESULT_OK){
                if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)

                    mLatitude = place.latLng!!.latitude
                    mLongitude = place.latLng!!.longitude
                }

            }
        }

    private fun choosePhotoFromGallery() {
         Dexter.withContext(this).withPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
             .withListener(object : PermissionListener {
                 override fun onPermissionGranted(response : PermissionGrantedResponse?)
                 {
                    val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                     imagePicker.launch(galleryIntent)
                 }
                 override fun onPermissionDenied(response: PermissionDeniedResponse)
                 {Toast.makeText(this@MyScheduleActivity,"permission denied for pictures",Toast.LENGTH_SHORT).show()}
                 override fun onPermissionRationaleShouldBeShown(permissions : PermissionRequest, token: PermissionToken) {
                     showRationalDialogForPermissions()
                 }
             }).onSameThread().check()
    }

    private fun capturePhotoFromCamera() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report : MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        imageCapture.launch(cameraIntent)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions : MutableList<PermissionRequest>?,
                                                                token: PermissionToken?) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
    }






    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("It looks like you have turned offf permission " +
                "required for this feature . " +
                "it can be changed from settings")
            .setPositiveButton("Go to Settings")
            {_,_->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package",packageName,null)
                    intent.data=uri
                    startActivity(intent)
                } catch (e:ActivityNotFoundException){
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"){dialog,_ ->
                dialog.dismiss()
            }.show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri{
        val file = File(externalCacheDir?.absoluteFile.toString() +
                File.separator + "DailyPlans" + System.currentTimeMillis()/1000 + ".jpg"
        )
        if(bitmap != null){
            try{
                val bytes = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes)

                val fo = FileOutputStream(file)
                fo.write(bytes.toByteArray())
                fo.close()


            }catch (e: IOException){
                e.printStackTrace()
            }}
        return Uri.parse((file.absolutePath))
    }



    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object{
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}