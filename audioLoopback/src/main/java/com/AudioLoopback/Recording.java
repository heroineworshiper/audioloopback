/*
 * Audio Loopback
 * Copyright (C) 2013-2017  Adam Williams <broadcast at earthling dot net>
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;

public class Recording  extends Thread  {
    

// size in bytes of recording buffer
 	static int bufferSize = 0;
 	static int bufferSamples;
 	static int REFLECTIONS = 128;
	static int INITIAL_DELAY = 4000;
 	static float REVERB_LEVEL = (float)0.1;
 	int[] reflectionTimes = new int[REFLECTIONS];
 	// level * 256 for each reflection
 	int[] reflectionLevels = new int[REFLECTIONS];
 	short[] reverbData = new short[Settings.RECORDER_SAMPLERATE * 2];
 	
 	boolean prevMonitorMic;
 	int feedbackSamples;
 	int feedbackCounter;
 	int FEEDBACK_TOTAL = Settings.RECORDER_SAMPLERATE;
 	int FEEDBACK_MAX_SAMPLES = Settings.RECORDER_SAMPLERATE / 4;
 	int FEEDBACK_MAX_LEVEL = 32767;
	AudioManager audioManager;

	Recording()
	{
		Log.i("", "Recording.Recording");
	// size in bytes.
	// Must be supported for audiorecord to work
		bufferSize = AudioRecord.getMinBufferSize(
				Settings.RECORDER_SAMPLERATE,
    		AudioFormat.CHANNEL_IN_MONO, 
            AudioFormat.ENCODING_PCM_16BIT); 
		bufferSamples = bufferSize / 2;
        Log.v("Recording", "Recording: bufferSize=" + bufferSize);

		int totalSamples = reverbData.length - bufferSamples - INITIAL_DELAY;
		for(int i = 0; i < REFLECTIONS; i++)
		{
			reflectionTimes[i] = (int)(Math.random() *
					totalSamples + 
					INITIAL_DELAY);
//			reflectionTimes[i] = (REFLECTIONS - i) * totalSamples / 
//					REFLECTIONS + 
//					INITIAL_DELAY;
			reflectionLevels[i] = (int)(REVERB_LEVEL * 
					(totalSamples - (reflectionTimes[i] - INITIAL_DELAY)) * 
					256 / 
					totalSamples);
		}
	}


	private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
			Log.i("Recording", "bluetoothStateReceiver state=" + state);
			switch (state) {
				case AudioManager.SCO_AUDIO_STATE_CONNECTED:
					Log.i("Recording", "SCO_AUDIO_STATE_CONNECTED");
					audioManager.setBluetoothScoOn(true);
					break;
				case AudioManager.SCO_AUDIO_STATE_CONNECTING:
					break;
				case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
					break;
				case AudioManager.SCO_AUDIO_STATE_ERROR:
					break;
			}
		}
	};


	public void run()
    {
//    	startStream();


        short data[] = new short[bufferSize / 2];
        byte wavData[] = new byte[bufferSize * 2];

        boolean gotIt = false;
        AudioRecord recorder = null;

        // try to use bluetooth microphone
//		Mane.activity.registerReceiver(bluetoothStateReceiver, new IntentFilter(
//				AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
//
//		audioManager = (AudioManager) Mane.activity.getSystemService(Context.AUDIO_SERVICE);
//		if (!audioManager.isBluetoothScoAvailableOffCall())
//		{
//			Log.i("Recording", "SCO is not available, bluetooth recording is not possible");
//		}
//		else
//		{
//			if (!audioManager.isBluetoothScoOn())
//			{
//				Log.i("Recording", "starting bluetooth");
//				audioManager.startBluetoothSco();
//			}
//		}




Log.i("Recording", "run 1");
//		Mane.probe();

// open the good one
		for(int currentSource = 0;
        		currentSource < Settings.MAX_SOURCES  && !gotIt;
        		currentSource++)
        {
	        gotIt = true;



			try
	        {
	        	recorder = new AudioRecord(
	        		currentSource,
					Settings.RECORDER_SAMPLERATE,
	                AudioFormat.CHANNEL_IN_MONO,
	                AudioFormat.ENCODING_PCM_16BIT, 
	                bufferSize);
	
//	        Log.v("Loopback", "run: audio state=" + recorder.getRecordingState());
	        
	        	recorder.startRecording();
	        }
	        catch(Exception e)
	        {
//	        	Log.v("Recording", "startRecording: failed currentSource=" + currentSource);
	        	gotIt = false;
	        }
	        
	        if(gotIt)
	        {
	        	Log.i("Recording", "startRecording: got audio source " + currentSource);
	        }
        	
        }
//		Log.i("", "Recording.run 3");

		if(gotIt)
        {
	        while(true)
	        {
//				Log.i("", "Recording.run 4");

				recorder.read(data, 0, bufferSamples);
//				Log.i("", "Recording.run 5");



				int maxFeedbackLevel = (int)(FEEDBACK_MAX_LEVEL *
    					Settings.speakerGain);
        		if(Settings.monitorMic && Settings.doReverb)
        		{
        			int totalSamples = reverbData.length - bufferSamples - INITIAL_DELAY;
        			for(int i = 0; i < REFLECTIONS; i++)
        			{
        				reflectionTimes[i] = (int)(Math.random() *
        						totalSamples + 
        						INITIAL_DELAY);
        				reflectionLevels[i] = (int)(REVERB_LEVEL * 
        						(totalSamples - (reflectionTimes[i] - INITIAL_DELAY)) * 
        						256 / 
        						totalSamples);
//        				maxFeedbackLevel += FEEDBACK_MAX_LEVEL *
//        						reflectionLevels[i] * 
//        						Settings.speakerGain / 256;
        			}

        			for(int j = 0; j < REFLECTIONS; j++)
	        		{
	        			int start = reflectionTimes[j];
	        			int gain = reflectionLevels[j];
	        			for(int k = 0; k < bufferSamples; k++)
	        			{
	        				int sample = reverbData[start + k];
	        				sample += gain * data[k] / 256;
	        				sample = Mane.clamp(sample, -32767, 32767);
	        				reverbData[start + k] = (short) sample;
	        			}
	        		}
        		}
    			if(maxFeedbackLevel > FEEDBACK_MAX_LEVEL) maxFeedbackLevel = FEEDBACK_MAX_LEVEL;
        		
	        	
	        	int newPeak = 0;
	        	int newSpeakerPeak = 0;
	        	int outOffset = 0;
	        	boolean gotFeedback = false;
	        	for(int i = 0; i < bufferSamples; i++)
	        	{
	        		int sample = data[i];
	        		int magnitude = Math.abs(sample);
	        		if(magnitude > newPeak)
	        		{
	        			newPeak = magnitude;
	        		}
	        		
	        		
	        		if(Settings.monitorMic)
	        		{
		        		float outSample;
		        		if(Settings.doReverb)
		        		{
		        			outSample = (sample + reverbData[i]) * Settings.speakerGain;
		        		}
		        		else
		        		{
		        			outSample = sample * Settings.speakerGain;
		        		}

		        		int outSample_i = (int)outSample;
		        		magnitude = Math.abs(outSample_i);

		        		// test for feedback
		        		if(magnitude >= maxFeedbackLevel)
		        		{
		        			feedbackSamples++;
		        		}
		        		feedbackCounter++;
		        		
		        		if(feedbackCounter >= FEEDBACK_TOTAL)
		        		{
		        			if(feedbackSamples >= FEEDBACK_MAX_SAMPLES)
		        			{
Log.v("Recording", "run: gotFeedback feedbackSamples=" +
		feedbackSamples +
		" feedbackCounter=" +
		feedbackCounter);
		        				gotFeedback = true;
		        			}
		        			
		        			feedbackCounter = 0;
		        			feedbackSamples = 0;
		        		}
		        		
		        		
		        		outSample_i = Mane.clamp(outSample_i, -32767, 32767);
		        		
		        		
		        		
		        		
		        		magnitude = Math.abs(outSample_i);
		        		if(magnitude > newSpeakerPeak)
		        			newSpeakerPeak = magnitude;
	
		        		
		        		wavData[outOffset] = (byte)(outSample_i & 0xff);
		        		outOffset++;
		        		wavData[outOffset] = (byte)((outSample_i >> 8) & 0xff);
		        		outOffset++;
	        		}
	        	}
	        	
	        	
//Log.v("Recording", "run: newSpeakerPeak=" + newSpeakerPeak);
	        	Mane.updatePeaks(newPeak, newSpeakerPeak);

	        	
	        	if(Mane.meterWindow != null)
	        	{
					Bundle params = new Bundle();
					params.putInt("micPeak", Mane.micPeak);
					params.putInt("micPeakLong", Mane.micPeakLong);
					Mane.meterWindow.sendData(Mane.meterWindow.id,
		        			MeterWindow.class, 
							StandOutWindow.DISREGARD_ID, 
							1,
							params);
					if(Mane.activity == null) Mane.resetPeaks();

	        	}
	        	
        		if(Settings.monitorMic)
        		{
        			
        			
        			
        			
        			
        		// shift reverb buffer
        			if(Settings.doReverb)
        			{
			        	System.arraycopy(reverbData, 
			        			bufferSamples, 
			        			reverbData, 
			        			0, 
			        			reverbData.length - bufferSamples);
			        	for(int i = reverbData.length - bufferSamples; 
			        			i < reverbData.length; 
			        			i++)
			        	{
			        		reverbData[i] = 0;
			        	}
        			}

        			if(gotFeedback)
        			{
//Log.v("Recording", "run: gotFeedback");
        				Settings.monitorMic = false;
        			}
        			else
        			{
        				Mane.data.write(wavData, 0, outOffset);
        			}
	        	}
        		else
    			if(prevMonitorMic)
        		{
    				prevMonitorMic = false;
        			// reset output
    				for(int i = 0; i < reverbData.length; i++)
    				{
    					reverbData[i] = 0;
    				}
        			feedbackSamples = 0;
        			feedbackCounter = 0;
        		}


				if(Settings.wantStart)
				{
					Settings.isRecording = true;
					Settings.wantStart = false;
					Mane.recordingThread = new RecordingThread(Settings.currentRecording);
				}

				if(Settings.isRecording)
				{
					Mane.recordingThread.write(data);
				}
//	        	Log.v("Recording", "run: read peak=" + peak + " fifo=" + Loopback.data.getSize());
//	        	Log.v("Recording", "run: read fifo=" + Loopback.data.getSize());

				if(Settings.wantStop && Settings.isRecording)
				{
					Mane.recordingThread.stopRecording();
					Mane.recordingThread = null;
					Settings.isRecording = false;
					Settings.wantStop = false;
				}
	        	
	
	        }
        }
        else
        {
        	Log.v("Recording", "startRecording: no audio source found");

        }
        	
    }
}









