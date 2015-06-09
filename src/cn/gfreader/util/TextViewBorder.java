package cn.gfreader.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewBorder extends TextView {

	public boolean isShowBorder = false;

	public TextViewBorder(Context context) {
		super(context);
	}

	public TextViewBorder(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TextViewBorder(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		Color color = new Color();
		int colorInt;
		if (isShowBorder) {
			colorInt = color.parseColor("#ffff0000");
		} else {
			colorInt = color.parseColor("#66333333");
		}
		Rect rect = canvas.getClipBounds();
		rect.left += 1;
		rect.top += 1;
		Paint paint = new Paint();
		paint.setColor(colorInt);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);
		canvas.drawRect(rect, paint);
	}
}
