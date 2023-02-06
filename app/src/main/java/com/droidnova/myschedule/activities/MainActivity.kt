package com.droidnova.myschedule.activities



import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidnova.myschedule.activities.databinding.ActivityMainBinding
import com.droidnova.myschedule.adapters.MyScheduleAdapter
import com.droidnova.myschedule.database.DatabaseHandler
import com.droidnova.myschedule.models.HappyPlaceModel
import com.droidnova.myschedule.utils.swipeToDeleteCallback
import com.droidnova.myschedule.utils.swipeToEditCallback



class MainActivity : AppCompatActivity() {
    private var binding : ActivityMainBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        setSupportActionBar(binding?.toolbarHappyPlace)
        supportActionBar?.title = "My Plans"


        binding?.fabAddHappyPlace?.setOnClickListener{
            val intent = Intent(this@MainActivity, MyScheduleActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
        getHappyPlacesListFromLocalDB()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.share_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menu_share ->{
                val string = "Download this app to manage your daily life plans and" +
                " important things that you often forget. you can just slide to delete or edit ." +
                        " \n\n https://play.google.com/store/apps/details?id=com.droidnova.myschedule"

                shareApp(string)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun shareApp(string : String){
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT,string)
        shareIntent.type="text/plain"
        startActivity(Intent.createChooser(shareIntent,"Share"))

    }


    private fun setUpHappyPlacesRecyclerView(happyPlaceList: ArrayList<HappyPlaceModel>){
        binding?.rvHappyPlacesList?.layoutManager = LinearLayoutManager(this)

        val placesAdapter = MyScheduleAdapter(this,happyPlaceList)
        binding?.rvHappyPlacesList?.adapter = placesAdapter
        binding?.rvHappyPlacesList?.setHasFixedSize(true)

        placesAdapter.setOnClickListener(object : MyScheduleAdapter.OnClickListener{
            override fun onClick(position : Int,model: HappyPlaceModel){
                val intent = Intent(this@MainActivity,
                    MyScheduleDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACES_DETAILS,model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object : swipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val adapter = binding?.rvHappyPlacesList?.adapter as MyScheduleAdapter
                adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE
                )
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)

        val deleteSwipeHandler = object : swipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvHappyPlacesList?.adapter as MyScheduleAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getHappyPlacesListFromLocalDB()

            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)

    }


    private fun getHappyPlacesListFromLocalDB(){
        val dbHandler = DatabaseHandler(this)
        val getHappyPlaceList : ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()
        if (getHappyPlaceList.size>0){

            binding?.rvHappyPlacesList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
            setUpHappyPlacesRecyclerView(getHappyPlaceList)
        }else {
            binding?.tvSwipeHint?.visibility=View.GONE
            binding?.rvHappyPlacesList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
        if(getHappyPlaceList.size in 1..3){
            binding?.tvSwipeHint?.visibility=View.VISIBLE
        }else{
            binding?.tvSwipeHint?.visibility=View.GONE
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if (resultCode== Activity.RESULT_OK){
                getHappyPlacesListFromLocalDB()
            }else{
                Log.e("Activity","canceled or back pressed")
            }
        }
    }
    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE=1
        var EXTRA_PLACES_DETAILS = "extra_place_details"
    }
}