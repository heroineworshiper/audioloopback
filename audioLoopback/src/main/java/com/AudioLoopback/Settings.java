/*
 * Audio Loopback
 * Copyright (C) 2017  Adam Williams <broadcast at earthling dot net>
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
import android.content.SharedPreferences;
import android.app.Activity;


public class Settings
{
    static boolean useMediaPlayer = false;
    static boolean monitorMic = false;
    static boolean doReverb = false;
    static float speakerGain = (float)1.0;
    static boolean usePopup = true;
    static int x = StandOutLayoutParams.CENTER;
    static int y = StandOutLayoutParams.CENTER;

	// got these sample rates to work.  Must be a supported samplerate for audiorecord to work
// 	static int RECORDER_SAMPLERATE = 8000;
// 	static int RECORDER_SAMPLERATE = 16000;
	static int RECORDER_SAMPLERATE = 44100;
	static int MAX_SOURCES = 16;

	static String RECORDING_DIR = "/sdcard/recordings/";
	static String RECORDING_PREFIX = "recording";
	static String currentRecording = "";
	// is currently recording to the currentRecording
	static boolean isRecording = false;
	// want to start recording to the currentRecording
	static boolean wantStart = false;
	// want to stop recording
	static boolean wantStop = false;

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
	
	

};





