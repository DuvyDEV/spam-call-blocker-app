package com.addev.listaspam.util

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Locale

object CountryLanguageUtils {
    fun getSimCountry(context: Context): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val simCountry = telephonyManager?.simCountryIso?.takeIf { it.isNotEmpty() }
        if (!simCountry.isNullOrEmpty()) return simCountry

        val networkCountry = telephonyManager?.networkCountryIso?.takeIf { it.isNotEmpty() }
        if (!networkCountry.isNullOrEmpty()) return networkCountry

        val localeCountry = Locale.getDefault().country.takeIf { it.isNotEmpty() }
        return localeCountry ?: "us"
    }
}
