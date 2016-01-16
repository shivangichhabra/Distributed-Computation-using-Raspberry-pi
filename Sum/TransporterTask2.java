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
import java.util.concurrent.Semaphore;


public class TransporterTask2 implements Runnable
{

	SlavePi current;
	ArrayList<String> array = null;
	ArrayList<SlavePi> slaves = null;
	ArrayList<SlavePi> failed_slaves = null;
	ArrayList<Task2OutChunk> outputchunks;
	Semaphore semaphore_pi_list = null;
	Semaphore semaphore_output_list = null;
	
	public TransporterTask2(ArrayList<String> tmplist, ArrayList<Task2OutChunk> outputchunks, SlavePi slavePi, ArrayList<SlavePi> slaves,ArrayList<SlavePi> failed_slaves, Semaphore semaphore_pi_list, Semaphore semaphore_output_list) 
	{
		this.array = tmplist;
		this.outputchunks = outputchunks;
		current = slavePi;
		this.slaves = slaves;
		this.semaphore_pi_list = semaphore_pi_list;
		this.semaphore_output_list = semaphore_output_list;
		this.failed_slaves = failed_slaves;
	}

	@Override
	public void run() 
	{
		System.out.println("Sending data to pi");
		//run sort on slave
		try 
		{
			Task2OutChunk output = null;
			
			current.slaveoos.writeObject(array);
			current.slaveoos.flush();
			current.slaveoos.reset();
			
			System.out.println("Waiting for slave");
			
			while(output == null)
			{
				output = null;
				output = (Task2OutChunk) current.slaveois.readObject();
			}
			
			System.out.println("Got reply from slave " + output.count.size() + " " + output.sum.size());
			
			semaphore_pi_list.acquire();
			slaves.add(current);
			semaphore_pi_list.release();
			
			System.out.println("Added one slave to avilable slaves "+ slaves.size());
			
			semaphore_output_list.acquire();
			outputchunks.add(output);
			semaphore_output_list.release();
			array.clear();
			
		} 
		catch (Exception e) 
		{
			System.out.println("One of the slaves failed");
			e.printStackTrace();
			// add data
			try 
			{
				Task2OutChunk output = new Task2OutChunk();
				for(int i =0; i < array.size();i++)
				{
					output.add(array.get(i));
				}
				semaphore_output_list.acquire();
				outputchunks.add(output);
				semaphore_output_list.release();
				
				semaphore_pi_list.acquire();
				failed_slaves.add(current);
				semaphore_pi_list.release();
			} 
			catch (InterruptedException e1) 
			{
				e1.printStackTrace();
			}
			
		} 
	}

}
