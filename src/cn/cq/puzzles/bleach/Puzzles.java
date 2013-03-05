package cn.cq.puzzles.bleach;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;

import com.pad.android.iappad.AdController;
import com.umeng.analytics.MobclickAgent;

public class Puzzles extends Activity {
	/** Called when the activity is first created. */

	private BlockView draw = null;
	private ArrayList<Integer> imgSourceIdlist = null;
	int tag = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 最上面的信号栏和Activity的Title栏隐藏掉即可，隐藏Title栏的代码
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 隐藏信号栏
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);
		draw = (BlockView) findViewById(R.id.draw);

		// 操作按钮，上一个，下一个
		final ImageButton lastButton = (ImageButton) findViewById(R.id.lastButton);
		final ImageButton nextButton = (ImageButton) findViewById(R.id.nextButton);

		// 动作
		lastButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showLastPic(draw);
			}
		});

		nextButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showNextPic(draw);
			}
		});

		// 初始化图片
		loadPicSource();

		// leadbolt banner
		AdController adCont = new AdController(this, "214458594");
		adCont.loadAd();
	}

	/**
	 * 装载图片资源到数组
	 */
	public void loadPicSource() {
		// 用反射装载图片数组
		Class cc = R.drawable.class;
		Field[] fields = cc.getFields();
		imgSourceIdlist = new ArrayList<Integer>();
		try {
			for (Field f : fields) {
				String s = f.getName();
				if (s.startsWith("puz")) {
					int i = f.getInt(cc);
					imgSourceIdlist.add(i);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d("dataerror", "load data error in ImageData.loadLocalImage");
		}
	}

	// 下一张
	public void showNextPic(BlockView v) {
		tag = ++tag % this.imgSourceIdlist.size();
		v.change(imgSourceIdlist.get(tag));
	}

	// 上一张
	public void showLastPic(BlockView v) {
		if (tag == 0) {
			tag = this.imgSourceIdlist.size();
		}
		tag = --tag % this.imgSourceIdlist.size();
		v.change(imgSourceIdlist.get(tag));
	}

	@Override
	protected void onPause() {// 其实这个方法在onCreate之前调用
		draw.setUpdateRunningAfterInited(false);
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		draw.setUpdateRunningAfterInited(true);
		MobclickAgent.onResume(this);
		super.onResume();
	}

	@Override
	// 后退键
	public void onBackPressed() {
		// 是否评分
		new AlertDialog.Builder(this)
				.setTitle("Exit")
				.setMessage("thanks! Rate this APP?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
								// 确定，去评分
								String pname = WelAct.class.getPackage()
										.getName();
								String url = "market://details?id=" + pname;
								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent.setData(Uri.parse(url));
								startActivity(intent);
							}
						})
				.setNegativeButton("Exit",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
								// 退出
								finish();
								Intent intent = new Intent(Intent.ACTION_MAIN);
								intent.addCategory(Intent.CATEGORY_HOME);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
							}
						}).show();

	}
}