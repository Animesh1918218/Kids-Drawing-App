package com.example.kidsdrawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog


import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas

import android.net.Uri


import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore

import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.get
import androidx.core.view.size

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() ,View.OnClickListener{
    private val galleryAccessVariable:ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        activityResult->
        if(activityResult.resultCode== RESULT_OK && activityResult.data!=null){
            val mygalleryBcakground:ImageView= findViewById(R.id.mybackgroundImage)
            mygalleryBcakground.setImageURI(activityResult.data?.data)
        }
    }
    private val permissionForStorageAccess:ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
      it.entries.forEach {
          var isGranted = it.value
          var permission =it.key
             if (isGranted){
                 if(permission==Manifest.permission.READ_EXTERNAL_STORAGE) {
                     Toast.makeText(this, "Permission Granted For Storage", Toast.LENGTH_SHORT)
                         .show()
                     val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                     galleryAccessVariable.launch(intent)
                 }
             }
          else{
              if(permission==Manifest.permission.READ_EXTERNAL_STORAGE){
                  Toast.makeText(this, "External Storage Acess Denied", Toast.LENGTH_SHORT).show()
              }
             }
      }
    }

    private var mdrawingView:MyDrawingView?=null
    private var brushWidthButton:ImageButton?=null

    private var gallerybtn:ImageButton?=null
    private var saveImageBtn:ImageButton?=null
    private var colorButton: ImageButton?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        colorButton = findViewById(R.id.colorButton)
        colorButton?.setOnClickListener {
            setupColorDialogueBox()
        }
        mdrawingView = findViewById(R.id.myDrawingView)
        brushWidthButton = findViewById(R.id.brushImage)
        brushWidthButton?.setOnClickListener {
            setUpBrushWidthDialogBox()
        }


        gallerybtn = findViewById(R.id.captureImageFromGallery)
        gallerybtn?.setOnClickListener {
             setPermissions()
        }
        saveImageBtn =findViewById(R.id.saveImageFromApp)
        saveImageBtn?.setOnClickListener {
            if(isExternalPemissionGranted()) {
                lifecycleScope.launch {
                    val myLayout: FrameLayout = findViewById(R.id.myFrameLayout)
                    val progressbar = progressBarDialogue()
                    progressbar.show()
                     saveBitmapInFile(getBitmapfromView(myLayout),progressbar)

                }
            }
            else{
                Toast.makeText(this, "File Permission Not Granted", Toast.LENGTH_SHORT).show()
            }
        }

    }



    fun setPermissions(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            customBuilderDialogue("Access Denied","Access for storage denied")
        }
        else{
            permissionForStorageAccess.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }
    fun setUpBrushWidthDialogBox(){
        val brushWidthDialog = Dialog(this)
        brushWidthDialog.setContentView(R.layout.brush_width_layout)
        brushWidthDialog.setTitle("Width size : ")
        val brushLarge:ImageButton = brushWidthDialog.findViewById(R.id.largebtn)
        brushLarge.setOnClickListener {
            mdrawingView?.setNewBrushWidth(20f)
            brushWidthDialog.dismiss()
        }
        val brushMedium:ImageButton = brushWidthDialog.findViewById(R.id.mediumbtn)
        brushMedium.setOnClickListener {
            mdrawingView?.setNewBrushWidth(10f)
            brushWidthDialog.dismiss()
        }
        val brushSmall:ImageButton = brushWidthDialog.findViewById(R.id.smallbtn)
        brushSmall.setOnClickListener {
            mdrawingView?.setNewBrushWidth(5f)
            brushWidthDialog.dismiss()
        }

      brushWidthDialog.show()
    }

    override fun onClick(v: View?) {

    }

    fun customBuilderDialogue(title:String,message:String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK"){
            dialogueInterface,why->
            dialogueInterface.dismiss()
        }
        builder.create().show()

    }
   private fun getBitmapfromView(view:View):Bitmap{
       val mbitmap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
       val mcanvas = Canvas(mbitmap)
       view.draw(mcanvas)
       return mbitmap
   }

    private fun isExternalPemissionGranted():Boolean{
        val storagePermission = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        return storagePermission==PackageManager.PERMISSION_GRANTED
    }
    private suspend fun saveBitmapInFile(bitmap:Bitmap,dialogue:Dialog):String{
        var result =""
        withContext(Dispatchers.IO){
            try {
                val bytes =  ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG,90,bytes)
                val imageFolder = File(externalCacheDir.toString()+File.separator+"Images")
                imageFolder.mkdir()
                val mfile = File(imageFolder,"Kids Drawing App_"+System.currentTimeMillis()/1000+".jpg")
                val moutputstream = FileOutputStream(mfile)
                 moutputstream.write(bytes.toByteArray())
                moutputstream.flush()
                moutputstream.close()
                result=mfile.absolutePath

                runOnUiThread {
                    if(result.isNotEmpty()){
                    Toast.makeText(this@MainActivity,"File saved to ${mfile.absolutePath}",Toast.LENGTH_LONG).show()

                    setUpEnablingFeatures(FileProvider.getUriForFile(baseContext,"com.example.kidsdrawingapp.fileprovider",mfile))
                    }
                else
                        Toast.makeText(this@MainActivity,"File not Saved",Toast.LENGTH_LONG).show()

                dialogue.dismiss()
                }
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(this@MainActivity,"File Not Saved",Toast.LENGTH_LONG).show()

            }

        }
        return result
    }



    private fun progressBarDialogue():Dialog{
        val prgressDialogue=Dialog(this)
        prgressDialogue.setContentView(R.layout.progress_bar)
        prgressDialogue.setCancelable(false)
        return prgressDialogue
    }



    private fun setUpEnablingFeatures(uri:Uri){


                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    intent.type = "image/jpeg"
                    startActivity(Intent.createChooser(intent, "Share image via "))

    }

  private fun setupColorDialogueBox(){
      val colorDialogueBox = Dialog(this)
      colorDialogueBox.setContentView(R.layout.all_colors)
      val coloLayout:LinearLayout = colorDialogueBox.findViewById(R.id.colorsLayout)

  }





}