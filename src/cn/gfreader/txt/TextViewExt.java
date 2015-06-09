package cn.gfreader.txt;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewExt extends TextView {

	public TextViewExt(Context context) {
		super(context);
		// TODO 自动生成的构造函数存根
	}

	public TextViewExt(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO 自动生成的构造函数存根
	}

	public TextViewExt(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// TODO 自动生成的方法存根
		super.onLayout(changed, left, top, right, bottom);
		removeHideText();
	}

	/**
	 * 去除当前页中没有显示出来的字，并返回去除的字数
	 * 
	 * @return 字数
	 */
	public int removeHideText() {
		CharSequence oldContent = getText();
		CharSequence newContent = oldContent.subSequence(0, getTextCount());
		setText(newContent);
		return oldContent.length() - newContent.length();
	}

	/**
	 * 获取当前页总字数
	 */
	public int getTextCount() {
		return getLayout().getLineEnd(getLinesCount());
	}

	/**
	 * 获取当前页总行数
	 */
	public int getLinesCount() {
		Layout layout = getLayout();
		int topOfLastLine = getHeight() - getPaddingTop() - getPaddingBottom() - getLineHeight();
		return layout.getLineForVertical(topOfLastLine);
	}
}
