package com.espressif.websocketdemo;

import java.net.URI;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class EspWebSocketClientImpl implements EspWebSocketConnection {

	private volatile WebSocketClient mWebSocketClient;

	@Override
	public synchronized boolean connectBlocking(String wsUri,
			final ConnectionHandler wsHandler) {
		// disconnect current connection
		disconnect();

		URI uri = URI.create(wsUri);

		String scheme = uri.getScheme();
		if (!scheme.equals("ws") && !scheme.equals("wss")) {
			throw new IllegalArgumentException(
					"unsupported scheme for WebSockets URI");
		}

		mWebSocketClient = new WebSocketClient(uri) {

			@Override
			public void onOpen(ServerHandshake arg0) {
				if (wsHandler != null) {
					wsHandler.onOpen();
				}
			}

			@Override
			public void onMessage(String arg0) {
				if (wsHandler != null) {
					wsHandler.onMessage(arg0);
				}
			}

			@Override
			public void onError(Exception arg0) {
				if (wsHandler != null) {
					wsHandler.onError(arg0);
				}
			}

			@Override
			public void onClose(int arg0, String arg1, boolean arg2) {
				if (wsHandler != null) {
					wsHandler.onClose(arg0, arg1, arg2);
				}
			}
		};

		if (uri.getPort() == 9000) {
			if (!scheme.equals("ws")) {
				throw new IllegalArgumentException("port 9000 only support ws");
			}
		} else if (uri.getPort() == 9443) {
			if (!scheme.equals("wss")) {
				throw new IllegalArgumentException("port 9443 only support wss");
			}
			// trust all hosts for server have some issues at present
			trustAllHosts(mWebSocketClient);
		} else {
			throw new IllegalArgumentException("port is illegal");
		}

		try {
			return mWebSocketClient.connectBlocking();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public synchronized void disconnect() {
		if (mWebSocketClient != null) {
			// close the web socket client immediately
			mWebSocketClient.close();
			mWebSocketClient = null;
		}
	}

	@Override
	public synchronized void sendBinaryMessage(byte[] payload) {
		mWebSocketClient.send(payload);
	}

	@Override
	public synchronized void sendRawTextMessage(byte[] payload) {
		mWebSocketClient.send(payload);
	}

	@Override
	public synchronized void sendTextMessage(String payload) {
		mWebSocketClient.send(payload);
	}

	// always verify the host - dont check for certificate
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	/**
	 * Trust every server - dont check for any certificate
	 */
	private void trustAllHosts(WebSocketClient appClient) {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			SSLSocketFactory factory = sc.getSocketFactory();
			appClient.setSocket(factory.createSocket());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
