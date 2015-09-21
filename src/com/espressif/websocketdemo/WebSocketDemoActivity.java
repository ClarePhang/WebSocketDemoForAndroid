package com.espressif.websocketdemo;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.websocketdemo.EspWebSocketConnection.ConnectionHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

class WebSocketState {

	private static final int FLAG_DISCONNECTED = 1;
	private static final int FLAG_CONNECTED = 1 << 1;
	private static final int FLAG_SUBSCRIBE = 1 << 2;

	private int mState = 0;

	public WebSocketState() {
		setDisconnected();
	}

	boolean isDisconnected() {
		return (mState & FLAG_DISCONNECTED) != 0;
	}

	boolean isConnected() {
		return (mState & FLAG_CONNECTED) != 0;
	}

	boolean isSubscribe() {
		return (mState & FLAG_SUBSCRIBE) != 0;
	}

	void setDisconnected() {
		mState = FLAG_DISCONNECTED;
	}

	void setConnected() {
		mState = FLAG_CONNECTED;
	}

	void setSubscribe() {
		mState = FLAG_SUBSCRIBE;
	}

}

public class WebSocketDemoActivity extends Activity implements OnClickListener {

	private static final String TAG = "WebSocketDemoActivity";
	private static final String MASTER_DEVICE_KEY = "e61f7534b1d0e2642bee485a6b51fd105fd1a20c";
	private TextView mTvConnectState;
	private TextView mTvSubscribeState;
	private TextView mTvSendStatus;
	private Button mBtnConnectDisconnect;
	private Button mBtnSubscribe;
	private Button mBtnSendRandomMessage;
	private boolean mIsConnectBtn;
	private WebSocketState mWebSocketState;
	private final EspWebSocketConnection mWebSocketConnection = new EspWebSocketClientImpl();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mTvConnectState = (TextView) findViewById(R.id.tvConnectState);
		mTvSubscribeState = (TextView) findViewById(R.id.tvSubscribeState);
		mTvSendStatus = (TextView) findViewById(R.id.tvSendStatus);
		mBtnConnectDisconnect = (Button) findViewById(R.id.btnConnectDisconnect);
		mBtnConnectDisconnect.setOnClickListener(this);
		mBtnSubscribe = (Button) findViewById(R.id.btnSubscribe);
		mBtnSubscribe.setOnClickListener(this);
		mBtnSendRandomMessage = (Button) findViewById(R.id.btnSendRandomMessage);
		mBtnSendRandomMessage.setOnClickListener(this);

		mWebSocketState = new WebSocketState();
		disableSubscribeButton();
		enableConnectButton();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWebSocketConnection.disconnect();
	}
	
	/**
	 * show alert message in top
	 * 
	 * @param message
	 *            the message to be shown
	 */
	private void alertTop(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast toast = Toast.makeText(getApplicationContext(), message,
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();
			}
		});
	}
	
	private void alertCenter(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast toast = Toast.makeText(getApplicationContext(), message,
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();
			}
		});
	}
	
	private void updateSendStatus(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String statusHeader = getString(R.string.textview_send_status);
				mTvSendStatus.setText(statusHeader + message);
			}
		});
	}
	
	/**
	 * update UI about the status
	 */
	private void updateUI() {
		String tvConnectTitle = getString(R.string.state_connect);
		String tvDisconnectTitle = getString(R.string.state_disconenct);
		String tvSubscribeTitle = getString(R.string.state_subscribe);
		String tvUnsubscribeTitle = getString(R.string.state_unsubscribe);
		if (mWebSocketState.isConnected()) {
			mTvSubscribeState.setText(tvUnsubscribeTitle);
			mTvConnectState.setText(tvConnectTitle);
		} else if (mWebSocketState.isDisconnected()) {
			mTvSubscribeState.setText(tvUnsubscribeTitle);
			mTvConnectState.setText(tvDisconnectTitle);
		} else if (mWebSocketState.isSubscribe()) {
			mTvSubscribeState.setText(tvSubscribeTitle);
		}
	}

	/**
	 * enable subscribe button could be tapped
	 */
	private void enableSubscribeButton() {
		if (mWebSocketState.isDisconnected()) {
			throw new IllegalStateException(
					"before subscribing, connect should be built up");
		}
		mBtnSubscribe.setEnabled(true);
		updateUI();
	}

	/**
	 * disable subscribe button could be tapped
	 */
	private void disableSubscribeButton() {
		mBtnSubscribe.setEnabled(false);
		updateUI();
	}

	/**
	 * enable connect button could be tapped
	 */
	private void enableConnectButton() {
		mIsConnectBtn = true;
		String btnTitle = getString(R.string.button_connect_title);
		mBtnConnectDisconnect.setText(btnTitle);
		updateUI();
	}

	/**
	 * enable disconnect button could be tapped
	 */
	private void enableDisconnectButton() {
		mIsConnectBtn = false;
		String btnTitle = getString(R.string.button_disconnect_title);
		mBtnConnectDisconnect.setText(btnTitle);
		updateUI();
	}

	/**
	 * enable send random message could be tapped
	 */
	private void enableSendRandomMessage() {
		if (!mWebSocketState.isSubscribe()) {
			throw new IllegalStateException(
					"before sending message, subscribe should be completed");
		}
		mBtnSendRandomMessage.setEnabled(true);
	}

	private class ConnectTask extends AsyncTask<Void, Void, Boolean>
	{

		private ProgressDialog mProgressDialog;
		
		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(WebSocketDemoActivity.this);
			mProgressDialog
					.setMessage("Web Socket is connecting, please wait for a moment...");
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			return connect();
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			mProgressDialog.dismiss();
			if(result)
			{
				alertCenter("Web socket connect suc");
			}
			else
			{
				alertCenter("Web socket connect fail");
			}
		}
	}
	
	private boolean connect() {
		Log.d(TAG, "connect()");
		// ws://iot.espressif.cn:9000 is like http,
		// wss://iot.espressif.cn:9443 is like https
		final String wsuri = "wss://iot.espressif.cn:9443/";

		boolean isConnectSuc = mWebSocketConnection.connectBlocking(wsuri,
				new ConnectionHandler() {

					@Override
					public void onOpen() {
						Log.d(TAG, "ConnectionHandler onOpen");
						connectSuc();
					}

					@Override
					public void onMessage(String message) {
						Log.d(TAG, "ConnectionHandler onMessage: " + message);
						try {
							JSONObject jsonResult = new JSONObject(message);
							// for subscribe response will contain status
							boolean isSubScribe = !jsonResult.isNull("status");
							if (isSubScribe) {
								int status = jsonResult.getInt("status");
								if (status == HttpStatus.SC_OK) {
									if (mWebSocketState.isConnected()) {
										Log.i(TAG,
												"ConnectionHandler onMessage subscribe suc");
										subscribeSuc();
									}
								}
							} else {
								alertTop(message);
							}
						} catch (JSONException e) {
							Log.w(TAG,
									"ConnectionHandler onMessage JSONException: "
											+ e.toString());
							disconnect();
						}
					}

					@Override
					public void onError(Exception ex) {
						Log.w(TAG, "ConnectionHandler onError:" + ex.toString());
						disconnect();
					}

					@Override
					public void onClose(int code, String reason, boolean remote) {
						Log.d(TAG, "ConnectionHandler onClose, code:" + code
								+ ", reason:" + ", remote:" + remote);
						disconnect();
					}
				});
		return isConnectSuc;
	}

	private void connectSuc() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "connectSuc");
				mWebSocketState.setConnected();
				enableDisconnectButton();
				enableSubscribeButton();
			}
		});
	}
	
	private void disconnect() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "disconnect()");
				mWebSocketConnection.disconnect();
				mWebSocketState.setDisconnected();
				enableConnectButton();
				disableSubscribeButton();
			}
		});
	}

	private void subscribe() {
		Log.d(TAG, "subscribe()");
		String request = "{\"path\": \"/v1/mbox/\", \"method\": \"POST\", \"body\": {\"action\": \"subscribe\", \"type\": \"datastream\", \"stream\": \"light\"}, \"meta\": {\"Authorization\": \"token "
				+ MASTER_DEVICE_KEY + "\"}}";
		System.out.println("subscribe():" + request);
		mWebSocketConnection.sendTextMessage(request);
	}
	
	private void subscribeSuc() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "subscribeSuc()");
				mWebSocketState.setSubscribe();
				disableSubscribeButton();
				enableSendRandomMessage();
			}
		});
	}

	private void sendRandomMessage() {
		Log.d(TAG, "sendRandomMessage()");
		new Thread() {
			public void run() {
				updateSendStatus("send Random Message...");
				EspWebSocketMessageSender sender = new EspWebSocketMessageSender();
				boolean isSendSuc = sender.sendRandomMessage();
				if(isSendSuc)
				{
					updateSendStatus("send Random Message suc");
				}
				else
				{
					updateSendStatus("send Random Message fail");
				}
			}
		}.start();
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnConnectDisconnect) {
			if (mIsConnectBtn) {
				new ConnectTask().execute();
			} else {
				disconnect();
			}
		} else if (v == mBtnSendRandomMessage) {
			sendRandomMessage();
		} else if (v == mBtnSubscribe) {
			subscribe();
		}
	}
	
}
