/*
 * Audio Mane
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


import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Build;
import android.util.Log;

public class Mane extends IntentService {

 	static int micPeak;
 	static int speakerPeak;
 	static int micPeakLong;
 	static int speakerPeakLong;
 	static boolean gotPeaks;
 	static int totalMicPeaks;
 	static int totalSpeakerPeaks;
 	static int MAX_PEAKS = 30;
 	static MainActivity activity;
	static Settings settings;
	static Recording recording;
	static Playback playback;
	static RecordingThread recordingThread;

	static Mane mane = null;
	static MeterWindow meterWindow;
	static Fifo data;
	
	
	public Mane() {
		super("ManeService");
	}

	@Override
    protected void onHandleIntent(Intent intent) 
	{
		mane = this;

		run();
	}
	
	static public void initialize(MainActivity activity)
	{
		Log.i("Mane", "initialize 1");

		
		
		if(mane == null)
		{
			Settings.loadSettings(activity);
			Intent mServiceIntent = new Intent(activity, Mane.class);
			activity.startService(mServiceIntent);
		}
		
		
		
//		if(mane == null)
//		{
//			mane = new Mane();
//			mane.loadSettings(activity);
//			mane.start();
//		}
    	
	}
	
	
    public void run() 
    {
		Log.i("Mane", "run 1");


    	recording = new Recording();
    	playback = new Playback();


    	// make recording buffer a multiple bigger than the playback buffer
    	int bufferSize = recording.bufferSize;
    	while(playback.bufferSize > bufferSize) bufferSize *= 2;
		
    	Log.v("", "Mane.run: using fifo bufferSize=" + bufferSize);
    	Mane.data = new Fifo(bufferSize);

    	
    	recording.start();
    	playback.start();

    	
    	try {
			Thread.sleep(100000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    static void probe()
	{
		// probe all the recording devices
		for(int currentSource = 0;
			currentSource < Settings.MAX_SOURCES;
			currentSource++)
		{
			AudioRecord recorder = null;
			try
			{
				recorder = new AudioRecord(
						currentSource,
						Settings.RECORDER_SAMPLERATE,
						AudioFormat.CHANNEL_IN_MONO,
						AudioFormat.ENCODING_PCM_16BIT,
						AudioRecord.getMinBufferSize(
								Settings.RECORDER_SAMPLERATE,
								AudioFormat.CHANNEL_IN_MONO,
								AudioFormat.ENCODING_PCM_16BIT));
				recorder.startRecording();

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
					Log.i("Mane", "run: currentSource=" + currentSource +
							" address=" + recorder.getRoutedDevice().getAddress() +
							" type=" + recorder.getRoutedDevice().getType());
				}
			}
			catch(Exception e)
			{
				//Log.i("Mane", "run: currentSource=" +
				//		currentSource +
				//		" FAILED");
			}
			recorder = null;
		}



	}

    static synchronized void updatePeaks(int micPeak, int speakerPeak)
    {
    	gotPeaks = true;
    	if(micPeak > Mane.micPeak) Mane.micPeak = micPeak;
    	if(speakerPeak > Mane.speakerPeak) Mane.speakerPeak = speakerPeak;
    	
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
    	Mane.micPeak = 0;
    	Mane.speakerPeak = 0;
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
}
