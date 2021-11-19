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

import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.util.Log;

public class Playback  extends Thread  {
	Playback()
	{
		// minimum buffer size in bytes
		bufferSize = AudioTrack.getMinBufferSize(
				Settings.RECORDER_SAMPLERATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		Log.v("Playback", "Playback: bufferSize=" + bufferSize);
	}
	
	
	public void run() 
    {
    	
    	if(Settings.useMediaPlayer)
    	{
    	
//		    StreamProxy proxy = new StreamProxy();
//		    proxy.setData(Loopback.data);
//		    proxy.init();
//		    proxy.start();
//		    String proxyUrl = String.format("http://127.0.0.1:%d/%s", 
//		    		proxy.getPort(), 
//		    		"x.wav");
//		    MediaPlayer mediaPlayer = new MediaPlayer();
//		    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//		    try {
//				mediaPlayer.setDataSource(proxyUrl);
//				mediaPlayer.prepare(); // might take long! (for buffering, etc)
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		    mediaPlayer.start();
    	}
    	else
    	{
    		AudioTrack atrack = new AudioTrack(
//    				AudioManager.STREAM_VOICE_CALL, 
    				AudioManager.STREAM_MUSIC,
					Settings.RECORDER_SAMPLERATE,
//    				AudioFormat.CHANNEL_CONFIGURATION_STEREO, 
    				AudioFormat.CHANNEL_CONFIGURATION_MONO, 
    				AudioFormat.ENCODING_PCM_16BIT, 
    				bufferSize, 
    				AudioTrack.MODE_STREAM);
    		atrack.play();

    		byte wavData[] = new byte[bufferSize];

    		while(true)
    		{
    			Mane.data.waitSize(bufferSize);
    			Mane.data.read(wavData, 0, bufferSize);
    			atrack.write(wavData, 0, bufferSize);
    		}
    		
    	}
	    
	    
    	
    	try {
			Thread.sleep(100000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
	// minimum buffer size in bytes
    int bufferSize;
}
