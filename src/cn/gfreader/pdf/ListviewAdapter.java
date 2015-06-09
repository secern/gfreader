package cn.gfreader.pdf;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import cn.gfreader.main.R;

public class ListviewAdapter extends ArrayAdapter<ListviewItem> {

	private Context mContext;
	
	public ListviewAdapter(Context context, int textViewResourceId,
			List<ListviewItem> objects) {
		super(context, textViewResourceId, objects);
		
		this.mContext = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.pdf_listview_item, null);
		ImageView imageView = (ImageView) view.findViewById(R.id.pdfListviewItemImageview);
		ListviewItem listviewItem = getItem(position);
		imageView.setImageBitmap(listviewItem.getBitmap());
		LayoutParams layoutParams = imageView.getLayoutParams();
		layoutParams.width = (int) listviewItem.getWidth();
		layoutParams.height = (int) listviewItem.getHeight();
		imageView.setLayoutParams(layoutParams);
		return view;
	}
}
