package com.deny.calculatorimage

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieDrawable
import com.deny.calculatorimage.databinding.ActivityMainBinding
import com.deny.calculatorimage.util.ConverterUtil
import com.deny.calculatorimage.viewmodel.MainViewModel
import com.google.android.material.shape.CornerFamily
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    private companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101
    }

    private var imageUri : Uri ? = null
    private lateinit var cameraPermissions : Array<String>
    private lateinit var storagePermissions : Array<String>

    private lateinit var progress : ProgressDialog

    private lateinit var calculatorViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        calculatorViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        progress = ProgressDialog(this)
        progress.setTitle("Please wait")
        progress.setCanceledOnTouchOutside(false)



//        binding.recognizeBtnImg.setOnClickListener {
//            if (imageUri == null){
//                showToast("Pick Image First")
//            } else {
//                recognizeTextFromImage()
//            }
//        }




//        if (BuildConfig.APP_THEME == "red") {
//            // Apply red theme-specific behavior
//            // ...
//        } else if (BuildConfig.APP_THEME == "green") {
//            // Apply green theme-specific behavior
//            // ...
//        }
//
//        if (BuildConfig.UI_FUNCTIONALITY == "pickPicture") {
//            // Apply pick picture functionality-specific behavior
//            // ...
//        } else if (BuildConfig.UI_FUNCTIONALITY == "useCamera") {
//            // Apply use camera functionality-specific behavior
//            // ...
//        }

        binding.apply {
            initView()
            setUpObserver()
            setUpListener()
        }


    }

    private fun ActivityMainBinding.initView() {
        if (imageUri == null){
            animationView.isVisible = true
            animationView2.isGone = true
            viewResult.isGone = true
        } else {
            animationView.isGone = true
            animationView2.isVisible = true
            viewResult.isVisible = true
        }

        viewHeaderBtn.apply {
            shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, ConverterUtil.dpToPx(context, 24f))
                .setTopRightCorner(CornerFamily.ROUNDED, ConverterUtil.dpToPx(context, 24f))
                .setBottomLeftCorner(CornerFamily.ROUNDED, ConverterUtil.dpToPx(context, 0f))
                .setBottomRightCorner(CornerFamily.ROUNDED, ConverterUtil.dpToPx(context, 0f))
                .build()
        }

        val pickPictureFromFilesystem = resources.getBoolean(R.bool.pick_picture_from_filesystem)
        val useBuiltinCamera = resources.getBoolean(R.bool.use_builtin_camera)

        if (pickPictureFromFilesystem){
            inputBtnImg.icon = ContextCompat.getDrawable(this@MainActivity2, R.drawable.ic_baseline_image_24)
        }

        if (useBuiltinCamera){
            inputBtnImg.icon = ContextCompat.getDrawable(this@MainActivity2, R.drawable.ic_baseline_add_a_photo_24)
        }

    }

    private fun ActivityMainBinding.setUpObserver() {
        calculatorViewModel.apply {
            resultLiveData.observe(this@MainActivity2) { evaluationResult ->
                progress.dismiss()
                txtResult.text = "$evaluationResult"
                validState(evaluationResult != null)
            }

            resultInputLiveData.observe(this@MainActivity2) { expression ->
                if (expression != null){
                    val parts = expression.split(Regex("[-+*/]"))
                    val operator = expression[parts[0].length].toString()
                    val num1 = parts[0]
                    val num2 = parts[1]

                    txtChar1.text = num1
                    txtChar2.text = num2
                    txtExpression.text = operator
                }
                validState(expression != null)
            }
        }
    }

    private fun ActivityMainBinding.setUpListener() {
        val pickPictureFromFilesystem = resources.getBoolean(R.bool.pick_picture_from_filesystem)
        val useBuiltinCamera = resources.getBoolean(R.bool.use_builtin_camera)

        inputBtnImg.setOnClickListener {
            if (pickPictureFromFilesystem){
                if (checkStoragePermission()){
                    pickImageGallery()
                } else {
                    requestPermissionStorage()
                }
            } else if (useBuiltinCamera){
                if (checkCameraPermission()){
                    pickImageCamera()
                }
                else {
                    requestPermissionCamera()
                }
            }
        }
    }

    private fun ActivityMainBinding.validState(valid : Boolean){
        if (valid){
            txtError.isGone = true
            viewValid.isVisible = true
        } else {
            txtError.isVisible = true
            viewValid.isGone = true
        }
        txtError.text = "Failed recognize text or invalid expression"
    }

    private fun recognizeTextFromImage(){
        progress.setMessage("Preparing Image")
        progress.show()

        try {

            val inputImage = InputImage.fromFilePath(this, imageUri!!)
            progress.setMessage("Recognizing Text")

            calculatorViewModel.processImage(inputImage)

//            val textTaskResult = textTecognizer.process(inputImage)
//                .addOnSuccessListener {
//                    progress.dismiss()
//                    Log.e("Catch", "Catch Text : ${it.text}")
//                    val result = extractExpression(it)
//                    if (result != null) {
//                        val expression = result.first
//                        //Log.e("Catch", "expression : ${expression}")
//                        val evaluationResult = evaluateExpression(expression)
//                        binding.edView.setText(" \nResult : $evaluationResult")
//                    } else {
//                        binding.edView.setText("No expression found.")
//                    }
//                }
//                .addOnFailureListener {
//                    progress.dismiss()
//                    showToast("Failed recognize text due to ${it.message}")
//                }

        } catch (e:java.lang.Exception){
            showToast("Failed prepare image due to ${e.message}")
        }
    }


    private fun pickImageGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryResultLauncher.launch(intent)
    }

    private val galleryResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->

            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data
                binding.apply {
                    binding.imgView.setImageURI(imageUri)
                    recognizeTextFromImage()
                    initView()
                }
            } else {
                showToast("Canceled...!")
            }
    }

    private fun pickImageCamera(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Sample Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraResultLauncher.launch(intent)
    }

    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK){
                binding.apply {
                    imgView.setImageURI(imageUri)
                    recognizeTextFromImage()
                    initView()
                }

            } else {
                showToast("Canceled...!")
            }
        }

    private fun checkStoragePermission() : Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkCameraPermission() : Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionStorage(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE)
    }

    private fun requestPermissionCamera(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()){
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if (cameraAccepted && storageAccepted){
                        pickImageCamera()
                    } else {
                        showToast("Camera and Storage permission required....!")
                    }
                }
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()){
                    val storarageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (storarageAccepted){
                        pickImageGallery()
                    } else {
                        showToast("Storage permission required....!")
                    }
                }
            }
        }
    }



    private fun showToast(message : String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}