package com.droidnova.myschedule.activities


import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.droidnova.myschedule.activities.databinding.ActivityMySceduleDetailBinding
import com.droidnova.myschedule.models.HappyPlaceModel
import java.util.*

class MyScheduleDetailActivity : AppCompatActivity(),TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var player : MediaPlayer? = null
    private var binding : ActivityMySceduleDetailBinding?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySceduleDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        tts = TextToSpeech(this,this)

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
            val imageURI :Uri = Uri.parse(happyPlaceDetailModel.image)
            if (imageURI.toString()=="null") {
                Log.e("urinaihe",imageURI.toString())
                binding?.ivPlaceImage?.setImageResource(R.drawable.add_screen_image_placeholder)

            } else {
                Log.e("urihai",imageURI.toString())
                binding?.ivPlaceImage?.setImageURI(imageURI)
            }

            binding?.tvDescription?.text=happyPlaceDetailModel.description
            binding?.tvDate?.text=happyPlaceDetailModel.date
            binding?.ivTextToSpeaker?.setOnClickListener {
                speakOut(binding?.tvDescription?.text.toString())
            }
        }
    }
    private fun speakOut(text: String){
        tts!!.speak(text,TextToSpeech.QUEUE_FLUSH,null,"")
    }

    override fun onInit(status: Int) {
        if(status==TextToSpeech.SUCCESS){
            val result = tts?.setLanguage(Locale.UK)
            if(result==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS","language not supported")
            }
        }else{
            Log.e("TTS","initialization failed")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (tts!=null){
            tts?.stop()
            tts?.shutdown()
        }
        if(player!=null){
            player?.stop()
        }
    }

}