import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

typedef void X5WebViewCreatedCallback(X5WebViewController controller);
typedef void PageFinishedCallback();
typedef void ShowCustomViewCallback();
typedef void HideCustomViewCallback();
typedef void OnNeedDownload();
typedef void ProgressChangedCallback(int progress);
typedef void MessageReceived(String name, String data);

/// onPageFinished 网页加载完成时回调， onNeedDownload x5web不支持的类型，需要下载后加载 ， onProgressChanged当前加载的进度

class X5WebView extends StatefulWidget {
  final url;
  final X5WebViewCreatedCallback onWebViewCreated;
  final PageFinishedCallback onPageFinished;
  final ShowCustomViewCallback onShowCustomView;
  final HideCustomViewCallback onHideCustomView;
  final OnNeedDownload onNeedDownload;
  final ProgressChangedCallback onProgressChanged;
  final bool javaScriptEnabled;

  const X5WebView(
      {Key key,
      this.url,
      this.javaScriptEnabled = false,
      this.onWebViewCreated,
      this.onPageFinished,
      this.onShowCustomView,
      this.onNeedDownload,
      this.onHideCustomView,
      this.onProgressChanged})
      : super(key: key);

  @override
  _X5WebViewState createState() => _X5WebViewState();
}

class _X5WebViewState extends State<X5WebView> {
  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
        viewType: 'com.wiliamsy/x5WebView',
        onPlatformViewCreated: _onPlatformViewCreated,
        creationParamsCodec: const StandardMessageCodec(),
        creationParams: _CreationParams.fromWidget(widget).toMap(),
        //应该是这个地方， 传参的
        layoutDirection: TextDirection.rtl,
      );
    } else if (defaultTargetPlatform == TargetPlatform.iOS) {
      //TODO 添加ios WebView
      return Container();
    } else {
      return Container();
    }
  }

  void _onPlatformViewCreated(int id) {
    if (widget.onWebViewCreated == null) {
      return;
    }
    final X5WebViewController controller = X5WebViewController._(id, widget);
    widget.onWebViewCreated(controller);
  }
}

class X5WebViewController {
  X5WebView _widget;

  X5WebViewController._(
    int id,
    this._widget,
  ) : _channel = MethodChannel('com.wiliamsy/x5WebView_$id') {
    _channel.setMethodCallHandler(_onMethodCall);
  }

  final MethodChannel _channel;

  Future<void> loadUrl(String url, {Map<String, String> headers}) async {
    assert(url != null);
    return _channel.invokeMethod('loadUrl', {
      'url': url,
      'headers': headers,
    });
  }

  Future<bool> isX5WebViewLoadSuccess() async {
    return _channel.invokeMethod('isX5WebViewLoadSuccess');
  }

  Future<String> evaluateJavascript(String js) async {
    assert(js != null);
    return _channel.invokeMethod('evaluateJavascript', {
      'js': js,
    });
  }

  Future<void> addJavascriptChannels(
      List<String> names, MessageReceived callback) async {
    assert(names != null);
    await _channel.invokeMethod("addJavascriptChannels", {'names': names});
    _channel.setMethodCallHandler((call) {
      if (call.method == "onJavascriptChannelCallBack") {
        Map arg = call.arguments;
        callback(arg["name"], arg["msg"]);
      }
      return;
    });
  }

  Future<void> goBackOrForward(int i) async {
    assert(i != null);
    return _channel.invokeMethod('goBackOrForward', {
      'i': i,
    });
  }

  Future<String> replaceFilePath(String path) async {
    assert(path != null);
    return _channel.invokeMethod('replaceFilePath', {
      'filePath': path,
    });
  }

  Future<bool> canGoBack() async {
    return _channel.invokeMethod('canGoBack');
  }

  Future<bool> canGoForward() async {
    return _channel.invokeMethod('canGoForward');
  }

  Future<void> goBack() async {
    return _channel.invokeMethod('goBack');
  }

  Future<void> goForward() async {
    return _channel.invokeMethod('goForward');
  }

  Future<void> openWithThird() async {
    return _channel.invokeMethod('openWithThird');
  }


  Future<void> reload() async {
    return _channel.invokeMethod('reload');
  }

  Future<String> currentUrl() async {
    return _channel.invokeMethod('currentUrl');
  }

  /// 原生 > flutter
  Future _onMethodCall(MethodCall call) async {
    switch (call.method) {
      case "onPageFinished":
        if (_widget.onPageFinished != null) {
          _widget.onPageFinished();
        }
        break;
      case "onNeedDownload":
        if(_widget.onNeedDownload !=null){
          _widget.onNeedDownload();
        }
        break;
      case "onShowCustomView":
        if (_widget.onShowCustomView != null) {
          _widget.onShowCustomView();
        }
        break;
      case "onHideCustomView":
        if (_widget.onHideCustomView != null) {
          _widget.onHideCustomView();
        }
        break;
      case "onProgressChanged":
        if (_widget.onProgressChanged != null) {
          Map arg = call.arguments;
          _widget.onProgressChanged(arg["progress"]);
        }
        break;
      default:
        throw MissingPluginException(
            '${call.method} was invoked but has no handler');
        break;
    }
  }
}

class _CreationParams {
  _CreationParams({this.url, this.javaScriptEnabled, this.jsChannelName});

  static _CreationParams fromWidget(X5WebView widget) {
    return _CreationParams(
        url: widget.url, javaScriptEnabled: widget.javaScriptEnabled);
  }

  final String url;
  final bool javaScriptEnabled;
  final String jsChannelName;

  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      'url': url,
      'javaScriptEnabled': javaScriptEnabled,
      "jsChannelName": jsChannelName
    };
  }
}
