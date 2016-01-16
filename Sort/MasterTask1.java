/*
 *  Project 2
 *  Professor : Peizhao Hu
 *  Course :  CSCI - 759
 *  
 *  @author1 : Ruturaj Hagawane
 *  @author2 : FNU Shivangi
 * 
 * This class communicates with slave machines
 * and store intermediate results in temporary file 
 * for better RAM utilization
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

public class MasterTask1 
{
	/*
	 * it merges results from slave and stores in temporary file
	 */
	private static File mergechunks(ArrayList<ArrayList<String>> sorted_strings_lists) 
	{

		final StringComparator comparator = new StringComparator();
		File newtmpfile = null;
		PriorityQueue<ArrayList<String>> pq = new PriorityQueue<ArrayList<String>>(11, 
				new Comparator<ArrayList<String>>() {
			public int compare(ArrayList<String> i, ArrayList<String> j) {
				return comparator.compare(i.get(0), j.get(0));
			}
		});

		// stores chunks in heap (priority queue)
		for(int i = 0; i < sorted_strings_lists.size(); i++)
		{
			pq.add(sorted_strings_lists.get(i));
		}

		try 
		{
			//creates temporary new file
			newtmpfile = File.createTempFile("sortInBatch", "flatfile");
			newtmpfile.deleteOnExit();
			BufferedWriter fbw = new BufferedWriter(new FileWriter(newtmpfile));	
			
			//get smallest element from chunk 
			// and put it in the temporary file
			while(pq.size()>0) 
			{
				ArrayList<String> bfb = pq.poll();
				String r = bfb.get(0);
				bfb.remove(0);
				if(false == bfb.isEmpty()) 
				{
					// add it back to queue
					pq.add(bfb); 
				}
				fbw.write(r);
				fbw.newLine();
			}
			fbw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		sorted_strings_lists.clear();
		return newtmpfile; 
	}

	/*
	 * merges all temporary file into single output file 
	 */
	public static void mergeSortedFiles(List<File> files, File outputfile) 
	{
		final StringComparator comparator = new StringComparator();
		PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>(20, 
				new Comparator<BinaryFileBuffer>() {
			public int compare(BinaryFileBuffer i, BinaryFileBuffer j) {
				return comparator.compare(i.peek(), j.peek());
			}
		});

		try 
		{
			for (int i=0; i < files.size(); i++) 
			{
				BinaryFileBuffer bfb;
				bfb = new BinaryFileBuffer(files.get(i));
				pq.add(bfb);
			}
			BufferedWriter fbw = new BufferedWriter(new FileWriter(outputfile));
			
			//get smallest element from temporary file
			// and put it in the output file
			while(pq.size()>0) 
			{
				BinaryFileBuffer bfb = pq.poll();
				String r = bfb.pop();
				fbw.write(r);
				fbw.newLine();
				
				//delete temporary files if empty
				if(bfb.empty()) 
				{
					bfb.fbr.close();
					bfb.originalfile.delete();
				} 
				else 
				{
					// add it back to the queue
					pq.add(bfb); 
				}
			}
			fbw.close();
			for(BinaryFileBuffer bfb : pq )
			{
				bfb.close();
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args) 
	{
		int chunksize = 10000;
		int blocksize = 10;
		Semaphore semaphore_pi_list = new Semaphore(1);
		Semaphore semaphore_output_list = new Semaphore(1);
		List<File> files = new ArrayList<File>();
		ArrayList<SlavePi>  avilable_slaves = new ArrayList<SlavePi>();
		ArrayList<SlavePi>  failed_slaves = new ArrayList<SlavePi>();
		ArrayList<String> ips = new ArrayList<String>();
		String slaveipfile = args[0];
		String inputfile = args[1];
		String outputfile = args[2];
		
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
				System.out.println("Connecting to: " + ips.get(i));
				Socket slave = new Socket(ips.get(i),1234);
				System.out.println("Connected to:  " + ips.get(i));
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
		ArrayList<ArrayList<String>> sorted_strings = new ArrayList<ArrayList<String>>();

		try
		{
			FileReader fileReader = new FileReader(inputfile);
			BufferedReader br = new BufferedReader(fileReader);
			String line = "";
			int total_slaves = avilable_slaves.size();

			System.out.println("Total available slaves: " + total_slaves);

			while(line != null) 
			{
				boolean didsomething = false;
				semaphore_pi_list.acquire();
				int avilable_pi = avilable_slaves.size();
				semaphore_pi_list.release();
				if(avilable_pi > 0)
				{
					System.out.println("Allocating task to the slave !");
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
						//System.out.println("Giving " + currentchunksize + " elements to slave");
						semaphore_pi_list.acquire();
						SlavePi worker = avilable_slaves.get(0);
						avilable_slaves.remove(0);
						semaphore_pi_list.release();
					
						TransporterTask1 t = new TransporterTask1(tmplist, sorted_strings, worker, avilable_slaves, failed_slaves, semaphore_pi_list, semaphore_output_list);
						Thread thread = new Thread(t);
						thread.start();

						//System.out.println("We have free slaves " + avilable_slaves.size() );

						didsomething = true;
					}
				}

				if(sorted_strings.size() >= blocksize)
				{
					//get 10 array, merge them and write them to a new file
					System.out.println("Writing 10 chunks to temporary file ...");
					semaphore_output_list.acquire();
					files.add(mergechunks(sorted_strings));
					sorted_strings.clear();
					semaphore_output_list.release();
					didsomething = true;
				}

				if(false == didsomething)
				{
					System.out.println("Master has nothing to do, sorting some data");
					long currentchunksize = 0;
					ArrayList<String> tmplist = new ArrayList<String>();

					while((currentchunksize < chunksize) && (   (line = br.readLine()) != null) )
					{
						tmplist.add(line);
						currentchunksize++;
					}
					
					if(currentchunksize > 0)
					{
						Collections.sort(tmplist,new StringComparator());
						semaphore_output_list.acquire();
						sorted_strings.add(tmplist);
						semaphore_output_list.release();
					}
				}
			}
			br.close();

			int slave_competed = avilable_slaves.size() + failed_slaves.size();
			System.out.println("Waiting for reply from " + (total_slaves - slave_competed) + " slaves");
			while(total_slaves != slave_competed)
			{
				// do nothing
				semaphore_pi_list.acquire();
				slave_competed = avilable_slaves.size() + failed_slaves.size();
				semaphore_pi_list.release();
			}
			
			System.out.println("All slaves completed task !");
			
			System.out.println("Writing remaining " + sorted_strings.size() + " chunks to temporary file...");
			files.add(mergechunks(sorted_strings));

			System.out.println("Writing all temporary files to output file...");
			mergeSortedFiles(files, new File(outputfile));

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
