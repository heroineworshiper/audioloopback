/*
 * Audio Loopback
 * Copyright (C) 2013  Adam Williams <broadcast at earthling dot net>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 */

package com.AudioLoopback;

import wei.mark.standout.StandOutWindow;



import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends WindowBase implements OnSeekBarChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        Log.v("MainActivity", "onCreate");			

        Loopback.initialize(this);
        
        setContentView(R.layout.activity_main);

        SeekBar sb = (SeekBar)findViewById(R.id.speaker_gain);
        sb.setMax(100);
        sb.setProgress(gainToSlider(Loopback.speakerGain));
        sb.setOnSeekBarChangeListener(this);
        
        CheckBox checkBox = (CheckBox)findViewById(R.id.monitor_mic);
        checkBox.setChecked(Loopback.monitorMic);
        checkBox = (CheckBox)findViewById(R.id.reverb);
        checkBox.setChecked(Loopback.doReverb);
        checkBox = (CheckBox)findViewById(R.id.use_popup);
        checkBox.setChecked(Loopback.usePopup);

		paint = newPaint();


    }

	public void onResume() {
		Log.v("MainActivity", "onResume");
		super.onResume();
		
		StandOutWindow.closeAll(this, 
				MeterWindow.class);
	 	Loopback.activity = this;
	}

	public void onPause()
	{
		Log.v("MainActivity", "onPause");
		Loopback.saveSettings(this);
		Loopback.activity = null;

		if(Loopback.usePopup) StandOutWindow.show(this, 
				MeterWindow.class, 
				StandOutWindow.DEFAULT_ID);

		super.onPause();
	}

	public void onStop()
	{
		Log.v("MainActivity", "onStop");
		Loopback.saveSettings(this);
		Loopback.activity = null;

		super.onStop();
	}
	
	public static Paint newPaint() {
		Paint paint = new Paint();
		paint.setDither(true);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(1);
		return paint;
	}

    private int gainToSlider(float speakerGain) 
    {
    	if(speakerGain == 0.0) return 0;
    	
    	float db = todb(speakerGain);
    	int result = (int) (100.0 * (db - minSliderDB) / (maxSliderDB - minSliderDB));
		return result;
	}

	private float sliderToGain(int value) 
	{
		if(value == 0) return 0;
		
		float db = (float)value / 100 * (maxSliderDB - minSliderDB) + minSliderDB;
		float result = fromdb(db);
//		Log.v("MainActivity", "sliderToGain value=" + value + " db=" + db + " gain=" + result);
		return result;
	}
	
	static float todb(float value)
	{
		return (float) (20.0 * Math.log10(value));
	}
	
	static float fromdb(float db)
	{
		return (float) Math.pow(10, db / 20);
	}
	
	
	static void drawMeter(SurfaceView canvas1, 
			Paint paint,
			int level_i,
			int longLevel_i)
	{
      SurfaceHolder mSurfaceHolder = canvas1.getHolder();
      Canvas canvas = mSurfaceHolder.lockCanvas(null);


      if(canvas != null)
      {
      	canvas.drawColor(0, PorterDuff.Mode.CLEAR);
	
      	int h = canvas.getHeight();
      	int w = canvas.getWidth() - 1;
      	
      	
      	float db = todb((float)level_i / 32767);
      	if(db < minMeterDB) db = minMeterDB;
      	if(db > 0) db = 0;
      	int pixels = (int)(w * (db - minMeterDB) / (-minMeterDB));
      	
      	int x20 = (int)(w * ((-20 - minMeterDB) / (-minMeterDB)));
      	int x6 = (int)(w * ((-6 - minMeterDB) / (-minMeterDB)));
      	
        paint.setStyle(Paint.Style.FILL);
  		if(pixels < x20)
  		{
  			paint.setColor(Color.GREEN);
  			canvas.drawRect(new Rect(0, 0, pixels, h), paint);
  		}
  		else
  		if(pixels < x6)
  		{
  			paint.setColor(Color.GREEN);
 			canvas.drawRect(new Rect(0, 0, x20, h), paint);
  			paint.setColor(Color.YELLOW);
  			canvas.drawRect(new Rect(x20, 0, pixels, h), paint);
  		}
  		else
  		{
  			paint.setColor(Color.GREEN);
 			canvas.drawRect(new Rect(0, 0, x20, h), paint);
  			paint.setColor(Color.YELLOW);
  			canvas.drawRect(new Rect(x20, 0, x6, h), paint);
  			paint.setColor(Color.RED);
  			canvas.drawRect(new Rect(x6, 0, pixels, h), paint);

  		}
  		
      	float long_db = todb((float)longLevel_i / 32767);
      	if(long_db < minMeterDB) long_db = minMeterDB;
      	if(long_db > 0) long_db = 0;
      	int pixel = (int)(w * (long_db - minMeterDB) / (-minMeterDB));
        paint.setStyle(Paint.Style.STROKE);

        if(pixel < x20)
  		{
  			paint.setColor(Color.GREEN);
  		}
  		else
  		if(pixel < x6)
  		{
  			paint.setColor(Color.YELLOW);
  		}
  		else
  		{
  			paint.setColor(Color.RED);
  		}
        canvas.drawRect(new Rect(pixel, 0, pixel + 1, h), paint);

      		
  	    mSurfaceHolder.unlockCanvasAndPost(canvas);

      }
      else
      {
    	  Log.v("MainActivity", "drawMeter: no canvas");
      }
		
	}
	

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    public void updateGUI()
    {
    	if(Loopback.monitorMic == false &&
    			((CheckBox) findViewById(R.id.monitor_mic)).isChecked())
    	{
    		((CheckBox) findViewById(R.id.monitor_mic)).setChecked(false);
    		
	    	Builder dialog = new AlertDialog.Builder((Context) this);
	    	
	    	dialog.setMessage("Feedback detected.");
	    	dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() 
	    	{
	            public void onClick(DialogInterface dialog, int which) 
	            {
	            }

	        });
	    	dialog.show();

    	}
    	
    	if(Loopback.gotPeaks)
    	{
//    		Log.v("MainActivity", "updateGUI " + Loopback.micPeak + " " + Loopback.speakerPeak);
    		drawMeter((SurfaceView) findViewById(R.id.in_meter), 
    				paint,
    				Loopback.micPeak,
    				Loopback.micPeakLong);
    		drawMeter((SurfaceView) findViewById(R.id.out_meter), 
    				paint,
    				Loopback.speakerPeak,
    				Loopback.speakerPeakLong);
    		
//			Bundle data = new Bundle();
//			data.putInt("micPeak", Loopback.micPeak);
//			data.putInt("micPeakLong", Loopback.micPeakLong);
//			StandOutWindow.sendData(this, 
//					MeterWindow.class, 
//					StandOutWindow.DISREGARD_ID, 
//					1,
//					data,
//					null,
//					StandOutWindow.DISREGARD_ID);
// 
    		
    		Loopback.resetPeaks();
    	}
    	
    }
    
    
    
    
    
    
    public void onClick(View view)
    {
    	EditText text = null;
    	
    	
        switch (view.getId()){
	        	
        case R.id.monitor_mic:
        	Loopback.monitorMic = 
        		((CheckBox) findViewById(R.id.monitor_mic)).isChecked();
        case R.id.use_popup:
        	Loopback.usePopup = 
        		((CheckBox) findViewById(R.id.use_popup)).isChecked();
        case R.id.reverb:
        	Loopback.doReverb = 
        		((CheckBox) findViewById(R.id.reverb)).isChecked();
        	break;
        }
    }

	@Override
	public void onProgressChanged(SeekBar arg0, int value, boolean arg2) 
	{
		Loopback.speakerGain = sliderToGain(value);
//		Log.v("MainActivity", "onProgressChanged: value=" + value + 
//				" gain=" + Loopback.speakerGain);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) 
	{
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) 
	{
		
	}
    
	static float minMeterDB = -40;
	static float maxSliderDB = 12;
	static float minSliderDB = -12;
	Paint paint;
}
