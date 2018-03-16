import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

void main() {
  runApp(new MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _textController = new TextEditingController();
  final _storage = new FlutterSecureStorage();
  final _key = "my_key1";

  Future read() async {
    String value = await _storage.read(_key);
    print("value = $value");
    _textController.text = value;
  }

  Future write() async {
    await _storage.write(_key, _textController.text);
  }

  Future delete() async {
    await _storage.delete(_key);
    _textController.text = "";
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: new Text('Plugin example app'),
        ),
        body: new Center(
          child: new Column(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              new Container(
                width: 100.0,
                child: new TextField(
                  controller: _textController,
                ),
              ),
              new Row(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  new RaisedButton(
                      onPressed: () => read(), child: new Text("Read")),
                  new RaisedButton(
                      onPressed: () => write(), child: new Text("Write")),
                  new RaisedButton(
                      onPressed: () => delete(), child: new Text("Delete")),
                ],
              ),
              new Row(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  new RaisedButton(
                      onPressed: () => _textController.text = "Value1",
                      child: new Text("Value1")),
                  new RaisedButton(
                      onPressed: () => _textController.text = "Value2",
                      child: new Text("Value2")),
                  new RaisedButton(
                      onPressed: () => _textController.text = "Value3",
                      child: new Text("Value3")),
                ],
              )
            ],
          ),
        ),
      ),
    );
  }
}
