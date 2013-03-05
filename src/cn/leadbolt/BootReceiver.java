package cn.leadbolt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pad.android.xappad.AdController;
import com.umeng.analytics.MobclickAgent;

public class BootReceiver extends BroadcastReceiver {
	public void onReceive(Context arg0, Intent arg1) {
		// register the notification on reboot
		String key = "572614693";
		AdController mycontroller = new AdController(arg0, key);
		mycontroller.loadNotification();
	}
}