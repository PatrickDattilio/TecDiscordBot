package com.dattilio.tecdiscord

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest

@Serializable
data class Login(val username: String, val password: String)

fun main() {
    val json = Json(JsonConfiguration.Stable)
    val file = File("login.text")
    file.createNewFile()
    val login: Login = json.parse(Login.serializer(), file.readText())
    val okHttp = OkHttpClient()
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("uname", login.username)
        .addFormDataPart("pwd", login.password)
        .addFormDataPart("phrase", "")
        .addFormDataPart("submit", "true")
        .build()


    val loginRequest = Request.Builder()
        .url("https://login.eternalcitygame.com/login.php".toHttpUrlOrNull()!!)
        .header(
            "User-Agent",
            "'Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36'"
        )
        .header("Cookie", "biscuit=test")
        .post(requestBody)
        .build()
    val response = okHttp.newCall(loginRequest).execute()
    val responseHeaders = response.headers
    val cookies = responseHeaders.values("Set-Cookie")
    var user: String=""
    var pass: String=""
    for (cookie in cookies) {
        val userMatch = Regex("user=(.*?);").find(cookie)
        val passMatch = Regex("pass=(.*?);").find(cookie)
        if (userMatch != null) {
            user = userMatch.groupValues[1]
        }
        if (passMatch != null) {
            pass = passMatch.groupValues[1]
        }
    }

    val socket = okHttp.socketFactory.createSocket("tec.skotos.net", 6730)
    socket?.let {
        val output = it.getOutputStream().writer()
        output.write("SKOTOS Orchil 0.2.3\r\n")
        val hashString = user + pass + "NONE"
        val zealousHash = MessageDigest.getInstance("MD5").digest(hashString.toByteArray()).toHexString()

        output.write("USER $user")
        output.write("SECRET NONEt")
        output.write("HASH $zealousHash")
        output.write("CHAR ")
        output.write("")
        try {
            while (true) {
                val line = it.getInputStream().reader().readText()
                line.let {

                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            socket.close()
        }
    }
}


private val CHARS = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

/**
 *  Returns the string of two characters representing the HEX value of the byte.
 */
internal fun Byte.toHexString() : String {
    val i = this.toInt()
    val char2 = CHARS[i and 0x0f]
    val char1 = CHARS[i shr 4 and 0x0f]
    return "$char1$char2"
}

/**
 *  Returns the HEX representation of ByteArray data.
 */
internal fun ByteArray.toHexString() : String {
    val builder = StringBuilder()
    for (b in this) {
        builder.append(b.toHexString())
    }
    return builder.toString()
}


