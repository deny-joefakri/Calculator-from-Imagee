package com.deny.calculatorimage.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.deny.calculatorimage.R

class ProgressDialog : DialogFragment() {

    companion object {
        const val DIALOG_TAG = "BaseProgressDialogFragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.MainAlertDialog)
            val inflater = requireActivity().layoutInflater
            val dialogView = inflater.inflate(R.layout.progress_dialog, null)

            builder.setView(dialogView)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val sizeWidth = resources.getDimensionPixelSize(R.dimen.mud_dimens_150dp)
        val sizeHeight = resources.getDimensionPixelSize(R.dimen.mud_dimens_120dp)
        dialog?.window?.setLayout(sizeWidth, sizeHeight)
    }
}
