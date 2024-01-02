package com.celalkorucu.xxx.Activitys

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.celalkorucu.xxx.R
import com.celalkorucu.xxx.ViewModel.SignUpViewModel
import com.celalkorucu.xxx.databinding.ActivitySignUpBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.io.IOException

class SignUpActivity : AppCompatActivity() {

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedProfilePhoto : Bitmap? = null
    private lateinit var signUpBinding: ActivitySignUpBinding
    private lateinit var signUpViewModel: SignUpViewModel

    //onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(signUpBinding.root)

        //Launcher'ların initialize olduğu metod
        registerLauncher()


        signUpViewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)

        //SignUp işleminin dinlenme anı
        signUpViewModel.signUpControl.observe(this){
            if(it){
                Toast.makeText(this@SignUpActivity , "Kayıt olma başarılı" , Toast.LENGTH_LONG).show()
                val intent = Intent(this , LoginActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this , "Beklenmedik bir hata meydana geldi" , Toast.LENGTH_LONG).show()

            }
        }
    }

    //Login Ekranına döndüren metod
    fun goToSignInActivity(view : View){
        val intent = Intent (this@SignUpActivity , LoginActivity::class.java)
        startActivity(intent)
        finish()

    }

    //SignUp Button Click
    fun signUpClick(view : View){

        //Seçilen veya seçilmeyen profil fotoğrafını byteArray türüne çevirir
        //Ardından byteArray ve kullanıcı adını signUp metoduna parametre olaran verir

        val userName = signUpBinding.signUpUserNameText.text.toString()
        val byteArrayProfilePhoto : ByteArray

        if(userName != "" ){

            if(selectedProfilePhoto != null){
                val smallBitmap = makeSmallImage(selectedProfilePhoto!!,300)
                byteArrayProfilePhoto = bitmapToBytesArray(smallBitmap!!)
            }
            else {
                val defaultProfilePhotoBitmap =
                    BitmapFactory.decodeResource(this.resources, R.drawable.default__pp)
                val smallDefaultProfilePhotoBitmap = makeSmallImage(defaultProfilePhotoBitmap, 300)
                smallDefaultProfilePhotoBitmap.let {
                    byteArrayProfilePhoto = bitmapToBytesArray(it!!)
                }
            }
            signUpViewModel.signUp(byteArrayProfilePhoto , userName)

        }else{
            Toast.makeText(this, "Kullanıcı adı giriniz",Toast.LENGTH_LONG).show()
        }
    }

    //Gerekli izinler ve galeriye gitmemizi sağlayan metod
    fun signUpAddImageClick(view : View) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

            if(ContextCompat.checkSelfPermission(this , android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view , "Permission needed for gallery" , Snackbar.LENGTH_INDEFINITE).setAction("Give permission"){
                        //Request Permission
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)

                    }.show()
                }else{
                    //Request Permission
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)

                }
            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                //Start Activity for result
                activityResultLauncher.launch(intentToGallery)
            }
        }else{

            if(ContextCompat.checkSelfPermission(this , android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view , "Permission needed for gallery" , Snackbar.LENGTH_INDEFINITE).setAction("Give permission"){
                        //Request Permission
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
                }else{
                    //Request Permission
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)

                }
            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                //Start Activity for result
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    //Launcher
    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == RESULT_OK){
                val intentFromResult = it.data
                if(intentFromResult != null){
                    val imageData :Uri ? = intentFromResult.data

                    imageData.let {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source: ImageDecoder.Source = ImageDecoder.createSource(this.contentResolver ,it!!)
                            try {
                                selectedProfilePhoto = ImageDecoder.decodeBitmap(source)
                            } catch (e: IOException) {
                                throw RuntimeException(e)
                            }
                            signUpBinding.sigUpProfilePhoto.setImageBitmap(selectedProfilePhoto)
                        } else {
                            try {
                                selectedProfilePhoto = MediaStore.Images.Media.getBitmap(
                                    this@SignUpActivity.contentResolver,
                                    imageData
                                )
                            } catch (e: IOException) {
                                throw RuntimeException(e)
                            }
                            signUpBinding.sigUpProfilePhoto.setImageBitmap(selectedProfilePhoto)
                        }

                        signUpBinding.signUpAddImageText.text=""
                    }
                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it){
                //Permission Granted
                val intentToGallery = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                //Permission Denied
                Toast.makeText(this@SignUpActivity , "Permission needed" , Toast.LENGTH_LONG).show()
            }
        }
    }

    //Bitmap türünde verilen fotoğrafı küçülten metod
    private fun makeSmallImage(image: Bitmap, maxSize: Int): Bitmap? {
        var wight = image.width
        var height = image.height
        val bitmapRatio = wight.toDouble() / height.toDouble()
        if (bitmapRatio > 1) {
            wight = maxSize
            height = (wight / bitmapRatio).toInt()
        } else {
            height = maxSize
            wight = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, wight, height, true)
    }

    //Bitmap türünde verilen fotoğrafı ByteArray türüne çeviren metod
    private fun bitmapToBytesArray(selectedProfilePhotoBitmap: Bitmap): ByteArray {

        val outputStream = ByteArrayOutputStream()
        selectedProfilePhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        return outputStream.toByteArray()
    }
 }