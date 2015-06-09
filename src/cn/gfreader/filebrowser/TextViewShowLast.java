package cn.gfreader.filebrowser;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewShowLast extends TextView {

	public TextViewShowLast(Context context) {
		super(context);
	}

	public TextViewShowLast(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TextViewShowLast(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/*
	 * 当超过能显示的字符数时，把前面部分去除（非 Javadoc）
	 * @see android.widget.TextView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);
		String text = (String) getText();
		Paint paint = getPaint();
		int viewWidth = getWidth();
		StringBuffer stringBuffer = new StringBuffer();
		String string = null;
		for (int i = text.length() - 1; i >= 0; i--) {
			if (paint.measureText(stringBuffer.toString()) > viewWidth - 10) {
				stringBuffer.delete(0, stringBuffer.toString().length());
				stringBuffer.append(text.substring(i + 1, text.length()));
				string = stringBuffer.toString();
				break;
			}
			stringBuffer.append(text.charAt(i));
		}
		if (string == null) {
			string = stringBuffer.reverse().toString();
		}
		canvas.drawText(string, 0, getPaddingTop() + getLineHeight(), paint);
	}

}
