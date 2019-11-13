package com.wilimasy.flutter_tbs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.TbsVideo
import com.tencent.smtt.sdk.WebView
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import java.io.File

class FlutterTbsPlugin (var context: Context, var activity: Activity) : MethodChannel.MethodCallHandler {
    companion object {
        @JvmStatic
        fun registerWith(registrar: PluginRegistry.Registrar) {
            //channel.invoke 用于  原生 > flutter的 通信
            val channel = MethodChannel(registrar.messenger(), "com.wiliamsy/x5WebView")
            channel.setMethodCallHandler(FlutterTbsPlugin(registrar.context(), registrar.activity()))

            //注册自定义 flutter widget
            registrar.platformViewRegistry().registerViewFactory("com.wiliamsy/x5WebView", X5WebViewFactory(registrar.messenger(), registrar.activeContext()))

        }
    }

    /**
      * flutter >  原生  的通信
      */
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "init" -> {
                QbSdk.initX5Environment(context.applicationContext, object : QbSdk.PreInitCallback {
                    override fun onCoreInitFinished() {

                    }

                    override fun onViewInitFinished(p0: Boolean) {
                        //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                        Log.d("app_tbs","x5初始化$p0")
                        result.success(p0)
                    }

                })
            }
            "canUseTbsPlayer" -> {
                //返回是否可以使用tbsPlayer
                result.success(TbsVideo.canUseTbsPlayer(context))
            }
            "openVideo" -> {
                val url = call.argument<String>("url")
                val screenMode = call.argument<Int>("screenMode") ?: 103
                val bundle = Bundle()
                bundle.putInt("screenMode", screenMode)
                TbsVideo.openVideo(context, url, bundle)
                result.success(null)
            }
            "openFile" -> {
                val filePath = call.argument<String>("filePath")
                val params = hashMapOf<String, String>()
                params["local"] = call.argument<String>("local") ?: "false"
                params["style"] = call.argument<String>("style") ?: "0"
                params["topBarBgColor"] = call.argument<String>("topBarBgColor") ?: "#2CFC47"
                var menuData = call.argument<String>("menuData")
                if (menuData != null) {
                    params["menuData"] = menuData
                }
                if (!File(filePath).exists()) {
                    Toast.makeText(context, "文件不存在,请确认$filePath 是否正确", Toast.LENGTH_LONG).show()
                    result.success("文件不存在,请确认$filePath 是否正确")
                    return
                }
                QbSdk.canOpenFile(activity, filePath) { canOpenFile ->
                    if (canOpenFile) {
                        QbSdk.openFileReader(activity, filePath, params) { msg ->
                            result.success(msg)
                        }
                    } else {
                        Toast.makeText(context, "X5Sdk无法打开此文件", Toast.LENGTH_LONG).show()
                        result.success("X5Sdk无法打开此文件")
                    }
                }


//                val screenMode = call.argument<Int>("screenMode") ?: 103
//                val bundle = Bundle()
//                bundle.putInt("screenMode", screenMode)
//                TbsVideo.openVideo(context, url, bundle)
//                result.success(null)
            }

            "openWebActivity" -> {
                val url = call.argument<String>("url")
                val title = call.argument<String>("title")
                val intent = Intent(activity, X5WebViewActivity::class.java)
                intent.putExtra("url", url)
                intent.putExtra("title", title)
                activity.startActivity(intent)
                result.success(null)
            }
            "getCarshInfo" -> {
                val info = WebView.getCrashExtraMessage(context)
                result.success(info)
            }
            "setDownloadWithoutWifi" -> {
                val isWithoutWifi = call.argument<Boolean>("isWithoutWifi")
                QbSdk.setDownloadWithoutWifi(isWithoutWifi ?: false)
                result.success(null)
            }

            "openWithThird" -> {
                val filePath = call.argument<String>("filePath")
                if (filePath != null) {
                    var open : OpenNativeFileUtils = OpenNativeFileUtils()
                    open.openFile(filePath,context)
                }
                result.success(true)
            }

            else -> {
                result.notImplemented()
            }
        }
    }
}