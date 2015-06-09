package cn.gfreader.txt;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Config {

	private SharedPreferences sharedPreferences;
	private String[] configKeyStrings = new String[] { "readview_paddingLeft", "readview_paddingRight", "readview_paddingTop", "readview_paddingBottom", "readview_lineSpacingMultiplier", "readview_textSize", "readview_bg" };

	public Config(Context context) {
		sharedPreferences = context.getSharedPreferences("textReaderConfig", Context.MODE_PRIVATE);
		Map<String, ?> map = sharedPreferences.getAll();
		if (map.isEmpty()) {
			Editor editor = sharedPreferences.edit();
			editor.putInt("readview_paddingLeft", 10);
			editor.putInt("readview_paddingRight", 10);
			editor.putInt("readview_paddingTop", 10);
			editor.putInt("readview_paddingBottom", 10);
			editor.putFloat("readview_lineSpacingMultiplier", 1f);
			editor.putInt("readview_textSize", 16);
			editor.putString("readview_bg", "1.jpg");
			editor.commit();
			editor.clear();
		}
		map = null;
	}

	public String getString(String key) {
		return sharedPreferences.getString(key, null);
	}

	public int getInt(String key) {
		return sharedPreferences.getInt(key, 0);
	}

	public float getFloat(String key) {
		return sharedPreferences.getFloat(key, 0);
	}

	public boolean set(String key, String value) {
		if (isKeyExist(key)) {
			Editor editor = sharedPreferences.edit();
			editor.putString(key, value);
			editor.commit();
			editor.clear();
			return true;
		}
		return false;
	}

	public boolean set(String key, int value) {
		if (isKeyExist(key)) {
			Editor editor = sharedPreferences.edit();
			editor.putInt(key, value);
			editor.commit();
			editor.clear();
			return true;
		}
		return false;
	}

	public boolean set(String key, float value) {
		if (isKeyExist(key)) {
			Editor editor = sharedPreferences.edit();
			editor.putFloat(key, value);
			editor.commit();
			editor.clear();
			return true;
		}
		return false;
	}

	/*
	 * 检查键值是否定义，防止设置时输入错误
	 */
	public boolean isKeyExist(String key) {
		boolean isExist = false;
		for (int i = 0; i < configKeyStrings.length; i++) {
			if (configKeyStrings[i].equals(key)) {
				isExist = true;
				break;
			}
		}
		return isExist;
	}
}
