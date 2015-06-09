package cn.gfreader.util;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import cn.gfreader.main.MainActivity;

public class Messages {

	public static void msgAndBackToHome(String content, Context context) {
		Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(context, MainActivity.class);
		context.startActivity(intent);
	}
	
	public static void msg(String content, Context context) {
		Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
	}

}
