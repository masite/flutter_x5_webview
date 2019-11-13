# flutter_x5_webview
提供tbs预览， 全内嵌 实现预览。

感谢 https://github.com/VenusCao/x5_webview_flutter 提供的思路，本插件只是对这个项目的 修改。增加以下功能：

1. 提供是否需要下载的方法（比如xls pdf网页，x5不支持在线打开，需要我们下载到本地，通过TbsReaderView 来加载本地文件）
2. 加载本地文件的视图 也是内嵌到flutter中的。总体思路仅仅是把webview，其他视图加入到FrameLayout 容器中，getView 返回容器。通过_controller 来显示不同的view而已。
3.第三方打开；加载本地文件会提供一个返回值，用来判断，如果无法加载，则通过第三方打开。

### 关键方法


#### 基础使用
```
X5WebView(
        url: widget.url,
        javaScriptEnabled: true,
        onWebViewCreated: (control) {
          _controller = control; 
        },
        onNeedDownload: _needDownload, //是否需要下载。x5无法在线打开的，都会走这个回调
        onProgressChanged: (progress) {
          print("webview加载进度------$progress");
          this.progress = progress;
          setState(() {});
        },
        onPageFinished: _onPageFinish, //加载完成时
      );
```

#### 下载完成后，打开
```
_controller.replaceFilePath(filePath).then((value) {
          print("--test native path--   $value"); //value ： true/false （字符串0.0）
          if (value != "true") { //本地文件无法打开，则需要通过第三方打开，
            isCanOpen = false;
            openWithThirdFilePath = appDocPath;
          }
          setState(() {
            isPageFinished = true;
            isLoading = false;
          });
        });
```

#### 第三方打开文件
```
FlutterTbs.openFileWithThird(openWithThirdFilePath);
```
