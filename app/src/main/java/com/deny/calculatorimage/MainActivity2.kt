package com.deny.calculatorimage

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.deny.calculatorimage.databinding.ActivityMainBinding
import com.deny.calculatorimage.dialog.ProgressDialog
import com.deny.calculatorimage.util.ConverterUtil
import com.deny.calculatorimage.util.PermissionUtil
import com.deny.calculatorimage.viewmodel.MainViewModel
import com.google.android.material.shape.CornerFamily
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage

class MainActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    private companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101
    }


    private val progressDialog by lazy { ProgressDialog() }

    private lateinit var calculatorViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        calculatorViewModel = ViewModelProvider(this)[MainViewModel::class.java]


        binding.apply {
            initView()
            setUpObserver()
            setUpListener()
        }


    }

    private fun ActivityMainBinding.initView() {
        if (calculatorViewModel.imageResultUri.value == null){
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
            inputBtnImg.icon = ContextCompat.getDrawable(this@MainActivity2, R.drawable.baseline_attach_file_24)
        }

        if (useBuiltinCamera){
            inputBtnImg.icon = ContextCompat.getDrawable(this@MainActivity2, R.drawable.ic_baseline_add_a_photo_24)
        }

    }


    private fun ActivityMainBinding.setUpObserver() {
        calculatorViewModel.apply {
            resultLiveData.observe(this@MainActivity2) { evaluationResult ->
                progressDialog.dismissAllowingStateLoss()
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

            imageResultUri.observe(this@MainActivity2) { uri ->
            }
        }
    }

    private fun ActivityMainBinding.setUpListener() {
        val pickPictureFromFilesystem = resources.getBoolean(R.bool.pick_picture_from_filesystem)
        val useBuiltinCamera = resources.getBoolean(R.bool.use_builtin_camera)

        inputBtnImg.setOnClickListener {
            if (pickPictureFromFilesystem){
                requestPermissionStorage()
            } else if (useBuiltinCamera){
                requestPermissionCamera()
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

    private fun ActivityMainBinding.setResultImage(){
        imgView.setImageURI(calculatorViewModel.imageResultUri.value)
        recognizeTextFromImage()
        initView()
    }

    private fun recognizeTextFromImage(){
        progressDialog.show(supportFragmentManager, ProgressDialog.DIALOG_TAG)

        try {

            calculatorViewModel.apply {
                val inputImage = InputImage.fromFilePath(this@MainActivity2, imageResultUri.value!!)
                processImage(inputImage)
            }


        } catch (e:java.lang.Exception){
            showRedSnackbar("Failed prepare image due to ${e.message}")
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
                calculatorViewModel.imageResultUri.value = data!!.data
                binding.setResultImage()
            } else {
                showRedSnackbar("Canceled...!")
            }
    }

    private fun pickImageCamera(){
        calculatorViewModel.apply {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "Sample Title")
            values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description")

            imageResultUri.value = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageResultUri.value)
            cameraResultLauncher.launch(intent)
        }

    }

    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK){
                binding.setResultImage()
            } else {
                showRedSnackbar("Canceled...!")
            }
        }


    private fun requestPermissionStorage(){
        if (PermissionUtil.checkStoragePermission(this)){
            pickImageGallery()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE)
        }
    }

    private fun requestPermissionCamera(){
        if (PermissionUtil.checkCameraPermission(this)){
            pickImageCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_REQUEST_CODE -> {
                if (PermissionUtil.cameraRequestPermissionsResult(grantResults)){
                    pickImageCamera()
                } else {
                    showRedSnackbar("Camera and Storage permission required....!")
                }
            }
            STORAGE_REQUEST_CODE -> {
                if (PermissionUtil.storageRequestPermissionsResult(grantResults)){
                    pickImageGallery()
                } else {
                    showRedSnackbar("Storage permission required....!")
                }
            }
        }
    }


    fun showRedSnackbar(message: String) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
        val sbView = snackbar.view
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.redDot))
        val textView = sbView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        snackbar.show()
    }
}