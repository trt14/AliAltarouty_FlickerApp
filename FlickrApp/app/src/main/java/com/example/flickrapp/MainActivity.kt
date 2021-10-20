package com.example.flickrapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var images: ArrayList<Image>

    private lateinit var rvMain: RecyclerView
    private lateinit var rvAdapter: RVAdapter

    private lateinit var llBottom: LinearLayout
    private lateinit var etSearch: EditText
    private lateinit var btSearch: Button

    private lateinit var ivMain: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        images = arrayListOf()

        rvMain = findViewById(R.id.rvMain)
        rvAdapter = RVAdapter(this, images)
        rvMain.adapter = rvAdapter
        rvMain.layoutManager = LinearLayoutManager(this)

        llBottom = findViewById(R.id.llBottom)
        etSearch = findViewById(R.id.etSearch)
        btSearch = findViewById(R.id.btSearch)

        etSearch.text.toString().replace(",","&")
        btSearch.setOnClickListener {
            images.clear()

            if(etSearch.text.isNotEmpty()){
                requestAPI()
            }else{
                Toast.makeText(this, "Search field is empty", Toast.LENGTH_LONG).show()
            }
        }

        ivMain = findViewById(R.id.ivMain)
        ivMain.setOnClickListener { closeImg() }
    }

    private fun requestAPI(){
        CoroutineScope(IO).launch {
            val data = async { getPhotos() }.await()
            if(data.isNotEmpty()){
                println(data)
                showPhotos(data)
            }else{
                Toast.makeText(this@MainActivity, "No Images Found", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getPhotos(): String{
        var response = ""
        try{
//https://api.flickr.com/services/rest/?method=flickr.photos.search&per_page=10&api_key=7fdbf2b412b6b3adc28816636497c229&tags=cats&format=json&nojsoncallback=1
            response = URL("https://api.flickr.com/services/rest/?method=flickr.photos.search&per_page=10&api_key=7fdbf2b412b6b3adc28816636497c229&tags=${etSearch.text}&format=json&nojsoncallback=1")
                .readText(Charsets.UTF_8)
        }catch(e: Exception){
            println("ISSUE: $e")
        }
        return response
    }

    private suspend fun showPhotos(data: String){
        withContext(Main){
            val jsonObj = JSONObject(data)
            val photos = jsonObj.getJSONObject("photos").getJSONArray("photo")
            println("photos")
            println(photos.getJSONObject(0))
            println(photos.getJSONObject(0).getString("farm"))
            for(i in 0 until photos.length()){
                val title = photos.getJSONObject(i).getString("title")
                val farmID = photos.getJSONObject(i).getString("farm")
                val serverID = photos.getJSONObject(i).getString("server")
                val id = photos.getJSONObject(i).getString("id")
                val secret = photos.getJSONObject(i).getString("secret")
                val photoLink = "https://farm$farmID.staticflickr.com/$serverID/${id}_$secret.jpg"
                images.add(Image(title, photoLink))
            }
            rvAdapter.notifyDataSetChanged()
        }
    }

    fun openImg(link: String){
        Glide.with(this).load(link).into(ivMain)
        ivMain.isVisible = true
        rvMain.isVisible = false
        llBottom.isVisible = false
    }

    private fun closeImg(){
        ivMain.isVisible = false
        rvMain.isVisible = true
        llBottom.isVisible = true
    }
}