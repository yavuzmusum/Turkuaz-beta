package com.turkuaz.beta.data

import android.content.Context
import android.provider.Settings
import java.security.MessageDigest

/**
 * Bolum 0.2: cihaz parmak izi. MVP: Android'in ANDROID_ID degeri
 * (uygulama+imza+kullanici basina kararli) SHA-256 ile hashlenip
 * kullaniliyor. TODO (production): daha guclu bir donanim kimligi /
 * attestation mekanizmasi degerlendirin (ANDROID_ID fabrika sifirlamada
 * degisebilir).
 */
object DeviceId {
    fun get(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-device"
        val digest = MessageDigest.getInstance("SHA-256").digest(androidId.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
