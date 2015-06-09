package cn.gfreader.main;

import java.io.File;

public class ListviewItem {
	
	public File file;
	public String bookName;
	public ItemType itemType;
	public enum ItemType{
		BOOK, BUTTON, UNKNOW
	}
}
