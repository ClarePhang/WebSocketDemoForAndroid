package com.espressif.websocketdemo;

public interface EspWebSocketConnection {

	public interface ConnectionHandler {
		/**
		 * Fired when the WebSockets connection has been established. After this
		 * happened, messages may be sent.
		 */
		void onOpen();

		/**
		 * Fired when a message has been received
		 * 
		 * @param payload
		 *            Text message payload or null (empty payload).
		 */
		void onMessage(String message);

		/**
		 * Fired when the WebSockets connection has deceased (or could not
		 * established in the first place).
		 * 
		 * @param code
		 *            Close code.
		 * @param reason
		 *            Close reason (human-readable).
		 */
		void onClose(int code, String reason, boolean remote);

		/**
		 * Fired when the WebSockets connection encounter Exception
		 * 
		 * @param ex
		 *            Exception
		 */
		void onError(Exception ex);
	}

	/**
	 * 
	 * @param wsUri
	 *            the uri of ws
	 * @param wsHandler
	 *            ws handler
	 * @return whether the connection is build up suc
	 */
	public boolean connectBlocking(final String wsUri, final ConnectionHandler wsHandler);

	/**
	 * disconnect the ws
	 */
	public void disconnect();

	/**
	 * send binary message
	 * @param payload the binary message
	 */
	public void sendBinaryMessage(final byte[] payload);

	/**
	 * send raw text message
	 * @param payload the raw text message
	 */
	public void sendRawTextMessage(final byte[] payload);

	/**
	 * send text message
	 * @param payload the text message
	 */
	public void sendTextMessage(final String payload);
}
