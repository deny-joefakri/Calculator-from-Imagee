package com.deny.calculatorimage.permission

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.deny.calculatorimage.MainActivity.Companion.CAMERA_REQUEST_CODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

suspend fun AppCompatActivity.requestCameraPermission(): Boolean = withContext(Dispatchers.Main) {
    val permissionCamera = Manifest.permission.CAMERA
    val permissionStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val granted = PackageManager.PERMISSION_GRANTED

    if (ContextCompat.checkSelfPermission(this@requestCameraPermission, permissionCamera) == granted
        && ContextCompat.checkSelfPermission(this@requestCameraPermission, permissionStorage) == granted) {
        true
    } else {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this@requestCameraPermission, permissionCamera)
            || ActivityCompat.shouldShowRequestPermissionRationale(this@requestCameraPermission, permissionStorage)) {

            ActivityCompat.requestPermissions(this@requestCameraPermission, arrayOf(permissionCamera, permissionStorage), CAMERA_REQUEST_CODE)
        }

        val result = suspendCancellableCoroutine<Boolean> { continuation ->

            ActivityCompat.requestPermissions(this@requestCameraPermission, arrayOf(permissionCamera, permissionStorage), CAMERA_REQUEST_CODE)

            continuation.invokeOnCancellation { // If the coroutine is cancelled
                ActivityCompat.checkSelfPermission(
                    this@requestCameraPermission,
                    permissionCamera
                )
                ActivityCompat.checkSelfPermission(
                    this@requestCameraPermission,
                    permissionStorage
                )
            }
        }

        result
    }
}