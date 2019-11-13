package com.wilimasy.flutter_tbs
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.content.FileProvider
import java.io.File
import java.util.HashMap

class OpenNativeFileUtils {

    var map: MutableMap<String, String>

    init {
        map = HashMap()
        map[".3gp"] = "video/3gpp"
        map[".apk"] = "application/vnd.android.package-archive"
        map[".asf"] = "video/x-ms-asf"

        map[".avi"] = "video/x-msvideo"
        map[".bin"] = "application/octet-stream"
        map[".bmp"] = "image/bmp"

        map[".c"] = "text/plain"
        map[".class"] = "application/octet-stream"
        map[".conf"] = "text/plain"

        map[".cpp"] = "text/plain"
        map[".doc"] = "application/msword"
        map[".exe"] = "application/octet-stream"

        map[".gif"] = "image/gif"
        map[".gtar"] = "application/x-gtar"
        map[".gz"] = "application/x-gzip"

        map[".h"] = "text/plain"
        map[".htm"] = "text/html"
        map[".html"] = "text/html"
        map[".jar"] = "application/java-archive"

        map[".java"] = "text/plain"
        map[".jpeg"] = "image/jpeg"
        map[".jpg"] = "image/jpeg"

        map[".js"] = "application/x-javascript"
        map[".log"] = "text/plain"
        map[".m3u"] = "audio/x-mpegurl"

        map[".m4a"] = "audio/mp4a-latm"
        map[".m4b"] = "audio/mp4a-latm"
        map[".m4p"] = "audio/mp4a-latm"

        map[".m4u"] = "video/vnd.mpegurl"
        map[".m4v"] = "video/x-m4v"
        map[".mov"] = "video/quicktime"

        map[".mp2"] = "audio/x-mpeg"
        map[".mp3"] = "audio/x-mpeg"
        map[".mp4"] = "video/mp4"

        map[".mpc"] = "application/vnd.mpohun.certificate"
        map[".mpe"] = "video/mpeg"
        map[".mpeg"] = "video/mpeg"

        map[".mpg"] = "video/mpeg"
        map[".mpg4"] = "video/mp4"
        map[".mpga"] = "audio/mpeg"

        map[".msg"] = "application/vnd.ms-outlook"
        map[".ogg"] = "audio/ogg"
        map[".pdf"] = "application/pdf"

        map[".png"] = "image/png"
        map[".pps"] = "application/vnd.ms-powerpoint"
        map[".ppt"] = "application/vnd.ms-powerpoint"

        map[".prop"] = "text/plain"
        map[".rar"] = "application/x-rar-compressed"
        map[".rc"] = "text/plain"

        map[".rmvb"] = "audio/x-pn-realaudio"
        map[".rtf"] = "application/rtf"
        map[".sh"] = "text/plain"

        map[".tar"] = "application/x-tar"
        map[".tgz"] = "application/x-compressed"
        map[".txt"] = "text/plain"

        map[".wav"] = "audio/x-wav"
        map[".wma"] = "audio/x-ms-wma"
        map[".wmv"] = "audio/x-ms-wmv"

        map[".wps"] = "application/vnd.ms-works"
        map[".xml"] = "text/xml"
        map[".xml"] = "text/plain"

        map[".z"] = "application/x-compress"
        map[".zip"] = "application/zip"
        map[""] = "*/*"
    }

    /**
     * 根据文件后缀名获得对应的MIME类型。
     */
    fun getMIMEType(path: String): String? {
        val file = File(path)
        return if (file.exists()) {
            getMIMEType(file)
        } else {
            "*/*"
        }
    }

    /**
     * 根据文件后缀名获得对应的MIME类型。
     */
    fun getMIMEType(file: File): String? {
        val type = "*/*"
        val fName = file.name
        // 获取后缀名前的分隔符"."在fName中的位置。
        val dotIndex = fName.lastIndexOf(".")
        if (dotIndex < 0) {
            return type
        }
        //获取文件的后缀名
        val end = fName.substring(dotIndex, fName.length).toLowerCase()
        return if (end == "") {
            type
        } else map[end]
    }

    fun isPicture(path: String): Boolean {
        val type = getMIMEType(path)
        return if (type!!.contains("image")) {
            true
        } else false
    }

    fun isAudioOrVideo(path: String): Boolean {
        val type = getMIMEType(path)
        return if (type!!.contains("audio") || type.contains("video")) {
            true
        } else false
    }

    fun openFile(filePath: String, context: Context) {
        var file: File? = null
        if (!TextUtils.isEmpty(filePath)) {
            file = File(filePath)
        }

        if (null != file && file.exists()) {
            val intent1 = Intent()
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            //设置intent的Action属性
            intent1.action = Intent.ACTION_VIEW
            //获取文件file的MIME类型
            val type = OpenNativeFileUtils.instance.getMIMEType(file)

            //判断是否是AndroidN以及更高的版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val contentUri = FileProvider
                        .getUriForFile(context.applicationContext, context.applicationContext.getPackageName() + ".fileopenprovider", file)
                intent1.setDataAndType(contentUri, type)
            } else {
                intent1.setDataAndType(Uri.fromFile(file), type)
            }
            //这里最好try一下，有可能会报错。 //比如说你的MIME类型是打开邮箱，但是你手机里面没装邮箱客户端，就会报错。
            try {
                context.startActivity(intent1)
            } catch (e: Exception) {
            }

        } else {
        }
    }

    companion object {
        var fileTypeUtil: OpenNativeFileUtils? = null

        val instance: OpenNativeFileUtils
            get() {
                if (fileTypeUtil == null) {
                    fileTypeUtil = OpenNativeFileUtils()
                }
                return fileTypeUtil as OpenNativeFileUtils
            }
    }
}