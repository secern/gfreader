package cn.gfreader.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListviewAdapter extends ArrayAdapter<ListviewItem> {

	private Context mContext;

	public ListviewAdapter(Context context, int textViewResourceId, List<ListviewItem> objects) {
		super(context, textViewResourceId, objects);
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.main_activity_listview_item, null);
		TextView textView = (TextView) linearLayout.findViewById(R.id.mainactivityListviewItemName);
		ImageView imageView = (ImageView) linearLayout.findViewById(R.id.mainactivityListviewItemIcon);
		ListviewItem listviewItem = getItem(position);
		if (listviewItem.itemType == ListviewItem.ItemType.BOOK) {
			textView.setText(listviewItem.file.getName());
			String fileExt = listviewItem.bookName.substring(listviewItem.bookName.length() - 3, listviewItem.bookName.length());
			fileExt = fileExt.toLowerCase();
			InputStream inputStream = null;
			try {
				if ("txt".equals(fileExt)) {
					inputStream = mContext.getAssets().open("icons/txt.png");
				} else if ("pdf".equals(fileExt)) {
					inputStream = mContext.getAssets().open("icons/pdf.png");
				} else {
					inputStream = mContext.getAssets().open("icons/file.png");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
			imageView.setImageBitmap(bitmap);
		} else if (listviewItem.itemType == ListviewItem.ItemType.UNKNOW) {
			textView.setText(listviewItem.bookName + "(该书不存在)");
		} else if (listviewItem.itemType == ListviewItem.ItemType.BUTTON) {//创建button
			Button button = new Button(mContext);
			LinearLayout linearLayout2 = new LinearLayout(mContext);
			linearLayout2.addView(button);
			linearLayout2.setGravity(Gravity.CENTER);
			linearLayout2.setPadding(0, 10, 0, 10);
			button.setText("导入本机书籍");
			button.setTextColor(Color.parseColor("#ffffffff"));
			button.setGravity(Gravity.CENTER);
			button.setBackgroundResource(R.drawable.button_1);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, cn.gfreader.filebrowser.MainActivity.class);
					mContext.startActivity(intent);
				}
			});
			return linearLayout2;
		}
		return linearLayout;
	}
}
