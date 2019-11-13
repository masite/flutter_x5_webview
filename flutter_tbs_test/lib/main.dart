import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

import 'package:flutter_tbs/x5_webview.dart';

import 'package:flutter_tbs/flutter_tbs.dart';

void main() {
  FlutterTbs.init().then((isOK) {
    print(isOK ? "X5内核成功加载" : "X5内核加载失败");
  });
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);
  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  X5WebViewController _controller;
  int progress = 0;


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: buildX5webView(),
    );
  }

  Widget buildX5webView() {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return X5WebView(
        url: "https://fs.neets.cc/6294341660442624/1572417205752.xls?e=t&t=13144975869280256",
        javaScriptEnabled: true,
        onWebViewCreated: (control) {
          _controller = control;
        },
        onNeedDownload: _needDownload,
        onProgressChanged: (progress) {
          print("webview加载进度------$progress");
          this.progress = progress;
          setState(() {});
        },
        onPageFinished: _onPageFinish,
      );
    } else {
      return Container();
    }
  }

  ///tbs - web view支持在线打开的，会走 page finish.
  Future _onPageFinish() async {
    setState(() {
      print("--_onPageFinish-----加载完成");
    });
  }

  Future _needDownload() async {
    _controller.replaceFilePath("/storage/emulated/0/7779343921381376 (3).xls").then((value) {
      print("--test native path--   $value");
      if (value != "true") {
      }
      setState(() {
      });
    });
  }


}
