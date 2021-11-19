/*
 * Audio Loopback
 * Copyright (C) 2013-2021  Adam Williams <broadcast at earthling dot net>
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

// Writes large buffers in the background

package com.AudioLoopback;


import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Semaphore;

public class RecordingThread  extends Thread  {
// size in bytes of writing buffer
	static int bufferSize = 0;
	static int MIN_BUFFER_SIZE = 131072;
	static int BUFFER_COUNT = 2;
	short[][] buffers;
	// current buffer being written to
	static int currentBuffer = 0;
	// number of bytes filled in each buffer
	static int[] filled = new int[BUFFER_COUNT];
	static FileOutputStream fd;
	static Semaphore writeLock = new Semaphore(0);
	boolean done = false;
	long totalBytes = 0;
	String path = "";

	RecordingThread(String path) {
// calculate the buffer size
		bufferSize = Recording.bufferSize;
		while(bufferSize < MIN_BUFFER_SIZE)
		{
			bufferSize += Recording.bufferSize;
		}
	
		buffers = new short[BUFFER_COUNT][];
		for(int i = 0; i < BUFFER_COUNT; i++)
		{
			buffers[i] = new short[bufferSize / 2];
		}
		Log.i("", "RecordingThread.RecordingThread path=" + path);
		this.path = path;

		try {
			fd = new FileOutputStream(path);
		} catch (IOException e) {
			e.printStackTrace();
		}

		writeHeader();

		if(Mane.activity != null)
		{
			Mane.activity.currentStatus = "Recording " + path + "\n0 bytes";
		}

		start();
	};

	public void writeHeader()
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
		header[offset++] = 1;
		header[offset++] = 0;
		// samplerate
		header[offset++] = (byte)(Settings.RECORDER_SAMPLERATE & 0xff);
		header[offset++] = (byte)((Settings.RECORDER_SAMPLERATE >> 8) & 0xff);
		header[offset++] = (byte)((Settings.RECORDER_SAMPLERATE >> 16) & 0xff);
		header[offset++] = (byte)((Settings.RECORDER_SAMPLERATE >> 24) & 0xff);
		// byterate
		int byterate = Settings.RECORDER_SAMPLERATE * 2 * 2;
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
		try {
			fd.getChannel().write(ByteBuffer.wrap(header));
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public void run()
	{
		while(!done)
		{
			try {
				writeLock.acquire();
			} catch (InterruptedException e) {
			}


			int prevBuffer = currentBuffer - 1;
			if(prevBuffer < 0)
			{
				prevBuffer = BUFFER_COUNT - 1;
			}

			short[] src = buffers[prevBuffer];
			ByteBuffer byteBuffer = ByteBuffer.allocate(filled[prevBuffer]);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			byteBuffer.asShortBuffer().put(src, 0, filled[prevBuffer] / 2);
			try {
				fd.getChannel().write(byteBuffer);
			} catch (IOException e) {
				e.printStackTrace();
			}

			totalBytes += src.length * 2;
			if(Mane.activity != null)
			{
				Mane.activity.currentStatus = "Recording " + path + "\n" +  Long.toString(totalBytes) + " bytes";
			}

			filled[prevBuffer] = 0;

		}
	}


	void write(short[] data)
	{
		// change buffers
		if(filled[currentBuffer] + Recording.bufferSize > bufferSize || done)
		{
			int nextBuffer = currentBuffer + 1;
			if(nextBuffer >= BUFFER_COUNT)
			{
				nextBuffer = 0;
			}

			// out of room
			if(filled[nextBuffer] > 0)
			{
				return;
			}

			currentBuffer = nextBuffer;
			writeLock.release();
		}

		System.arraycopy(data, 0, buffers[currentBuffer], filled[currentBuffer] / 2, data.length);
		filled[currentBuffer] += data.length * 2;
	}

	void stopRecording()
	{
		done = true;
		// kick the loop
		short[] data = new short[1];
		write(data);

		try {
			join();
		} catch (InterruptedException e) {


		}

		if(Mane.activity != null)
		{
			Mane.activity.currentStatus = "Done recording " + path + "\n" +  Long.toString(totalBytes) + " bytes";
		}

	}

	

};






