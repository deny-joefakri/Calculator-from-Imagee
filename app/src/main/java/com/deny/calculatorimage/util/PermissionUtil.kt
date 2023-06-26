package com.deny.calculatorimage.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtil {

    fun checkStoragePermission(context : Context) : Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun checkCameraPermission(context : Context) : Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun cameraRequestPermissionsResult(grantResults: IntArray) : Boolean {
        return if (grantResults.isNotEmpty()){
            val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

            cameraAccepted && storageAccepted
        } else {
            false
        }
    }

    fun storageRequestPermissionsResult(grantResults: IntArray) : Boolean {
        return if (grantResults.isNotEmpty()){
            val storarageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            storarageAccepted
        } else {
            false
        }
    }

}