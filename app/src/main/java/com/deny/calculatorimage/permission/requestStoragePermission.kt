package com.deny.calculatorimage.permission

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.deny.calculatorimage.MainActivity2.Companion.CAMERA_REQUEST_CODE
import com.deny.calculatorimage.MainActivity2.Companion.STORAGE_REQUEST_CODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

suspend fun AppCompatActivity.requestStoragePermission(): Boolean = withContext(Dispatchers.Main) {
    val permissionStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val granted = PackageManager.PERMISSION_GRANTED

    if (ContextCompat.checkSelfPermission(this@requestStoragePermission, permissionStorage) == granted) {
        true
    } else {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this@requestStoragePermission, permissionStorage)) {

            ActivityCompat.requestPermissions(this@requestStoragePermission, arrayOf(permissionStorage), STORAGE_REQUEST_CODE)
        }

        val result = suspendCancellableCoroutine<Boolean> { continuation ->

            ActivityCompat.requestPermissions(this@requestStoragePermission, arrayOf(permissionStorage), STORAGE_REQUEST_CODE)

            continuation.invokeOnCancellation { // If the coroutine is cancelled
                ActivityCompat.checkSelfPermission(
                    this@requestStoragePermission,
                    permissionStorage
                )
            }
        }

        result
    }
}