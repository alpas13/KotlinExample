package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.lang.IllegalArgumentException

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User {
        if (checkIsUserNotExist(email)) {
            return User.makeUser(fullName, email, password)
                .also { user -> map[user.login] = user }
        } else
            throw IllegalArgumentException("A user with this email already exists")
    }

    fun registerUserByPhone(
        fullName: String,
        phone: String,
        import: Boolean? = null,
    ): User {
        return when {
            !checkIsNotIllegalPhone(phone) ->
                throw IllegalArgumentException("Phone number is not correct")
            !checkIsUserNotExist(phone = phone) ->
                throw IllegalArgumentException("A user with this phone already exists")
            else ->
                User.makeUser(fullName = fullName, phone = phone, import = import)
                    .also { user -> map[user.login] = user }
        }
    }

    fun requestAccessCode(rawPhone: String) {
        val phone = cleanPhoneNumber(rawPhone)
        if (!checkIsUserNotExist(phone))
            map[phone]?.updateRequestCode()
        else throw IllegalArgumentException("A user with this phone number does not exist")
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

    fun importUsers(users: List<String>): List<User> {
        val importedUsers = mutableListOf<User>()
        users.map { line ->
            val (fullName, email, password, phone) = line.split(';')
                .map { value -> if (value.isNotBlank()) value else null }

            val isFullUserData = !fullName.isNullOrBlank() &&
                    !email.isNullOrBlank() &&
                    !password.isNullOrBlank()

            if (isFullUserData) {
                val (hashPrefix, hashPassword) = password!!.split(':')

                importedUsers.add(registerImportedUsers(fullName!!, email!!, hashPrefix, hashPassword))
            } else if (!fullName.isNullOrBlank() && !phone.isNullOrBlank()) {
                importedUsers.add(registerUserByPhone(fullName, phone, true))
            } else {
                throw IllegalArgumentException("User must not have ")
            }
        }

        return importedUsers.map { it }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder() {
        map.clear()
    }

    private fun registerImportedUsers(
        fullName: String,
        email: String,
        hashPrefix: String,
        hashPassword: String
    ): User {
        if (checkIsUserNotExist(email)) {
            return User.makeUser(
                fullName,
                email,
                hashPrefix = hashPrefix,
                hashPassword = hashPassword
            )
                .also { user -> map[user.login] = user }
        } else
            throw IllegalArgumentException("A user with this email already exists")
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
