package com.example.myschedule.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


import com.example.myschedule.databinding.ActivityMySceduleDetailBinding
import com.example.myschedule.models.HappyPlaceModel

class MyScheduleDetailActivity : AppCompatActivity() {

    private var binding : ActivityMySceduleDetailBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySceduleDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var happyPlaceDetailModel : HappyPlaceModel?= null
        if(intent.hasExtra(MainActivity.EXTRA_PLACES_DETAILS)){
            happyPlaceDetailModel = intent.getSerializableExtra(MainActivity.EXTRA_PLACES_DETAILS) as HappyPlaceModel
        }
        if(happyPlaceDetailModel != null){
            setSupportActionBar(binding?.toolbarHappyPlaceDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = happyPlaceDetailModel.title

            binding?.toolbarHappyPlaceDetail?.setNavigationOnClickListener {
                onBackPressed()
            }

            binding?.ivPlaceImage?.setImageURI(Uri.parse(happyPlaceDetailModel.image))
            binding?.tvDescription?.text=happyPlaceDetailModel.description



        }
    }
}