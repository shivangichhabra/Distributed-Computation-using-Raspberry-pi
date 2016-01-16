/*
 *  Project 2
 *  Professor : Peizhao Hu
 *  Course :  CSCI - 759
 *  
 *  @author1 : Ruturaj Hagawane
 *  @author2 : FNU Shivangi
 * 
 * Communicates with slave machines and 
 * allocates task to the slave
 * 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class MasterTask2 
{	
	public static void main(String[] args) 
	{
		int chunksize = 10000;
		Semaphore semaphore_pi_list = new Semaphore(1);
		Semaphore semaphore_output_list = new Semaphore(1);
		ArrayList<SlavePi>  avilable_slaves = new ArrayList<SlavePi>();
		ArrayList<SlavePi>  failed_slaves = new ArrayList<SlavePi>();
		ArrayList<String> ips = new ArrayList<String>();
		String slaveipfile = args[0];
		String inputfile = args[1];
		String outputfile = args[2];
		Task2OutChunk output = new Task2OutChunk();

		// read all ips from file
		try
		{
			FileReader fileReader = new FileReader(slaveipfile);
			BufferedReader br = new BufferedReader(fileReader);
			String line;
			while ((line = br.readLine()) != null) 
			{
				ips.add(line);
			}
			br.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		//make connection to all clients	
		ObjectInputStream ois;
		ObjectOutputStream oos;
		for(int i =0;i<ips.size();i++)
		{
			try 
			{
				System.out.println("connecting to " + ips.get(i));
				Socket slave = new Socket(ips.get(i),1234);
				System.out.println("connected to " + ips.get(i));
				ois = new ObjectInputStream(slave.getInputStream());
				oos = new ObjectOutputStream(slave.getOutputStream());
				SlavePi pi = new SlavePi(slave, oos, ois);
				avilable_slaves.add(pi);
			} 
			catch (UnknownHostException e1) 
			{
				e1.printStackTrace();
			} 
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
		}

		// read data, create packets then send one by one through transformer thread
		ArrayList<Task2OutChunk> outputchunks = new ArrayList<Task2OutChunk>();

		try
		{
			FileReader fileReader = new FileReader(inputfile);
			BufferedReader br = new BufferedReader(fileReader);
			String line = "";
			int total_slaves = avilable_slaves.size();

			System.out.println("Total pie avilable " + total_slaves);

			while(line != null) 
			{
				boolean didsomething = false;
				semaphore_pi_list.acquire();
				int avilable_pi = avilable_slaves.size();
				semaphore_pi_list.release();
				if(avilable_pi > 0)
				{
					System.out.println("Slave is avilable");
					long currentchunksize = 0;
					ArrayList<String> tmplist = new ArrayList<String>();

					while((currentchunksize < chunksize) && ((line = br.readLine()) != null) )
					{
						tmplist.add(line);
						currentchunksize++;
					}
					//CREATE NEW TRANSPORTER THREAD AND SORT
					if(currentchunksize > 0)
					{
						System.out.println("Giving " + currentchunksize + " elements to slave");
						semaphore_pi_list.acquire();
						SlavePi worker = avilable_slaves.get(0);
						avilable_slaves.remove(0);
						semaphore_pi_list.release();

						TransporterTask2 t = new TransporterTask2(tmplist, outputchunks, worker, avilable_slaves, failed_slaves, semaphore_pi_list, semaphore_output_list);
						Thread thread = new Thread(t);
						thread.start();

						System.out.println("We have "+avilable_slaves.size() +"remaining free slaves " );

						didsomething = true;
					}
				}


				if(outputchunks.size() >= 0)
				{
					semaphore_output_list.acquire();
					for(int i=0; i < outputchunks.size(); i++)
					{
						output.add(outputchunks.get(i));
					}
					outputchunks.clear();
					semaphore_output_list.release();
				}

				if(false == didsomething)
				{
					System.out.println("Master has nothing to do, working on some data");
					long currentchunksize = 0;
					
					while((currentchunksize < chunksize) && (   (line = br.readLine()) != null) )
					{
						output.add(line);
						currentchunksize++;
					}
				}
			}
			br.close();

			int slave_competed = avilable_slaves.size() + failed_slaves.size();
			System.out.println("Waiting for few slaves" + (total_slaves - avilable_slaves.size()));
			while(total_slaves != slave_competed)
			{
				// do nothing
				semaphore_pi_list.acquire();
				slave_competed = avilable_slaves.size() + failed_slaves.size();
				semaphore_pi_list.release();
			}

			System.out.println("Done waiting");

			System.out.println("summing remaining chunks " + outputchunks.size());
			for(int i=0; i < outputchunks.size(); i++)
			{
				output.add(outputchunks.get(i));
			}

			System.out.println("Writing to output file");
			File output_file = new File(outputfile);
			BufferedWriter file_out = new BufferedWriter(new FileWriter(output_file));
			
			
			for(char i = 65; i < 91; i++)
			{
				Integer count_value = output.count.get(Character.toString(i));
				
				if(count_value != null && count_value > 0)
				{
					//writing output to file
					file_out.write(i + " " + count_value + " " + output.sum.get(Character.toString(i)) + "\n");
				}
			}
			file_out.close();

		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
}
