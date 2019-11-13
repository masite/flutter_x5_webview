package com.wilimasy.flutter_tbs

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import com.tencent.smtt.sdk.DownloadListener
import com.tencent.smtt.sdk.*
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

class X5WebView(private val context: Context, val id: Int, val params: Map<String, Any>, val messenger: BinaryMessenger? = null) : PlatformView, MethodChannel.MethodCallHandler ,TbsReaderView.ReaderCallback{
    private val webView: WebView
    private val channel: MethodChannel = MethodChannel(messenger, "com.wiliamsy/x5WebView_$id")
    private val mViewParent: FrameLayout
    private var mTbsReaderView: TbsReaderView? = null

    init {
        channel.setMethodCallHandler(this)

        webView = WebView(context)
        mViewParent = FrameLayout(context)
        val lp :ViewGroup.LayoutParams
        lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
        mViewParent.addView(webView,lp)

        webView.setDownloadListener(object : DownloadListener {
            override fun onDownloadStart(p0: String?, p1: String?, p2: String?, p3: String?, p4: Long) {
                channel.invokeMethod("onNeedDownload", null)
            }
        })

        webView.apply {
            settings.javaScriptEnabled = params["javaScriptEnabled"] as Boolean
            settings.domStorageEnabled=true
            val webSetting = webView.getSettings()
            webSetting.setAllowFileAccess(true)
            webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS)
            webSetting.setSupportZoom(true)
            webSetting.setBuiltInZoomControls(true)
            webSetting.setUseWideViewPort(true)
            webSetting.setSupportMultipleWindows(false)
            webSetting.setAppCacheEnabled(true)
            webSetting.setDomStorageEnabled(true)
            webSetting.setJavaScriptEnabled(true)
            webSetting.setGeolocationEnabled(true)
            webSetting.setAppCacheMaxSize(java.lang.Long.MAX_VALUE)
            webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND)

            webView.setInitialScale(100)




            loadUrl(params["url"].toString())
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                    view.loadUrl(url)
                    return super.shouldOverrideUrlLoading(view, url)
                }

                override fun shouldOverrideUrlLoading(view: WebView, requset: WebResourceRequest?): Boolean {
                    view.loadUrl(requset?.url.toString())
                    return super.shouldOverrideUrlLoading(view, requset)
                }

                override fun onPageFinished(p0: WebView?, url: String) {
                    super.onPageFinished(p0, url)
                    //向flutter通信
                    val arg = hashMapOf<String, Any>()
                    arg["url"] = url
                    channel.invokeMethod("onPageFinished", arg)
                }

            }
            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View?, call: IX5WebChromeClient.CustomViewCallback?) {
                    super.onShowCustomView(view, call)
                    channel.invokeMethod("onShowCustomView", null)
                }

                override fun onHideCustomView() {
                    super.onHideCustomView()
                    channel.invokeMethod("onHideCustomView", null)
                }

                override fun onProgressChanged(p0: WebView?, p1: Int) {
                    super.onProgressChanged(p0, p1)
                    //加载进度
                    val arg = hashMapOf<String, Any>()
                    arg["progress"] = p1
                    channel.invokeMethod("onProgressChanged", arg)
                }
            }

//            val data= Bundle()
            //true表示标准全屏，false表示X5全屏；不设置默认false，
//            data.putBoolean("standardFullScreen",true)
            //false：关闭小窗；true：开启小窗；不设置默认true，
//            data.putBoolean("supportLiteWnd",false)
            //1：以页面内开始播放，2：以全屏开始播放；不设置默认：1
//            data.putInt("DefaultVideoScreen",2)
//            x5WebViewExtension.invokeMiscMethod("setVideoParams",data)
        }
    }

    /**
      * MethodCall              flutter > 原生  的操作，已经传值（。arguments）
     *
     * MethodChannel.Result     原生 > flutter  传值  (result.success), 注： 当返回值 是耗时操作时，需要给  result.success放在主线程 ：
     *
     new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
            }
     *
      */
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "loadUrl" -> {
                val arg = call.arguments as Map<String, Any>
                val url = arg["url"].toString()
                val headers = arg["headers"] as? Map<String, String>
                webView.loadUrl(url, headers)
                result.success(null)
            }
            "canGoBack" -> {
                result.success(webView.canGoBack())
            }
            "canGoForward" -> {
                result.success(webView.canGoForward())
            }
            "goBack" -> {
                webView.goBack()
                result.success(null)
            }
            "goForward" -> {
                webView.goForward()
                result.success(null)
            }

            "goBackOrForward" -> {
                val arg = call.arguments as Map<String, Any>
                val point = arg["i"] as Int
                webView.goBackOrForward(point)
                result.success(null)
            }

            "replaceFilePath" -> {
                val arg = call.arguments as Map<String, Any>
                val filePath = arg["filePath"] as String

                if(filePath !=null && filePath.isNotEmpty()){
                    var b : Boolean
                    b = openLocalFile(filePath)
                    result.success(b.toString())
                }else{
                    mViewParent.removeAllViews()
                    val tv :TextView
                    tv = TextView(context)
                    tv.setText("不支持的文件格式")
                    tv.setTextSize(26f)
                    mViewParent.addView(tv)
                    result.success(filePath)
                }
            }



            "reload" -> {
                webView.reload()
                result.success(null)
            }
            "currentUrl" -> {
                result.success(webView.url)
            }
            "evaluateJavascript" -> {
                val arg = call.arguments as Map<String, Any>
                val js = arg["js"].toString()
                webView.evaluateJavascript(js) { value -> result.success(value) }
            }

            "addJavascriptChannels"->{
                val arg = call.arguments as Map<String, Any>
                val names = arg["names"] as List<String>
                for(name in names){
                    webView.addJavascriptInterface(JavascriptChannel(name,channel,context),name)
                }
                webView.reload()
                result.success(null)

            }
            "isX5WebViewLoadSuccess"->{
               val exception= webView.x5WebViewExtension
                if(exception==null){
                    result.success(false)
                }else{
                    result.success(true)
                }
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    private fun openLocalFile(path: String) : Boolean {
        mTbsReaderView = TbsReaderView(context, this)
        mViewParent.removeAllViews()
        mViewParent.addView(mTbsReaderView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT))

        val bundle = Bundle()
        bundle.putString("filePath", path)
        bundle.putString("tempPath", Environment.getExternalStorageDirectory().path)
        val result = mTbsReaderView!!.preOpen(parseFormat(path), false)
        if (result) {
            mTbsReaderView!!.openFile(bundle)
        }
        return result;
    }

    override fun onCallBackAction(integer: Int?, o: Any, o1: Any) {

    }
    private fun parseFormat(fileName: String): String {
        return fileName.substring(fileName.lastIndexOf(".") + 1)
    }

    override fun getView(): View {
        return mViewParent
    }

    override fun dispose() {
        channel.setMethodCallHandler(null)

        if (mTbsReaderView != null) {
            mTbsReaderView!!.onStop()
        }
        if (webView != null){
            webView.destroy()
        }
    }
}