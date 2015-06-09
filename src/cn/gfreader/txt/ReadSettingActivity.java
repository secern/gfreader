package cn.gfreader.txt;

import java.io.File;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import cn.gfreader.db.Db;
import cn.gfreader.main.R;
import cn.gfreader.util.FileOperations;
import cn.gfreader.util.ImageViewBorder;
import cn.gfreader.util.Messages;
import cn.gfreader.util.TextViewBorder;

public class ReadSettingActivity extends Activity implements OnClickListener {

	private int fontSize;
	private float rowSpacingMulti;
	private String bgName;
	private File txtFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read_setting_activity);

		// 初始化TXT
		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {
			String txtFilePath = bundle.getString("txt_file_path");
			txtFile = new File(txtFilePath);
		}
		if (txtFile.exists() == false) {
			Messages.msgAndBackToHome("读取PDF文件失败", this);
			return;
		}

		Config config = new Config(ReadSettingActivity.this);

		// 初始化字号设置
		fontSize = config.getInt("readview_textSize");
		TextViewBorder textViewBorder = null;
		switch (fontSize) {
		case 14:
			textViewBorder = (TextViewBorder) findViewById(R.id.readSettingFontSize14);
			break;
		case 16:
			textViewBorder = (TextViewBorder) findViewById(R.id.readSettingFontSize16);
			break;
		case 18:
			textViewBorder = (TextViewBorder) findViewById(R.id.readSettingFontSize18);
			break;
		default:
			break;
		}
		textViewBorder.isShowBorder = true;
		textViewBorder.invalidate();
		findViewById(R.id.readSettingFontSize14).setOnClickListener(this);
		findViewById(R.id.readSettingFontSize16).setOnClickListener(this);
		findViewById(R.id.readSettingFontSize18).setOnClickListener(this);

		// 初始化行距设置
		rowSpacingMulti = config.getFloat("readview_lineSpacingMultiplier");
		int rowSpacingMultiInt = (int) (rowSpacingMulti * 10);
		switch (rowSpacingMultiInt) {
		case 10:
			textViewBorder = (TextViewBorder) findViewById(R.id.readSettingRowSpacing_1);
			break;
		case 15:
			textViewBorder = (TextViewBorder) findViewById(R.id.readSettingRowSpacing_1_5);
			break;
		case 20:
			textViewBorder = (TextViewBorder) findViewById(R.id.readSettingRowSpacing_2);
			break;
		default:
			break;
		}
		textViewBorder.isShowBorder = true;
		textViewBorder.invalidate();
		findViewById(R.id.readSettingRowSpacing_1).setOnClickListener(this);
		findViewById(R.id.readSettingRowSpacing_1_5).setOnClickListener(this);
		findViewById(R.id.readSettingRowSpacing_2).setOnClickListener(this);

		// 初始化背景设置
		bgName = config.getString("readview_bg");
		ImageViewBorder imageViewBorder = null;
		if ("1.jpg".equals(bgName)) {
			imageViewBorder = (ImageViewBorder) findViewById(R.id.readSettingBg1);
		} else if ("2.jpg".equals(bgName)) {
			imageViewBorder = (ImageViewBorder) findViewById(R.id.readSettingBg2);
		} else if ("3.jpg".equals(bgName)) {
			imageViewBorder = (ImageViewBorder) findViewById(R.id.readSettingBg3);
		}
		imageViewBorder.isShowBorder = true;
		imageViewBorder.invalidate();
		findViewById(R.id.readSettingBg1).setOnClickListener(this);
		findViewById(R.id.readSettingBg2).setOnClickListener(this);
		findViewById(R.id.readSettingBg3).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		// 字号
		if (id == R.id.readSettingFontSize14 || id == R.id.readSettingFontSize16 || id == R.id.readSettingFontSize18) {
			TextViewBorder textViewBorder;
			textViewBorder = (TextViewBorder) findViewById(R.id.readSettingFontSize14);
			textViewBorder.isShowBorder = false;
			textViewBorder.invalidate();
			textViewBorder = (TextViewBorder) findViewById(R.id.readSettingFontSize16);
			textViewBorder.isShowBorder = false;
			textViewBorder.invalidate();
			textViewBorder = (TextViewBorder) findViewById(R.id.readSettingFontSize18);
			textViewBorder.isShowBorder = false;
			textViewBorder.invalidate();

			textViewBorder = (TextViewBorder) findViewById(id);
			textViewBorder.isShowBorder = true;
			textViewBorder.invalidate();

			fontSize = Integer.parseInt(textViewBorder.getText().toString());
		}
		// 行距
		if (id == R.id.readSettingRowSpacing_1 || id == R.id.readSettingRowSpacing_1_5 || id == R.id.readSettingRowSpacing_2) {
			TextViewBorder textViewBorder;
			textViewBorder = (TextViewBorder) findViewById(R.id.readSettingRowSpacing_1);
			textViewBorder.isShowBorder = false;
			textViewBorder.invalidate();
			textViewBorder = (TextViewBorder) findViewById(R.id.readSettingRowSpacing_1_5);
			textViewBorder.isShowBorder = false;
			textViewBorder.invalidate();
			textViewBorder = (TextViewBorder) findViewById(R.id.readSettingRowSpacing_2);
			textViewBorder.isShowBorder = false;
			textViewBorder.invalidate();

			textViewBorder = (TextViewBorder) findViewById(id);
			textViewBorder.isShowBorder = true;
			textViewBorder.invalidate();

			rowSpacingMulti = Float.parseFloat(textViewBorder.getText().toString());
		}
		// 背景
		if (id == R.id.readSettingBg1 || id == R.id.readSettingBg2 || id == R.id.readSettingBg3) {
			ImageViewBorder imageViewBorder;
			imageViewBorder = (ImageViewBorder) findViewById(R.id.readSettingBg1);
			imageViewBorder.isShowBorder = false;
			imageViewBorder.invalidate();
			imageViewBorder = (ImageViewBorder) findViewById(R.id.readSettingBg2);
			imageViewBorder.isShowBorder = false;
			imageViewBorder.invalidate();
			imageViewBorder = (ImageViewBorder) findViewById(R.id.readSettingBg3);
			imageViewBorder.isShowBorder = false;
			imageViewBorder.invalidate();

			imageViewBorder = (ImageViewBorder) findViewById(id);
			imageViewBorder.isShowBorder = true;
			imageViewBorder.invalidate();

			bgName = imageViewBorder.getContentDescription().toString();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			Config config = new Config(ReadSettingActivity.this);

			boolean isNeedPagination = false;
			boolean isNeedReadActivityReload = false;
			if (bgName != null && bgName.equals(config.getString("readview_bg")) == false) {
				isNeedReadActivityReload = true;
				config.set("readview_bg", bgName);
			}
			if (fontSize != config.getInt("readview_textSize")) {
				isNeedPagination = true;
				config.set("readview_textSize", fontSize);
			}
			if (rowSpacingMulti != config.getFloat("readview_lineSpacingMultiplier")) {
				isNeedPagination = true;
				config.set("readview_lineSpacingMultiplier", rowSpacingMulti);
			}
			if (isNeedPagination) {
				String bookMd5 = FileOperations.GetMd5(txtFile.getAbsolutePath());
				SQLiteDatabase writableDatabase = Db.getInstance(ReadSettingActivity.this).getWritableDatabase();
				writableDatabase.delete("text_pagination", "book_md5=?", new String[] { bookMd5 });
				ContentValues contentValues = new ContentValues();
				contentValues.put("is_pagination_finish", 0);
				writableDatabase.update("books", contentValues, "book_md5=?", new String[] { bookMd5 });

				Intent intent = new Intent(ReadSettingActivity.this, PaginationActivity.class);
				intent.putExtra("txt_file_path", txtFile.getAbsolutePath());
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			}
			if (isNeedReadActivityReload) {
				Intent intent = new Intent(ReadSettingActivity.this, ReadActivity.class);
				intent.putExtra("txt_file_path", txtFile.getAbsolutePath());
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
