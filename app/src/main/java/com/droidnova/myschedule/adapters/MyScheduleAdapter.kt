package com.droidnova.myschedule.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.droidnova.myschedule.activities.MyScheduleActivity
import com.droidnova.myschedule.activities.MainActivity
import com.droidnova.myschedule.activities.R
import com.droidnova.myschedule.activities.databinding.ItemsMyScheduleBinding
import com.droidnova.myschedule.database.DatabaseHandler
import com.droidnova.myschedule.models.HappyPlaceModel

open class MyScheduleAdapter (
    private val context: Context,
    private var list:ArrayList<HappyPlaceModel>)
     : RecyclerView.Adapter<MyScheduleAdapter.ViewHolder>(){

     private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: ItemsMyScheduleBinding):
        RecyclerView.ViewHolder(binding.root){
        val placeImage=binding.ivPlaceImage
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
    }

     fun setOnClickListener(onClickListener: OnClickListener){
         this.onClickListener = onClickListener
     }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemsMyScheduleBinding.inflate(LayoutInflater.from(parent.context),parent,false) )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        val internalImage= MyScheduleActivity()
        val imageURI :Uri = Uri.parse(model.image)
        if (imageURI.toString()!="null"){
            holder.placeImage.setImageURI(imageURI)
        } else {
            holder.placeImage.setImageResource(R.drawable.add_screen_image_placeholder)
        }


            holder.tvTitle.text = model.title
            holder.tvDescription.text = model.description

            holder.itemView.setOnClickListener {
                if (onClickListener != null){
                    onClickListener!!.onClick(position,model)
                }
            }


    }

     fun notifyEditItem(activity : Activity,position: Int,requestCode: Int){
         val intent = Intent(context, MyScheduleActivity::class.java)
         intent.putExtra(MainActivity.EXTRA_PLACES_DETAILS,list[position])
         activity.startActivityForResult(intent,requestCode)
         notifyItemChanged(position)
     }

     fun removeAt(position: Int){
         val dbHandler = DatabaseHandler(context)
         val isDelete = dbHandler.deleteHappyPlace(list[position])
         if(isDelete >0){
             list.removeAt(position)
             notifyItemRemoved(position)
         }
     }

    override fun getItemCount(): Int {
        return list.size
    }


      //step 1
      interface OnClickListener {
          fun onClick(position: Int, model: HappyPlaceModel)
      }

  }