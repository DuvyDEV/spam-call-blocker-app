package com.addev.listaspam.util

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.Toast
import com.addev.listaspam.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportDialogManager(private val context: Context) {

    fun show(number: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_report, null)
        val dialog = createDialog(dialogView)
        setupDialogView(dialogView)
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

    private fun setupDialogView(dialogView: View) {
        val messageEditText = dialogView.findViewById<EditText>(R.id.messageEditText)
        val spamRadio = dialogView.findViewById<RadioButton>(R.id.radioSpam)
        val noSpamRadio = dialogView.findViewById<RadioButton>(R.id.radioNoSpam)
        val checkboxUnknownPhone = dialogView.findViewById<CheckBox>(R.id.checkboxUnknownPhone)
        val checkboxTellows = dialogView.findViewById<CheckBox>(R.id.checkboxTellows)

        messageEditText.hint = context.getString(R.string.report_hint)
        spamRadio.text = context.getString(R.string.report_spam)
        noSpamRadio.text = context.getString(R.string.report_not_spam)

        checkboxUnknownPhone.text = buildProviderText("UnknownPhone", getLanguageDisplayName())
        checkboxTellows.text = buildProviderText("Tellows", getCountryDisplayName())
    }

    private fun setupDialogButtons(dialog: AlertDialog, dialogView: View, number: String) {
        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                if (!validateInput(dialogView)) return@setOnClickListener

                val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
                progressBar.visibility = View.VISIBLE
                button.isEnabled = false

                submitReport(dialog, dialogView, number, progressBar, button)
            }
        }
    }

    private fun validateInput(dialogView: View): Boolean {
        val messageEditText = dialogView.findViewById<EditText>(R.id.messageEditText)
        val spamRadio = dialogView.findViewById<RadioButton>(R.id.radioSpam)
        val noSpamRadio = dialogView.findViewById<RadioButton>(R.id.radioNoSpam)
        val checkboxUnknownPhone = dialogView.findViewById<CheckBox>(R.id.checkboxUnknownPhone)
        val checkboxTellows = dialogView.findViewById<CheckBox>(R.id.checkboxTellows)

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

        if (!checkboxUnknownPhone.isChecked && !checkboxTellows.isChecked) {
            Toast.makeText(
                context,
                context.getString(R.string.select_at_least_one_provider),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }

    private fun submitReport(
        dialog: AlertDialog,
        dialogView: View,
        number: String,
        progressBar: ProgressBar,
        button: Button
    ) {
        val messageEditText = dialogView.findViewById<EditText>(R.id.messageEditText)
        val spamRadio = dialogView.findViewById<RadioButton>(R.id.radioSpam)
        val checkboxUnknownPhone = dialogView.findViewById<CheckBox>(R.id.checkboxUnknownPhone)
        val checkboxTellows = dialogView.findViewById<CheckBox>(R.id.checkboxTellows)

        val message = messageEditText.text.toString().trim()
        val isSpam = spamRadio.isChecked

        CoroutineScope(Dispatchers.IO).launch {
            val reportedTo = mutableListOf<String>()

            if (checkboxUnknownPhone.isChecked) {
                getListaSpamApiLang(context)?.let { lang ->
                    if (ApiUtils.reportToUnknownPhone(number, message, isSpam, lang)) {
                        reportedTo.add("UnknownPhone")
                    }
                }
            }

            if (checkboxTellows.isChecked) {
                getTellowsApiCountry(context)?.let { country ->
                    if (ApiUtils.reportToTellows(number, message, isSpam, country)) {
                        reportedTo.add("Tellows")
                    }
                }
            }

            val reportMessage = buildReportMessage(reportedTo)

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                button.isEnabled = true
                Toast.makeText(context, reportMessage, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }

    private fun buildReportMessage(reportedTo: List<String>): String {
        return if (reportedTo.isNotEmpty()) {
            context.getString(R.string.report_success_prefix) + " " +
                    reportedTo.joinToString(" " + context.getString(R.string.and) + " ")
        } else {
            context.getString(R.string.report_failure)
        }
    }

    private fun getLanguageDisplayName(): String {
        val lang = getListaSpamApiLang(context)
        val langValues = context.resources.getStringArray(R.array.language_values)
        val langNames = context.resources.getStringArray(R.array.language_names)
        return getDisplayName(lang, langValues, langNames) ?: "Unknown"
    }

    private fun getCountryDisplayName(): String {
        val country = getTellowsApiCountry(context)
        val countryValues = context.resources.getStringArray(R.array.entryvalues_region_preference)
        val countryNames = context.resources.getStringArray(R.array.entries_region_preference)
        return getDisplayName(country, countryValues, countryNames) ?: "Desconocido"
    }

    private fun getDisplayName(
        value: String?,
        values: Array<String>,
        names: Array<String>
    ): String? {
        return value?.let {
            val index = values.indexOf(it)
            if (index in names.indices) names[index] else it
        }
    }

    private fun buildProviderText(providerName: String, displayName: String): String {
        return "$providerName ($displayName)"
    }
}
