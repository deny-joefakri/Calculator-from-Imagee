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
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.deny.calculatorimage.databinding.ActivityMainBinding
import com.deny.calculatorimage.viewmodel.MainViewModel
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


        binding.inputBtnImg.setOnClickListener {
            showInputImageDialog()
        }

        binding.recognizeBtnImg.setOnClickListener {
            if (imageUri == null){
                showToast("Pick Image First")
            } else {
                recognizeTextFromImage()
            }
        }

        calculatorViewModel.resultLiveData.observe(this) { evaluationResult ->
            progress.dismiss()
            binding.edView.setText(" \nResult : $evaluationResult")
        }
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

    private fun showInputImageDialog(){

        val popupMenu = PopupMenu(this, binding.inputBtnImg)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 3, "Gallery")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            val id = it.itemId
            if (id == 1){
                if (checkCameraPermission()){
                    pickImageCamera()
                }
                else {
                    requestPermissionCamera()
                }
            } else if (id == 2) {
                if (checkStoragePermission()){
                    pickImageGallery()
                } else {
                    requestPermissionStorage()
                }
            }
            return@setOnMenuItemClickListener true
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

                binding.imgView.setImageURI(imageUri)
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
                binding.imgView.setImageURI(imageUri)
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