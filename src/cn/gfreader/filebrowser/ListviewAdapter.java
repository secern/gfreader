package cn.gfreader.filebrowser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.gfreader.main.R;

public class ListviewAdapter extends ArrayAdapter<ListviewItem> {

	private Context mContext;

	public ListviewAdapter(Context context, int textViewResourceId, List<ListviewItem> objects) {
		super(context, textViewResourceId, objects);
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.filebrowser_listview_item, null);
		TextView textView = (TextView) linearLayout.findViewById(R.id.filebrowserListviewItemFilename);
		ListviewItem listviewItem = getItem(position);
		textView.setText(listviewItem.fileName);

		// 设置图标
		ImageView imageView = (ImageView) linearLayout.findViewById(R.id.filebrowserListviewItemIcon);
		String iconPath = null;
		if (listviewItem.fileType == ListviewItem.FileType.TXT) {
			iconPath = "icons/txt.png";
		} else if (listviewItem.fileType == ListviewItem.FileType.PDF) {
			iconPath = "icons/pdf.png";
		} else if (listviewItem.fileType == ListviewItem.FileType.UNKNOW) {
			iconPath = "icons/file.png";
		} else if (listviewItem.fileType == ListviewItem.FileType.DIRECTORY) {
			iconPath = "icons/folder.png";
		}
		try {
			InputStream inputStream = mContext.getAssets().open(iconPath);
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
			imageView.setImageBitmap(bitmap);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return linearLayout;
		// return super.getView(position, convertView, parent);
	}
}
