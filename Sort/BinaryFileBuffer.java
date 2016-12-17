/*  
 *  This class is same as BufferReader class but 
 *  adds functionality to peek top value of file
 * 
 */

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

class BinaryFileBuffer  
{
	public static int BUFFERSIZE = 2048;
	public BufferedReader fbr;
	public File originalfile;
	private String cache;
	private boolean empty;


	//BinaryFileBuffer constructor to initialize 
	public BinaryFileBuffer(File f) throws IOException 
	{
		originalfile = f;
		fbr = new BufferedReader(new FileReader(f), BUFFERSIZE);
		reload();
	}

	//returns true if empty
	public boolean empty() 
	{
		return empty;
	}

	//reload function (loads value in cache)
	private void reload() throws IOException 
	{
		try 
		{
			cache = fbr.readLine();
			if(cache == null)
			{
				empty = true;
				cache = null;
			}
			else
			{
				empty = false;
			}
		} 
		catch(EOFException oef) 
		{
			empty = true;
			cache = null;
		}
	}

	//close function
	public void close() throws IOException 
	{
		fbr.close();
	}


	// peek function (returns top value but doesn't remove it
	public String peek() 
	{
		if(empty())
		{
			return null;
		}
		return cache.toString();
	}

	//pop function
	public String pop() throws IOException 
	{
		String answer = peek();
		reload();
		return answer;
	}
}
