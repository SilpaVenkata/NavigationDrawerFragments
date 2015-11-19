package com.hci.geotagger.dbhandler;

import android.os.Handler;
import android.os.Looper;

class LooperThread extends Thread {
	public Handler mHandler;

	public LooperThread(Handler handler) {
		mHandler = handler;
	}

	@Override
	public void run() {
		Looper.prepare();
		Looper.loop();
	}
}
