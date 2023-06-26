package com.deny.calculatorimage

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
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
import com.deny.calculatorimage.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101
    }

    private var imageUri : Uri ? = null
    private lateinit var cameraPermissions : Array<String>
    private lateinit var storagePermissions : Array<String>

    private lateinit var progress : ProgressDialog

    private lateinit var textTecognizer : TextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        progress = ProgressDialog(this)
        progress.setTitle("Please wait")
        progress.setCanceledOnTouchOutside(false)

        textTecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        binding.inputBtnImg.setOnClickListener {
            showInputImageDialog()
        }
//
//        binding.recognizeBtnImg.setOnClickListener {
//            if (imageUri == null){
//                showToast("Pick Image First")
//            } else {
//                recognizeTextFromImage()
//            }
//        }
    }

    private fun recognizeTextFromImage(){
        progress.setMessage("Preparing Image")
        progress.show()

        try {

            val inputImage = InputImage.fromFilePath(this, imageUri!!)
            progress.setMessage("Recognizing Text")

            val textTaskResult = textTecognizer.process(inputImage)
                .addOnSuccessListener {
                    progress.dismiss()
                    val recognizedText = it.text

                    processTextRecognitionResult(it);

                }
                .addOnFailureListener {
                    progress.dismiss()
                    showToast("Failed recognize text due to ${it.message}")
                }

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

//                binding.imgView.setImageURI(imageUri)
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
//                binding.imgView.setImageURI(imageUri)
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

    private fun calculate(expression: String): String {
        // Perform the calculation logic here and return the result as a string
        // You can use libraries like Kotlin Expression Parser or evaluate the expression manually

        // For demonstration, let's assume a simple evaluation of the expression
        return try {
            val result = eval(expression)
            result.toInt().toString()
        } catch (e: Exception) {
            "Error: Invalid expression"
        }
    }

    private fun eval(expression: String): Double {
        // Evaluate the expression and return the result as a double
        // Custom implementation is required based on the chosen expression parser or evaluator
        // This is just a placeholder for demonstration purposes

        val parts = expression.split("+", "-", "*", "X","x", "/", ":")
        val num1 = parts[0].toDouble()
        val num2 = parts[1].toDouble()
        return when {
            expression.contains("+") -> num1 + num2
            expression.contains("-") -> num1 - num2
            expression.contains("*") -> num1 * num2
            expression.contains("X") -> num1 * num2
            expression.contains("/") -> num1 / num2
            expression.contains(":") -> num1 / num2
            else -> throw IllegalArgumentException("Invalid expression")
        }
    }

    private fun processTextRecognitionResult(texts: Text) {
        val blocks: List<Text.TextBlock> = texts.textBlocks
        if (blocks.isEmpty()) {
            showToast("No text found")
            return
        }

        Log.e("blocks", "Result blocks : ${blocks.size}")
        for (i in blocks.indices) {
            val lines: List<Text.Line> = blocks[i].lines
            Log.e("lines", "Result lines : ${blocks[i].text}")
            if (i == 0){
                val result = calculate(blocks[i].text)
//                binding.edView.setText("Input in Line ${i+1} : ${blocks[i].text} \nResult : ${result}")
            }
        }



    }


    private fun showToast(message : String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}