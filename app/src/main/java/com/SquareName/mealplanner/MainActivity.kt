package com.SquareName.mealplanner

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.SquareName.mealplanner.tflite.Classifier
import com.SquareName.mealplanner.tflite.Classifier.create
import com.SquareName.mealplanner.ui.Library.LibraryFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.FileDescriptor
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var classifier: Classifier
    private lateinit var library: LibraryFragment
    val RESULT_IMAGEFILE = 1001
    val RESULT_CAMERAFILE = 1002


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
////タイトルバー表示
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_diary, R.id.navigation_library, R.id.navigation_bookmarklist
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        try {
            library = LibraryFragment()
            classifier = create(this, Classifier.Device.CPU, 2)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //アクションバーの設定
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.actionbar, menu)

        supportActionBar?.title = ""
        val seachItem = menu.findItem(R.id.menu_search)
        val searchView = seachItem.actionView as SearchView
        searchView.setQueryHint("食材名・レシピ名を入力")
        searchView.setIconifiedByDefault(false)
        searchView.clearFocus()

        //searchViewのリスナー
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            //検索ボタンを押した
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            //テキストに変更がかかった
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isNotEmpty()){

                }else{

                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    //別アクティビティから戻ってきたときの処理　リクエストコードで認識する
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        val newRequestCode = requestCode and 0xffff
        lateinit var bmp: Bitmap
        lateinit var results:List<Classifier.Recognition>
        var text: String? = ""
        textView = findViewById<TextView>(R.id.result_textView)
        imageView = findViewById<ImageView>(R.id.imageView)

        if(resultCode == Activity.RESULT_OK && resultData != null){
            //終了リザルトが画像選択アクテビティ
            if (newRequestCode == RESULT_IMAGEFILE) {
                var uri: Uri? = resultData.data
                var pfDescriptor = getContentResolver().openFileDescriptor(uri!!, "r")
                val fileDescriptor: FileDescriptor = pfDescriptor!!.fileDescriptor
                bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                pfDescriptor.close()

                //終了リザルトがカメラアクテビティ
            } else if (newRequestCode == RESULT_CAMERAFILE) {
                if (resultData.extras == null) {
                    return
                } else {
                    bmp = resultData.extras!!["data"] as Bitmap
                }
            }
            this.imageView.setImageBitmap(resizeImage(bmp))
            results =
                classifier.recognizeImage(resizeImage(bmp), 1)
            text += results[0].title
            this.textView.text = text
        }

    }

    //ビットマップイメージをリサイズ
    fun resizeImage(bmp: Bitmap): Bitmap {
        var height = bmp.height
        var width = bmp.width
        while (true) {
            var i = 2
            if (width < 500 && height < 500) {
                break
            } else {
                if (width > 500 || height > 500) {
                    width = width / i
                    height = height / i
                } else {
                    break
                }
                i++
            }
        }

        var croppedBitmap =
            Bitmap.createScaledBitmap(bmp, width, height, false)

        return croppedBitmap
    }
}