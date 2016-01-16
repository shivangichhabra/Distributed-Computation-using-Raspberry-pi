/*
 *  Project 2
 *  Professor : Peizhao Hu
 *  Course :  CSCI - 759
 *  
 *  @author1 : Ruturaj Hagawane
 *  @author2 : FNU Shivangi
 * 
 * This class creates a thread which sends allocated 
 * task to the slave  
 * 
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Semaphore;


public class TransporterTask1 implements Runnable
{

	SlavePi current;
	ArrayList<String> array = null;
	ArrayList<SlavePi> slaves = null;
	ArrayList<SlavePi>  failed_slaves;
	ArrayList<ArrayList<String>> sorted_strings;
	Semaphore semaphore_pi_list = null;
	Semaphore semaphore_output_list = null;

	public TransporterTask1(ArrayList<String> tmplist, ArrayList<ArrayList<String>> sorted_strings, SlavePi slavePi, ArrayList<SlavePi> slaves,ArrayList<SlavePi> failed_slaves, Semaphore semaphore_pi_list, Semaphore semaphore_output_list) 
	{
		this.array = tmplist;
		this.sorted_strings = sorted_strings;
		current = slavePi;
		this.slaves = slaves;
		this.semaphore_pi_list = semaphore_pi_list;
		this.semaphore_output_list = semaphore_output_list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() 
	{
		System.out.println("Sending data to pi");
		//run sort on slave
		try 
		{
			current.slaveoos.writeObject(array);
			current.slaveoos.flush();
			current.slaveoos.reset();

			System.out.println("Waiting for slave.... ");

			while(true)
			{
				ArrayList<String> array_in = null;
				array_in = (ArrayList<String>) current.slaveois.readObject();
				if(array_in != null)
				{
					array = array_in;
					break;
				}
			}

			System.out.println("Got reply from slave " + array.size());

			semaphore_pi_list.acquire();
			slaves.add(current);
			semaphore_pi_list.release();

			System.out.println("Added one slave to avilable slaves "+ slaves.size());

			semaphore_output_list.acquire();
			sorted_strings.add(array);
			semaphore_output_list.release();

		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			try 
			{	
				System.out.println("slave failed");
				Collections.sort(array,new StringComparator());

				semaphore_pi_list.acquire();
				failed_slaves.add(current);
				semaphore_pi_list.release();

				System.out.println("Added one slave to avilable slaves "+ slaves.size());

				semaphore_output_list.acquire();
				sorted_strings.add(array);
				semaphore_output_list.release();
			} 
			catch (InterruptedException e1) 
			{
				e1.printStackTrace();
			}
		} 
	}

}
