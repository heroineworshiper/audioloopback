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
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;

public class Recording  extends Thread  {

	Recording()
	{
	// size in bytes.
	// Must be supported for audiorecord to work
		bufferSize = AudioRecord.getMinBufferSize(
    		RECORDER_SAMPLERATE,
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
	
	public void startStream()
	{
    	// write WAV header
    	byte[] header = new byte[44];
    	int offset = 0;
    	header[offset++] = 'R';
    	header[offset++] = 'I';
    	header[offset++] = 'F';
    	header[offset++] = 'F';
    	header[offset++] = (byte) 0xff;
    	header[offset++] = (byte) 0xff;
    	header[offset++] = (byte) 0xff;
    	header[offset++] = (byte) 0x7f;
    	header[offset++] = 'W';
    	header[offset++] = 'A';
    	header[offset++] = 'V';
    	header[offset++] = 'E';
    	header[offset++] = 'f';
    	header[offset++] = 'm';
    	header[offset++] = 't';
    	header[offset++] = ' ';
    	header[offset++] = 16;
    	header[offset++] = 0;
    	header[offset++] = 0;
    	header[offset++] = 0;
    	header[offset++] = 1;
    	header[offset++] = 0;
    	// channels
    	header[offset++] = 2;
    	header[offset++] = 0;
    	// samplerate
    	header[offset++] = (byte)(RECORDER_SAMPLERATE & 0xff);
    	header[offset++] = (byte)((RECORDER_SAMPLERATE >> 8) & 0xff);
    	header[offset++] = (byte)((RECORDER_SAMPLERATE >> 16) & 0xff);
    	header[offset++] = (byte)((RECORDER_SAMPLERATE >> 24) & 0xff);
    	// byterate
    	int byterate = RECORDER_SAMPLERATE * 2 * 2;
    	header[offset++] = (byte)(byterate & 0xff);
    	header[offset++] = (byte)((byterate >> 8) & 0xff);
    	header[offset++] = (byte)((byterate >> 16) & 0xff);
    	header[offset++] = (byte)((byterate >> 24) & 0xff);
    	// blockalign
    	header[offset++] = 4;
    	header[offset++] = 0;
    	// bits per sample
    	header[offset++] = 16;
    	header[offset++] = 0;
    	header[offset++] = 'd';
    	header[offset++] = 'a';
    	header[offset++] = 't';
    	header[offset++] = 'a';
    	header[offset++] = (byte) 0xff;
    	header[offset++] = (byte) 0xff;
    	header[offset++] = (byte) 0xff;
    	header[offset++] = (byte) 0x7f;
    	Loopback.data.write(header, 0, header.length);

		
	}
	
	
    public void run() 
    {
//    	startStream();


        short data[] = new short[bufferSize / 2];
        byte wavData[] = new byte[bufferSize * 2];

        boolean gotIt = false;
        AudioRecord recorder = null;
        for(int currentSource = 0; 
        		currentSource < 16  && !gotIt; 
        		currentSource++)
        {
	        gotIt = true;
	        
	        
	        
	        try
	        {
	        	recorder = new AudioRecord(
	        		currentSource,
	                RECORDER_SAMPLERATE, 
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
	        	Log.v("Recording", "startRecording: got audio source " + currentSource);
	        }
        	
        }
        
        if(gotIt)
        {
        	
        	

        	

        	
        	
	        while(true)
	        {
	        	recorder.read(data, 0, bufferSamples);
	        	
	        	

        		int maxFeedbackLevel = (int)(FEEDBACK_MAX_LEVEL * 
    					Loopback.speakerGain);
        		if(Loopback.monitorMic && Loopback.doReverb)
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
//        						Loopback.speakerGain / 256;
        			}

        			for(int j = 0; j < REFLECTIONS; j++)
	        		{
	        			int start = reflectionTimes[j];
	        			int gain = reflectionLevels[j];
	        			for(int k = 0; k < bufferSamples; k++)
	        			{
	        				int sample = reverbData[start + k];
	        				sample += gain * data[k] / 256;
	        				sample = Loopback.clamp(sample, -32767, 32767);
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
	        		
	        		
	        		if(Loopback.monitorMic)
	        		{
		        		float outSample;
		        		if(Loopback.doReverb)
		        		{
		        			outSample = (sample + reverbData[i]) * Loopback.speakerGain;
		        		}
		        		else
		        		{
		        			outSample = sample * Loopback.speakerGain;
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
		        		
		        		
		        		outSample_i = Loopback.clamp(outSample_i, -32767, 32767);
		        		
		        		
		        		
		        		
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
	        	Loopback.updatePeaks(newPeak, newSpeakerPeak);

	        	
	        	if(Loopback.meterWindow != null)
	        	{
					Bundle params = new Bundle();
					params.putInt("micPeak", Loopback.micPeak);
					params.putInt("micPeakLong", Loopback.micPeakLong);
					Loopback.meterWindow.sendData(Loopback.meterWindow.id,
		        			MeterWindow.class, 
							StandOutWindow.DISREGARD_ID, 
							1,
							params);
					if(Loopback.activity == null) Loopback.resetPeaks();

	        	}
	        	
        		if(Loopback.monitorMic)
        		{
        			
        			
        			
        			
        			
        		// shift reverb buffer
        			if(Loopback.doReverb)
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
        				Loopback.monitorMic = false;
        			}
        			else
        			{
        				Loopback.data.write(wavData, 0, outOffset);
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


//	        	Log.v("Recording", "run: read peak=" + peak + " fifo=" + Loopback.data.getSize());
//	        	Log.v("Recording", "run: read fifo=" + Loopback.data.getSize());
		      	
	        	
	
	        }
        }
        else
        {
        	Log.v("Recording", "startRecording: no audio source found");

        }
        	
    }
    

 // got these sample rates to work.  Must be a supported samplerate for audiorecord to work
// 	static int RECORDER_SAMPLERATE = 8000;
// 	static int RECORDER_SAMPLERATE = 16000;
 	static int RECORDER_SAMPLERATE = 44100;
// size in bytes of recording buffer
 	static int bufferSize = 0;
 	static int bufferSamples;
 	static int REFLECTIONS = 128;
	static int INITIAL_DELAY = 4000;
 	static float REVERB_LEVEL = (float)0.1;
 	int[] reflectionTimes = new int[REFLECTIONS];
 	// level * 256 for each reflection
 	int[] reflectionLevels = new int[REFLECTIONS];
 	short[] reverbData = new short[RECORDER_SAMPLERATE * 2]; 
 	
 	boolean prevMonitorMic;
 	int feedbackSamples;
 	int feedbackCounter;
 	int FEEDBACK_TOTAL = RECORDER_SAMPLERATE;
 	int FEEDBACK_MAX_SAMPLES = RECORDER_SAMPLERATE / 4;
 	int FEEDBACK_MAX_LEVEL = 32767;
}









