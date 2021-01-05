import 'dart:convert';
import 'dart:developer' as developer;
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:polaris/service/collection.dart';
import 'package:polaris/shared/dto.dart';
import 'package:polaris/shared/http_collection_api.dart';

class ProxyServer {
  static final String portParam = 'port';
  static final String audioEndpoint = '/audio';
  static final String pathQueryParameter = 'path';
  final Collection collection;

  ProxyServer({@required this.collection}) : assert(collection != null);

  HttpServer _server;

  static Future<ProxyServer> create(Collection collection) async {
    final mediaProxy = new ProxyServer(collection: collection);
    await mediaProxy.start();
    return mediaProxy;
  }

  Future start() async {
    _server = await HttpServer.bind(InternetAddress.loopbackIPv4, 0);
    run();
  }

  Future run() async {
    await for (HttpRequest request in _server) {
      try {
        if (request.uri.path.startsWith(browseEndpoint)) {
          final String path = Uri.decodeComponent(request.uri.path.substring(browseEndpoint.length));
          final List<CollectionFile> results = await collection.browse(path); // TODO error handling
          final encoded = utf8.encode(jsonEncode(results));
          request.response
            ..contentLength = encoded.length
            ..statusCode = HttpStatus.ok
            ..add(encoded)
            ..close();
        } else if (request.uri.path.startsWith(randomEndpoint)) {
          final List<Directory> results = await collection.random(); // TODO error handling
          final encoded = utf8.encode(jsonEncode(results));
          request.response
            ..contentLength = encoded.length
            ..statusCode = HttpStatus.ok
            ..add(encoded)
            ..close();
        } else if (request.uri.path.startsWith(recentEndpoint)) {
          final List<Directory> results = await collection.recent(); // TODO error handling
          final encoded = utf8.encode(jsonEncode(results));
          request.response
            ..contentLength = encoded.length
            ..statusCode = HttpStatus.ok
            ..add(encoded)
            ..close();
        } else if (request.uri.path.startsWith(thumbnailEndpoint)) {
          final String path = Uri.decodeComponent(request.uri.path.substring(thumbnailEndpoint.length));
          final data = await collection.getImage(path); // TODO error handling
          request.response
            ..contentLength = -1
            ..statusCode = HttpStatus.ok;
          // TODO content-type header?
          data.pipe(request.response);
        } else if (request.uri.path.startsWith(audioEndpoint)) {
          final String path = request.uri.queryParameters[pathQueryParameter];
          final data = await collection.getAudio(path); // TODO error handling
          request.response
            ..contentLength = -1
            ..statusCode = HttpStatus.ok;
          // TODO content-type header?
          data.pipe(request.response);
        } else {
          request.response
            ..contentLength = 0
            ..statusCode = HttpStatus.notFound
            ..close();
        }
      } catch (e) {
        developer.log('Unhandled server error: $e');
        request.response
          ..statusCode = HttpStatus.internalServerError
          ..close();
      }
    }
  }

  int get port => _server?.port;

  Future stop() => _server.close();
}
