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


import wei.mark.standout.StandOutWindow.StandOutLayoutParams;
import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class Loopback extends IntentService {
	
	
	public Loopback() {
		super("LoopbackService");
	}

	@Override
    protected void onHandleIntent(Intent intent) 
	{
		loopback = this;

		run();
	}
	
	static public void initialize(Activity activity)
	{

		
		
		if(loopback == null)
		{
			Loopback.loadSettings(activity);
			Intent mServiceIntent = new Intent(activity, Loopback.class);
			activity.startService(mServiceIntent);
		}
		
		
		
//		if(loopback == null)
//		{
//			loopback = new Loopback();
//			loopback.loadSettings(activity);
//			loopback.start();
//		}
    	
	}
	
	
	
	static void loadSettings(Activity activity)
	{
		SharedPreferences file = activity.getSharedPreferences(
				"LoopbackPrefs", 0);

		speakerGain = file.getFloat("speaker_gain", speakerGain);
		monitorMic = (file.getInt("monitor_mic", monitorMic ? 1 : 0) == 1 ? true : false);
		doReverb = (file.getInt("do_reverb", doReverb ? 1 : 0) == 1 ? true : false);
		usePopup = (file.getInt("use_popup", usePopup ? 1 : 0) == 1 ? true : false);
		x = file.getInt("x", x);
		y = file.getInt("y", y);

	}
	
	
	static void saveSettings(Activity activity)
	{
		SharedPreferences file = activity.getSharedPreferences(
				"LoopbackPrefs", 0);
		SharedPreferences.Editor file2 = file.edit();

		file2.putFloat("speaker_gain", speakerGain);
		file2.putInt("monitor_mic", monitorMic ? 1 : 0);
		file2.putInt("do_reverb", doReverb ? 1 : 0);
		file2.putInt("use_popup", usePopup ? 1 : 0);
		file2.putInt("x", x);
		file2.putInt("y", y);
		file2.commit();
	
	}
	
	
    public void run() 
    {

    	Recording recording = new Recording();
    	Playback playback = new Playback();

    	// make buffers equal size for the fifo to work
    	// doesn't work
//    	if(recording.bufferSize < playback.bufferSize)
//    	{
//    		recording.bufferSize = playback.bufferSize;
//    	}
//    	
//    	if(playback.bufferSize < recording.bufferSize)
//    	{
//    		playback.bufferSize = recording.bufferSize;
//    	}

    	// make recording buffer a multiple bigger than the playback buffer
    	int bufferSize = recording.bufferSize;
    	while(playback.bufferSize > bufferSize) bufferSize *= 2;
		
    	Log.v("Loopback", "run: using fifo bufferSize=" + bufferSize);
    	Loopback.data = new Fifo(bufferSize);

    	
    	recording.start();
    	playback.start();

    	
    	try {
			Thread.sleep(100000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
	

    static synchronized void updatePeaks(int micPeak, int speakerPeak)
    {
    	gotPeaks = true;
    	if(micPeak > Loopback.micPeak) Loopback.micPeak = micPeak;
    	if(speakerPeak > Loopback.speakerPeak) Loopback.speakerPeak = speakerPeak;
    	
    	totalMicPeaks++;
    	totalSpeakerPeaks++;
    	
    	if(micPeak > micPeakLong)
    	{
    		micPeakLong = micPeak;
    		totalMicPeaks = 0;
    	}
    	else
    	if(totalMicPeaks > MAX_PEAKS)
    	{
    		totalMicPeaks = 0;
    		micPeakLong = micPeak;
    	}
    	
    	if(speakerPeak > speakerPeakLong)
    	{
    		speakerPeakLong = speakerPeak;
    		totalSpeakerPeaks = 0;
    	}
    	else
   		if(totalSpeakerPeaks > MAX_PEAKS)
   		{
   			speakerPeakLong = speakerPeak;
   			totalSpeakerPeaks = 0;
   			
   		}
    }

    static synchronized void resetPeaks()
    {
    	gotPeaks = false;
    	Loopback.micPeak = 0;
    	Loopback.speakerPeak = 0;
    }

    
    static float clamp(float x, float min, float max)
    {
    	if(x < min) 
    		x = min;
    	else
    	if(x > max) 
    		x = max;
    	
    	return x;
    }

    static int clamp(int x, int min, int max)
    {
    	if(x < min) 
    		x = min;
    	else
    	if(x > max) 
    		x = max;
    	
    	return x;
    }

 	static int micPeak;
 	static int speakerPeak;
 	static int micPeakLong;
 	static int speakerPeakLong;
 	static boolean gotPeaks;
 	static int totalMicPeaks;
 	static int totalSpeakerPeaks;
 	static int MAX_PEAKS = 30;
 	static Activity activity;

	static int RECORDER_SAMPLERATE = 48000;
    static boolean useMediaPlayer = false;
    static boolean monitorMic = false;
    static boolean doReverb = false;
    static float speakerGain = (float)1.0;
    static boolean usePopup = true;
    static int x = StandOutLayoutParams.CENTER;
    static int y = StandOutLayoutParams.CENTER;
	static Loopback loopback = null;
	static MeterWindow meterWindow;
	static Fifo data;
}
