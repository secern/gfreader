package cn.gfreader.txt;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import cn.gfreader.main.R;

public class ToolbarBottom {

	private LinearLayout toolbarLayout;
	private ReadView readView;
	private SeekBar seekBar;
	private boolean isVisible = false, isSettingSeekbar = false;
	private int pagesCount;

	public ToolbarBottom(ReadView readView, LinearLayout linearLayout, int pagesCount) {
		this.readView = readView;
		this.pagesCount = pagesCount;
		toolbarLayout = linearLayout;
		seekBar = (SeekBar) toolbarLayout.findViewById(R.id.readViewSeekbar);
	}

	public void seekBarInit(int curPageNum) {
		// 设置进度
		int progress = (int) ((curPageNum * 1.0f) / pagesCount * seekBar.getMax());
		progress = curPageNum == 1 ? 0 : progress;
		seekBar.setProgress(progress);

		// 绑定事件
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (isSettingSeekbar) {
					return;
				}
				// 计算要跳转到的页数
				int moveToPageNum = (int) (progress * 1.0f / seekBar.getMax() * pagesCount);
				moveToPageNum = moveToPageNum <= 0 ? 1 : moveToPageNum;
				readView.moveToPage(moveToPageNum);
			}
		});
	}

	/*
	 * 设置进度条的进度
	 */
	public void setSeekbarProgress(int curPageNum) {
		isSettingSeekbar = true;
		int progress = (int) ((curPageNum * 1.0f) / pagesCount * seekBar.getMax());
		progress = curPageNum == 1 ? 0 : progress;
		seekBar.setProgress(progress);
		isSettingSeekbar = false;
	}

	/*
	 * 显示或者隐藏底部工具栏
	 */
	public void visibleToggle() {
		if (isVisible) {
			toolbarLayout.setVisibility(View.GONE);
		} else {
			toolbarLayout.setVisibility(View.VISIBLE);
			toolbarLayout.bringToFront();
		}
		isVisible = !isVisible;
	}
}
