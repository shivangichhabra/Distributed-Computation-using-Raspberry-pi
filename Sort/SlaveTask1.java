/*
 *  Project 2
 *  Professor : Peizhao Hu
 *  Course :  CSCI - 759
 *  
 *  @author1 : Ruturaj Hagawane
 *  @author2 : FNU Shivangi
 *  
 *  This class performs the allocated task and 
 *  sends output to the master
 * 
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

public class SlaveTask1
{
	public static void main(String[] args) 
	{
		ServerSocket listner = null;
		
		try
		{
			listner = new ServerSocket(1234);
			
			System.out.println("Slave is running...");

			Socket client = listner.accept();
			ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(client.getInputStream());
			
			System.out.println("Connection accepeted from " + client.getInetAddress().toString().split("/")[1]);
			
			while(true)
			{
				// get data
				@SuppressWarnings("unchecked")
				ArrayList<String> array = (ArrayList<String>) in.readObject();
				
				if(array != null)
				{
					System.out.println("Got chunk !");
					// sort data
					Collections.sort(array,new StringComparator());
					
					// reply
					out.writeObject(array);
					out.flush();
					out.reset();
					System.out.println("Returning sorted chunk...");
				}
			}			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try 
			{
				listner.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}

}
