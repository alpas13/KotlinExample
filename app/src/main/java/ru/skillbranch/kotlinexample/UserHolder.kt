package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*

object UserHolder {
    private val map = mutableMapOf<String, User>()
    private const val fileName = "src/main/java/ru/skillbranch/kotlinexample/users.csv"

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User {
        if (checkIsUserNotExist(email)) {
            return User.makeUser(fullName, email, password)
                .also { user -> map[user.login] = user }
        } else
            throw IllegalArgumentException("User with this email already exist")
    }

    fun registerUserByPhone(
        fullName: String,
        phone: String,
    ): User {
        return when {
            !checkIsNotIllegalPhone(phone) ->
                throw IllegalArgumentException("Phone number is not correct")
            !checkIsUserNotExist(phone = phone) ->
                throw IllegalArgumentException("User with this phone number already exist")
            else ->
                User.makeUser(fullName = fullName, phone = phone)
                    .also { user -> map[user.login] = user }
        }
    }

    fun requestAccessCode(rawPhone: String) {
        val phone = cleanPhoneNumber(rawPhone)
        if (!checkIsUserNotExist(phone))
            map[phone]?.updateRequestCode()
        else throw IllegalArgumentException("User with this phone number does not exist")
    }

    fun loginUser(rawLogin: String, password: String): String? {
        val isLoginPhone = cleanPhoneNumber(rawLogin)
            .matches("^\\+\\d*".toRegex())
        val login = if (isLoginPhone) cleanPhoneNumber(rawLogin) else rawLogin.trim().lowercase()

        return map[login]?.let { user ->
            when {
                !isLoginPhone && user.checkPassword(password) ||
                        isLoginPhone && password == user.accessCode ->
                    user.userInfo
                else -> null
            }
        }
    }

    fun importUsersFromFile(): Int {
        File(fileName).forEachLine { line ->
            val (fullName, email, phone) = line.split(',')
            if (email.isNotBlank() && email != "email") {
                val tempPass = Date().toString()
                registerUser(fullName, email, tempPass)
            } else if (phone.isNotBlank() && phone != "phone") {
                registerUserByPhone(fullName, phone)
            }
        }

        return map.size
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder() {
        map.clear()
    }

    private fun checkIsUserNotExist(email: String? = null, phone: String? = null): Boolean {
        val login: String = if (!phone.isNullOrBlank()) {
            cleanPhoneNumber(phone)
        } else
            email!!.trim().lowercase()

        return map[login] == null
    }

    private fun cleanPhoneNumber(phone: String): String {
        return phone.replace("[^+\\d]".toRegex(), "")
    }

    private fun checkIsNotIllegalPhone(phone: String): Boolean {
        return cleanPhoneNumber(phone).length == 12
    }
}