package ru.skillbranch.kotlinexample

import org.junit.After
import org.junit.Assert
import org.junit.Test
import ru.skillbranch.kotlinexample.extensions.dropLastUntil

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    /**
    Добавьте метод в UserHolder для очистки значений UserHolder после выполнения каждого теста,
    это необходимо чтобы тесты можно было запускать одновременно

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder(){
    map.clear()
    }
     */
    @After
    fun after() {
        UserHolder.clearHolder()
    }

    @Test
    fun register_user_success() {
        val holder = UserHolder
        val user = holder.registerUser("John Doe", "John_Doe@unknown.com", "testPass")
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: john_doe@unknown.com
            fullName: John Doe
            initials: J D
            email: John_Doe@unknown.com
            phone: null
            meta: {auth=password}
        """.trimIndent()

        Assert.assertEquals(expectedInfo, user.userInfo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_blank() {
        val holder = UserHolder
        holder.registerUser("", "John_Doe@unknown.com", "testPass")
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_illegal_name() {
        val holder = UserHolder
        holder.registerUser("John Jr Doe", "John_Doe@unknown.com", "testPass")
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_illegal_exist() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com", "testPass")
        holder.registerUser("John Doe", "John_Doe@unknown.com", "testPass")
    }

    @Test
    fun register_user_by_phone_success() {
        val holder = UserHolder
        val user = holder.registerUserByPhone("John Doe", "+7 (917) 971 11-11")
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: +79179711111
            fullName: John Doe
            initials: J D
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        Assert.assertEquals(expectedInfo, user.userInfo)
        Assert.assertNotNull(user.accessCode)
        Assert.assertEquals(6, user.accessCode?.length)
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_by_phone_fail_blank() {
        val holder = UserHolder
        holder.registerUserByPhone("", "+7 (917) 971 11-11")
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_by_phone_fail_illegal_name() {
        val holder = UserHolder
        holder.registerUserByPhone("John Doe", "+7 (XXX) XX XX-XX")
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_failby_phone_illegal_exist() {
        val holder = UserHolder
        holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
        holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
    }

    @Test
    fun login_user_success() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com", "testPass")
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: john_doe@unknown.com
            fullName: John Doe
            initials: J D
            email: John_Doe@unknown.com
            phone: null
            meta: {auth=password}
        """.trimIndent()

        val successResult = holder.loginUser("john_doe@unknown.com", "testPass")

        Assert.assertEquals(expectedInfo, successResult)
    }

    @Test
    fun login_user_by_phone_success() {
        val holder = UserHolder
        val user = holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: +79179711111
            fullName: John Doe
            initials: J D
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        val successResult = holder.loginUser("+7 (917) 971-11-11", user.accessCode!!)

        Assert.assertEquals(expectedInfo, successResult)
    }

    @Test
    fun login_user_fail() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com", "testPass")

        val failResult = holder.loginUser("john_doe@unknown.com", "test")

        Assert.assertNull(failResult)
    }

    @Test
    fun login_user_not_found() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com", "testPass")

        val failResult = holder.loginUser("john_cena@unknown.com", "test")

        Assert.assertNull(failResult)
    }

    @Test
    fun request_access_code() {
        val holder = UserHolder
        val user = holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
        val oldAccess = user.accessCode
        holder.requestAccessCode("+7 (917) 971-11-11")

        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: +79179711111
            fullName: John Doe
            initials: J D
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        val successResult = holder.loginUser("+7 (917) 971-11-11", user.accessCode!!)

        Assert.assertNotEquals(oldAccess, user.accessCode!!)
        Assert.assertEquals(expectedInfo, successResult)
    }

    @Test
    fun import_csv_file() {
        val users: List<String> = listOf(
            "John Doe;JohnDoe@unknow.com;[B@1f54bcc7:ee3a4a26aa61b10184a457b2b0ba8627;;",
            "John Stone;;[B@32d43b68:dabf96f836c987d52c9c41ceaad18235;+7 (848) 239-50-85;",
            "Ponnappa;Ponnappa@unknown.com;[B@5929b2e0:758dc3ac4488ef9156deb5a2aff3e3d8;+7 (843) 054-48-00;",
            "Mia Wong;MiaWong@unknown.com;[B@1e472364:5a3501f291ac14c259a0ffc7bd0b7c1b;;",
            "Peter Stanbridge;PeterStanbridge@unknown.com;[B@bcaccb4:cb808df91dcba7c1a0f520dcc1a65542;+7 (068) 917-73-36;",
            "Natalie Lee-Walsh;NatalieLee-Walsh@unknown.com;[B@18d6b9de:96cc96934c85ea5046b1394045083838;;",
            "Ang;;[B@cfcb696:8e842e66f206e845b0a9731f988fc384;+7 (170) 735-54-84;",
            "Nguta Ithya;NgutaIthya@unknown.com;[B@2b87f86e:d6d9302a65291e1cfae913ff9ba11acb;+7 (268) 621-14-16;",
            "Tamzyn French;TamzynFrench@unknown.com;[B@76752ec6:bd66dab31d0198e7b2aef64d91a5349c;;",
            "Salome Simoes;SalomeSimoes@unknown.com;[B@5bc0619f:e979c66a0e35e0f53cb7b4b2bd575728;+7 (283) 815-86-06;",
            "Trevor;;[B@32e12477:263590ba435d4b341727e60b6a8b5aed;+7 (022) 256-45-40;",
            "Tarryn Campbell-Gillies;TarrynCampbell-Gillies@unknown.com;[B@2631eecd:93b3d7dd87254b4edbfbd4afdc1da199;;",
            "Eugenia Anders;EugeniaAnders@unknown.com;[B@796de7d3:e7429518845a8369204b7ef116d60d69;+7 (122) 513-65-81;",
        )

        val holder = UserHolder
        val importedUsers = holder.importUsers(users)

        val expectedInfoPhone = """
            firstName: John
            lastName: Stone
            login: +78482395085
            fullName: John Stone
            initials: J S
            email: null
            phone: +78482395085
            meta: {src=csv}
        """.trimIndent()

        val expectedInfoMail = """
            firstName: John
            lastName: Doe
            login: johndoe@unknow.com
            fullName: John Doe
            initials: J D
            email: JohnDoe@unknow.com
            phone: null
            meta: {src=csv}
        """.trimIndent()

        val successResultPhone = importedUsers[1].userInfo
        val successResultMail = holder.loginUser("JohnDoe@unknow.com", "QhQcIT")

        Assert.assertEquals(expectedInfoPhone, successResultPhone)
        Assert.assertEquals(expectedInfoMail, successResultMail)
    }

    @Test
    fun drop_last_until() {
        val expectedInfo = listOf(1)
        val expectedInfo2 = listOf("House", "Nymeros", "Martell")

        val successResult = listOf(1, 2, 3).dropLastUntil { it == 2 }
        val successResult2 = "House Nymeros Martell of Sunspear".split(" ")
            .dropLastUntil { it == "of" }

        Assert.assertEquals(expectedInfo, successResult)
        Assert.assertEquals(expectedInfo2, successResult2)
    }
}
