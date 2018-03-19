import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _textController = new TextEditingController();
  final _storage = new FlutterSecureStorage();
  final _key = "my_key1";

  Future read() async {
    try {
      String value = await _storage.read(_key);
      print("value = $value");
      _textController.text = value;
    } catch (e) {
      print('Caught exception:');
      print(e);
    }
  }

  Future write() async {
    try {
      await _storage.write(_key, _textController.text);
    } catch (e) {
      print('Caught exception:');
      print(e);
    }
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
          title: const Text('Plugin example app'),
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
                      onPressed: () => read(),
                      child: const Text("Read")),
                  new RaisedButton(
                      onPressed: () => write(),
                      child: const Text("Write")),
                  new RaisedButton(
                      onPressed: () => delete(),
                      child: const Text("Delete")),
                ],
              ),
              new Row(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  new RaisedButton(
                      onPressed: () => _textController.text = "Value1",
                      child: const Text("Value1")),
                  new RaisedButton(
                      onPressed: () => _textController.text = "Value2",
                      child: const Text("Value2")),
                  new RaisedButton(
                      onPressed: () => _textController.text = "Value3",
                      child: const Text("Value3")),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
