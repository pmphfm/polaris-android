import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:get_it/get_it.dart';
import 'package:polaris/api/api.dart';
import 'package:shared_preferences/shared_preferences.dart';

final getIt = GetIt.instance;

const String serverURLKey = "polaris_server_url";

enum ConnectionStoreError {
  connectionAlreadyInProgress,
  unsupportedAPIVersion,
  networkError,
  requestFailed,
  unknownError,
}

enum ConnectionState {
  disconnected,
  connecting,
  reconnecting,
  connected,
}

class ConnectionStore extends ChangeNotifier {
  ConnectionState _state = ConnectionState.disconnected;

  get state => _state;

  Future reconnect() async {
    if (_state != ConnectionState.disconnected) {
      throw ConnectionStoreError.connectionAlreadyInProgress;
    }
    _setState(ConnectionState.reconnecting);

    SharedPreferences prefs = await SharedPreferences.getInstance();
    String host = prefs.getString(serverURLKey);

    if (host.isEmpty) {
      _setState(ConnectionState.disconnected);
      return;
    }

    try {
      // TODO find a way for UI to react to errors coming out of this
      await _tryConnect(host);
    } catch (e) {}
  }

  Future connect(String host) async {
    if (_state != ConnectionState.disconnected) {
      throw ConnectionStoreError.connectionAlreadyInProgress;
    }
    _setState(ConnectionState.connecting);
    return _tryConnect(host);
  }

  disconnect() {
    var api = getIt<API>();
    api.host = null;
    _setState(ConnectionState.disconnected);
  }

  Future _tryConnect(String host) async {
    assert(state == ConnectionState.connecting ||
        state == ConnectionState.reconnecting);
    var api = getIt<API>();
    api.host = host;

    try {
      var apiVersion = await api.getAPIVersion();
      if (apiVersion.major != 6) {
        throw ConnectionStoreError.unsupportedAPIVersion;
      }
    } on APIError catch (e) {
      _setState(ConnectionState.disconnected);
      switch (e) {
        case APIError.requestFailed:
          throw ConnectionStoreError.requestFailed;
          break;
        case APIError.networkError:
        case APIError.unspecifiedHost:
          throw ConnectionStoreError.networkError;
          break;
      }
    } catch (e) {
      _setState(ConnectionState.disconnected);
      throw ConnectionStoreError.unknownError;
    }

    SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setString(serverURLKey, host);
    _setState(ConnectionState.connected);
  }

  _setState(ConnectionState newState) {
    if (_state == newState) {
      return;
    }
    _state = newState;
    notifyListeners();
  }
}
