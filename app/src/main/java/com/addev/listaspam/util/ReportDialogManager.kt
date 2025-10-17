package com.addev.listaspam.util

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import com.addev.listaspam.R

class ReportDialogManager(private val context: Context) {

    fun show(number: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_report, null)
        val dialog = createDialog(dialogView)
        setupDialogButtons(dialog, dialogView, number)
        dialog.show()
    }

    private fun createDialog(dialogView: View): AlertDialog {
        return AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.report_title))
            .setView(dialogView)
            .setPositiveButton(context.getString(R.string.accept), null)
            .setNegativeButton(context.getString(R.string.cancel), null)
            .create()
    }

    private fun setupDialogButtons(dialog: AlertDialog, dialogView: View, number: String) {
        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                if (!validateInput(dialogView)) return@setOnClickListener
                submitReport(dialog, dialogView, number)
            }
        }
    }

    private fun validateInput(dialogView: View): Boolean {
        val messageEditText = dialogView.findViewById<EditText>(R.id.messageEditText)
        val spamRadio = dialogView.findViewById<RadioButton>(R.id.radioSpam)
        val noSpamRadio = dialogView.findViewById<RadioButton>(R.id.radioNoSpam)

        val message = messageEditText.text.toString().trim()
        val wordCount = message.split("\\s+".toRegex()).size
        val charCount = message.replace("\\s".toRegex(), "").length

        if (wordCount < 2 || charCount < 10) {
            messageEditText.error = context.getString(R.string.report_validation_message)
            return false
        }

        if (!spamRadio.isChecked && !noSpamRadio.isChecked) {
            Toast.makeText(
                context,
                context.getString(R.string.report_radio_validation),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }

    private fun submitReport(dialog: AlertDialog, dialogView: View, number: String) {
        val messageEditText = dialogView.findViewById<EditText>(R.id.messageEditText)
        val spamRadio = dialogView.findViewById<RadioButton>(R.id.radioSpam)

        val message = messageEditText.text.toString().trim()
        val isSpam = spamRadio.isChecked

        if (isSpam) {
            saveSpamNumber(context, number)
            Toast.makeText(
                context,
                context.getString(R.string.report_local_spam_confirmation, message),
                Toast.LENGTH_LONG
            ).show()
        } else {
            addNumberToWhitelist(context, number)
            Toast.makeText(
                context,
                context.getString(R.string.report_local_safe_confirmation, message),
                Toast.LENGTH_LONG
            ).show()
        }

        dialog.dismiss()
    }
}
