package com.AudioLoopback;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

public class MeterWindow extends StandOutWindow
{

	@Override
	public String getAppName() {
//		Log.v("MeterWindow", "getAppName");
		return "MeterWindow";
	}

	@Override
	public int getAppIcon() {
//		Log.v("MeterWindow", "getAppIcon");
		return android.R.drawable.ic_menu_close_clear_cancel;
	}

	@Override
	public void createAndAttachView(int id, FrameLayout frame) {
		// create a new layout from body.xml
		
//		Log.v("MeterWindow", "createAndAttachView");
		this.id = id;
		LayoutInflater inflater = (LayoutInflater) getSystemService(
				LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.meter_window, frame, true);
		Mane.meterWindow = this;
	}

	@Override
	public StandOutLayoutParams getParams(int id, Window window) {
//		Log.v("MeterWindow", "getParams");
		return new StandOutLayoutParams(id, 
				300, 
				60,
				Settings.x, 
				Settings.y);
	}


	// move the window by dragging the view
	@Override
	public int getFlags(int id) {
		return super.getFlags(id) | 
				StandOutFlags.FLAG_BODY_MOVE_ENABLE | 
				StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
	}

	@Override
	public String getPersistentNotificationMessage(int id) {
		return "Click to close the meter window";
	}

	@Override
	public Intent getPersistentNotificationIntent(int id) {
		return StandOutWindow.getCloseIntent(this, 
				MeterWindow.class, 
				id);
	}

	
	@Override
	public void onReceiveData(int id, 
			int requestCode, 
			Bundle data,
			Class<? extends StandOutWindow> fromCls, 
			int fromId) 
	{
		int micPeak = data.getInt("micPeak");
		int micPeakLong = data.getInt("micPeakLong");
		
//		Log.v("MeterWindow", "onReceiveData " + micPeak);
		if(paint == null)
		{
			
			paint = MainActivity.newPaint();
			
			
		}
		
		if(view != null)
		{
			MainActivity.drawMeter(
				(SurfaceView) view.findViewById(R.id.meter_window_meter), 
				paint,
				micPeak,
				micPeakLong);
		}
	}
	
	public void onMove(int id, Window window, View view, MotionEvent event) 
	{
		StandOutLayoutParams params = window.getLayoutParams();
		Settings.x = (int) params.x;
		Settings.y = (int) params.y;
	}
	
	public boolean onClose(int id, Window window) {
		Mane.meterWindow = null;
		
//		Log.v("MeterWindow", "onClose " + window.getX() + " " + window.getY());
		return super.onClose(id, window);
	}

	int id;
	View view;
	Paint paint = null;
}

