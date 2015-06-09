package cn.gfreader.txt;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.LinearLayout;
import cn.gfreader.main.MainActivity;
import cn.gfreader.main.R;
import cn.gfreader.util.Messages;

public class ReadActivity extends Activity {

	private boolean isReaderViewInit = false;
	private File txtFile;
	private ReadView readView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.txt_read);

		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {
			String txtFilePath = bundle.getString("txt_file_path");
			txtFile = new File(txtFilePath);
		}
		if (txtFile.exists() == false) {
			Messages.msgAndBackToHome("txt文件不存在", ReadActivity.this);
			return;
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && isReaderViewInit == false) {
			isReaderViewInit = true;
			// 获取用户绘制区域的宽高
			Rect viewAreaRect = new Rect();
			getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(viewAreaRect);

			ReadViewAdapter readViewAdapter = new ReadViewAdapter(ReadActivity.this, ReadActivity.this, txtFile, viewAreaRect.width(), viewAreaRect.height());
			readView = (ReadView) findViewById(R.id.readview);
			LinearLayout toolbarBottomLinearLayout = (LinearLayout) findViewById(R.id.readViewToolbar);
			ToolbarBottom toolbarBottom = new ToolbarBottom(readView, toolbarBottomLinearLayout, readViewAdapter.pagesCount);
			readView.setAdapter(readViewAdapter, toolbarBottom);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.read_activity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.readActivityMenuReadSetting) {
			Intent intent = new Intent(ReadActivity.this, ReadSettingActivity.class);
			intent.putExtra("txt_file_path", txtFile.getAbsolutePath());
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(ReadActivity.this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
