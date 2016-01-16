/*
 *  Project 2
 *  Professor : Peizhao Hu
 *  Course :  CSCI - 759
 *  
 *  @author1 : Ruturaj Hagawane
 *  @author2 : FNU Shivangi
 * 
 * This class stores sum and count for characters
 * 
 */

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class Task2OutChunk implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HashMap<String,BigInteger> sum = new HashMap<String,BigInteger>(26);
	HashMap<String,Integer> count = new HashMap<String,Integer>(26);
	
	void add(String input)
	{
		String letter = input.substring(0,1).toUpperCase();
		Integer number = Integer.parseInt(input.substring(1));
		
		if(sum.containsKey(letter))
		{
			BigInteger previous_sum = sum.get(letter);
			Integer previous_count = count.get(letter);
			sum.put(letter, previous_sum.add(BigInteger.valueOf(number)));
			count.put(letter, previous_count + 1);
		}
		else
		{
			sum.put(letter, BigInteger.valueOf(number));
			count.put(letter, 1);
		}	
	}
	
	void add(Task2OutChunk another)
	{
		Iterator<Entry<String, BigInteger>> sum_it = another.sum.entrySet().iterator();
		Iterator<Entry<String, Integer>> count_it = another.count.entrySet().iterator();
		
		while(sum_it.hasNext())
		{
			Map.Entry<String,BigInteger> pair1 = sum_it.next();
			
			String letter = pair1.getKey();
			
			if(sum.containsKey(letter))
			{
				BigInteger previous_sum = sum.get(letter);
				sum.put(letter, previous_sum.add(pair1.getValue()));
				
			}
			else
			{
				sum.put(letter, pair1.getValue());
			}	
			
		}
		
		while(count_it.hasNext())
		{
			Map.Entry<String,Integer> pair2 = count_it.next();
			String letter = pair2.getKey();
			
			if(count.containsKey(letter))
			{
				Integer previous_count = count.get(letter);
				count.put(letter, previous_count + pair2.getValue());
			}
			else
			{
				count.put(letter, pair2.getValue());
			}
		}
		
	}
}
