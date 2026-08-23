package com.sujonmax.diary.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

class PinStore(context: Context) {
    private val prefs = context.getSharedPreferences("secure_lock", Context.MODE_PRIVATE)
    private val random = SecureRandom()
    val configured: Boolean get() = prefs.contains("hash")
    val lockedUntil: Long get() = prefs.getLong("locked_until", 0L)
    fun setPin(pin: String) {
        require(pin.length in 4..8 && pin.all(Char::isDigit))
        val salt = ByteArray(16).also(random::nextBytes)
        prefs.edit().putString("salt", b64(salt)).putString("hash", b64(hash(pin, salt)))
            .putInt("failures", 0).putLong("locked_until", 0L).apply()
    }
    fun verify(pin: String): Boolean {
        if (System.currentTimeMillis() < lockedUntil) return false
        val salt = prefs.getString("salt", null)?.let(::fromB64) ?: return false
        val ok = MessageDigest.isEqual(hash(pin, salt), fromB64(prefs.getString("hash", "")!!))
        if (ok) prefs.edit().putInt("failures", 0).apply() else {
            val failures = prefs.getInt("failures", 0) + 1
            val lock = if (failures >= 5) System.currentTimeMillis() + 30_000L else 0L
            prefs.edit().putInt("failures", failures).putLong("locked_until", lock).apply()
        }
        return ok
    }
    private fun hash(pin: String, salt: ByteArray) =
        MessageDigest.getInstance("SHA-256").digest(salt + pin.toByteArray(Charsets.UTF_8))
    private fun b64(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun fromB64(value: String) = Base64.decode(value, Base64.NO_WRAP)
}