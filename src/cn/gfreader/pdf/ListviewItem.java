package cn.gfreader.pdf;

import android.graphics.Bitmap;

public class ListviewItem {
	
	private Bitmap bitmap;
	private float width, height;
	
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	public Bitmap getBitmap() {
		return this.bitmap;
	}
	
	public void setWidth(float width) {
		this.width = width;
	}
	
	public float getWidth() {
		return this.width;
	}
	
	public void setHeight(float height) {
		this.height = height;
	}
	
	public float getHeight() {
		return this.height;
	}
}
