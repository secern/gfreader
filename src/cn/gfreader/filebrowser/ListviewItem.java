package cn.gfreader.filebrowser;

public class ListviewItem {
	
	public FileType fileType = FileType.UNKNOW;
	public String filePath, fileName;

	public enum FileType {
		DIRECTORY, PDF, TXT, UNKNOW
	}
}
