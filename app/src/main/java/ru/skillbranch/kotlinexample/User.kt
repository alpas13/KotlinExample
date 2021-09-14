package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
) {
    val userInfo: String

    private val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }

    private val initials: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ") { it.first().uppercase() }

    private var phone: String? = null
        set(value) {
            field = value?.replace("[^+\\d]".toRegex(), "")
        }

    private var _login: String? = null
    internal var login: String
        set(value) {
            _login = value?.lowercase()
        }
        get() = _login!!

    private lateinit var passwordHash: String

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null

    private var hashPassPrefix: String? = null

    private val salt: String by lazy {
        setSalt()
    }

    // for mail
    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ) : this(
        firstName,
        lastName,
        email = email,
        meta = mapOf("auth" to "password")
    ) {
        println("Secondary mail constructor")
        passwordHash = encrypt(password)
    }

    // for import
    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        hashPrefix: String,
        hashPassword: String
    ) : this(
        firstName,
        lastName,
        email = email,
        meta = mapOf("src" to "csv")
    ) {
        println("Import mail constructor")
        hashPassPrefix = hashPrefix
        passwordHash = hashPassword
    }

    // for phone
    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String,
    ) : this(
        firstName,
        lastName,
        rawPhone = rawPhone,
        meta = mapOf("auth" to "sms")
    ) {
        println("Secondary phone constructor")
        val code = generateAccessCode()
        accessCode = code
        sendAccessCodeToUser(phone, code)
    }

    init {
        println("First init block, primary constructor was called")

        check(firstName.isNotBlank()) { "Firstname must be a not blank" }
        check(!email.isNullOrBlank() || !rawPhone.isNullOrBlank()) {
            "Email or phone must be a not blank"
        }

        phone = rawPhone
        login = email ?: phone!!

        userInfo = """
        firstName: $firstName
        lastName: $lastName
        login: $login
        fullName: $fullName
        initials: $initials
        email: $email
        phone: $phone
        meta: $meta
        """.trimIndent()
    }

    fun checkPassword(pass: String) = encrypt(pass) == passwordHash

    fun changePassword(oldPassword: String, newPassword: String) {
        if (checkPassword(oldPassword)) passwordHash = encrypt(newPassword)
        else throw IllegalArgumentException(
            "The entered password does not match the current password"
        )
    }

    fun updateRequestCode() {
        val newCode = generateAccessCode()
        accessCode = newCode
        sendAccessCodeToUser(phone, newCode)
    }

    private fun setSalt(): String {
        return if (!hashPassPrefix.isNullOrBlank()) hashPassPrefix!!
        else ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
    }

    private fun encrypt(password: String) = salt.plus(password).md5()

    private fun generateAccessCode(): String {
        val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

        return StringBuilder().apply {
            repeat(6) {
                possible.indices.random().also { idx ->
                    append(possible[idx])
                }
            }
        }.toString()
    }

    private fun sendAccessCodeToUser(phone: String?, code: String) {
        println("...send access code: $code on user phone: $phone")
    }

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }

    companion object Factory {
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            hashPrefix: String? = null,
            hashPassword: String? = null,
            phone: String? = null,
        ): User {
            val (firstName, lastName) = fullName.fullNameToPair()

            return when {
                !phone.isNullOrBlank() -> User(firstName, lastName, phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(
                    firstName,
                    lastName,
                    email,
                    password
                )
                !email.isNullOrBlank() &&
                        !hashPrefix.isNullOrBlank() &&
                        !hashPassword.isNullOrBlank() -> User(
                    firstName,
                    lastName,
                    email,
                    hashPrefix,
                    hashPassword
                )
                else -> throw IllegalArgumentException("Email or phone must not be null or blank")
            }
        }

        private fun String.fullNameToPair(): Pair<String, String?> {
            return this.split(" ")
                .filter { it.isNotBlank() }
                .run {
                    when (size) {
                        1 -> first() to null
                        2 -> first() to last()
                        else -> throw IllegalArgumentException(
                            "Fullname must contain only first" +
                                    " and last name, current split is: ${this@fullNameToPair}"
                        )
                    }
                }
        }
    }
}
