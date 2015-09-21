package com.espressif.websocketdemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

public class EspWebSocketMessageSender {

	private static final String MASTER_DEVICE_KEY = "e61f7534b1d0e2642bee485a6b51fd105fd1a20c";
	
	private static final Random random = new Random();
	
	/**
	 * send random message to iot.espressif.cn for WebSocket listening
	 * (it just a random message, don't entangle in the format)
	 */
	public boolean sendRandomMessage() {
		Socket socket = new Socket();
		SocketAddress remoteAddr = new InetSocketAddress("iot.espressif.cn",
				8000);
		boolean isConnect = false;
		try {
			socket.connect(remoteAddr);
			isConnect = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		boolean isSendSuc = false;
		if (isConnect) {
			
			JSONObject json = new JSONObject();
			int xInt = random.nextInt(256);
			int yInt = random.nextInt(256);
			int zInt = random.nextInt(256);
			try {
				json.put("path", "_path");
				json.put("method", "POST");
				JSONObject jsonBody = new JSONObject();
				JSONObject jsonDatapoint = new JSONObject();
				jsonDatapoint.put("at", System.currentTimeMillis());
				jsonDatapoint.put("x", xInt);
				jsonDatapoint.put("y", yInt);
				jsonDatapoint.put("z", zInt);
				jsonBody.put("datapoint", jsonDatapoint);
				JSONObject jsonAuth = new JSONObject();
				jsonAuth.put("Authorization", "token " + MASTER_DEVICE_KEY);
				json.put("meta", jsonAuth);
				json.put("body", jsonBody);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			try {
				String request = json.toString().replace("_path",
						"/v1/datastreams/light/datapoint/");
				request += "\n";
				System.out.println("request:" + request);
				socket.getOutputStream().write(request.getBytes());
				isSendSuc = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("isSendSuc:" + isSendSuc);
		return isSendSuc;
	}
}
