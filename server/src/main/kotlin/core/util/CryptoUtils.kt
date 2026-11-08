package core.util

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    // В продакшене этот ключ должен браться из переменных окружения (длина строго 16, 24 или 32 символа)
    private lateinit var keySpec: SecretKeySpec
    private var isInitialized = false

    fun init(secret: String) {
        require(secret.length in listOf(16, 24, 32)) {
            "Секретный ключ для AES должен быть длиной 16, 24 или 32 символа! Текущая длина: ${secret.length}"
        }
        val ALGORITHM = "AES"
        keySpec = SecretKeySpec(secret.toByteArray(), ALGORITHM)
        isInitialized = true
    }

    fun encrypt(value: String): String {
        check(isInitialized) { "CryptoUtils не был инициализирован! Вызовите init() при старте приложения." }
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val encrypted = cipher.doFinal(value.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun decrypt(encryptedValue: String): String {
        check(isInitialized) { "CryptoUtils не был инициализирован! Вызовите init() при старте приложения." }
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        val decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedValue))
        return String(decrypted)
    }
}