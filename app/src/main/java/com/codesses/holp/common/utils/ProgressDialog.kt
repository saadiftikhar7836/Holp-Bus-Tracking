package com.codesses.holp.common.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.codesses.holp.R


class ProgressDialog(private val mContext: Context) {

    private lateinit var progressDialog: Dialog


    // Show progress dialog
    fun show() {
        progressDialog = Dialog(mContext)

        progressDialog.setContentView(R.layout.custom_dialog_progress)

        if (progressDialog.window != null) progressDialog.window!!.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        progressDialog.setCancelable(false)
        if (this::progressDialog.isInitialized && !progressDialog.isShowing)
            progressDialog.show()

    }

    // Dismiss dialog
    fun dismiss() {
        if (this::progressDialog.isInitialized && progressDialog.isShowing)
            progressDialog.dismiss()
    }

}