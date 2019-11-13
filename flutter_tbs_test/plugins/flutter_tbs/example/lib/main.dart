import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_tbs/flutter_tbs.dart';
import 'package:flutter_tbs/x5_webview.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  X5WebViewController _controller;
  String url;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    String platformVersion;
    try {
      platformVersion = await FlutterTbs.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(children: <Widget>[
          Expanded(
              child: defaultTargetPlatform == TargetPlatform.android
                  ? X5WebView(
                url: "https://image.baidu.com/",
                javaScriptEnabled: true,
                onWebViewCreated: (control) {
                  _controller = control;
                },
                onPageFinished: () async {
                  var isSuccess =
                  await _controller.isX5WebViewLoadSuccess();
                  print(isSuccess ? "x5内核加载成功" : "x5内核加载失败");
                  var url = await _controller.currentUrl();
                  print(url);
                  var listName = ["X5Web", "Toast"];
                  _controller.addJavascriptChannels(listName,
                          (name, data) {
                        switch (name) {
                          case "X5Web":
                            showDialog(
                                context: context,
                                builder: (context) {
                                  return AlertDialog(
                                    title: Text("获取到的字符串为："),
                                    content: Text(data),
                                  );
                                });
                            break;
                          case "Toast":
                            print(data);
                            break;
                        }
                      });
                },
                onProgressChanged: (progress) {
                  print("webview加载进度------$progress");
                },
              )
                  :
              //可替换为其他已实现ios webview,此处使用webview_flutter
              Container()
//          WebView(
//              initialUrl: url,
//              javascriptMode: JavascriptMode.unrestricted,
//              javascriptChannels: [JavascriptChannel(name: "X5Web", onMessageReceived: (msg){
//                print(msg);
//              })].toSet(),
//              onWebViewCreated: (control) {
////                _otherController = control;
////                var body = _otherController
////                    .evaluateJavascript('document.body.innerHTML');
////                print(body);
//              },
//            )
          ),
          RaisedButton(
            onPressed: () {
//              _controller.evaluateJavascript(
//                  'document.getElementById("input").value="flutter调用js成功！"');
            },
            child: Text("flutter调用js(更改文字)"),
          )
        ]),
      ),
    );
  }
}
