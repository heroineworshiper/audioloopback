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

package com.AudioLoopback;

import java.util.concurrent.Semaphore;

public class Fifo {
	Fifo(int size)
	{
		data = new byte[size];
		allocated = size;
	}
	
	public void waitSize(int wantSize)
	{
		while(getSize() < wantSize)
		{
			waitFifo();
		}
	}

	public void waitFifo()
	{
		try {
			data_ready.acquire();
		} catch (InterruptedException e) {
		}
	}
	
	public int getSize()
	{
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int result = size;
		lock.release();
		
		return result;
	}
	
	void write(byte[] inData, int inOffset, int inSize)
	{

		try {
			lock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		while(inSize > 0)
		{
			int fragment = inSize;
	// Wrap write pointer
			if(in_position + fragment > allocated)
			{
				fragment = allocated - in_position;
			}


			if(size + fragment > allocated)
			{
	// Advance the read pointer until enough room is available.
	// Must do this for audio.
				int difference = size + fragment - allocated;
				out_position += difference;
	// Wrap it
				out_position %= allocated;
				size -= difference;
			}

			if(fragment <= 0) break;

			System.arraycopy(inData, inOffset, data, in_position, fragment);

			in_position += fragment;
			size += fragment;
			inOffset += fragment;
			inSize -= fragment;
			if(in_position >= allocated)
				in_position = 0;
		}
		
	//printf("write_fifo %d %d\n", __LINE__, fifo->size);

		lock.release();
		
		data_ready.release();
	}

	
	void read(byte[] dst, int outOffset, int outSize)
	{
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		while(outSize > 0)
		{
			int fragment = outSize;
			if(fragment + out_position > allocated)
				fragment = allocated - out_position;
			
			System.arraycopy(data, out_position, dst, outOffset, fragment);

			out_position += fragment;
			size -= fragment;
			outOffset += fragment;
			outSize -= fragment;
			if(out_position >= allocated)
				out_position = 0;
		}
		
		lock.release();
	}

	
	

	byte[] data;
	int size;
	int allocated;
// Next position to write
	int in_position;
// Next position to read
	int out_position;
	Semaphore lock = new Semaphore(1);
	Semaphore data_ready = new Semaphore(0);

	
	
}
