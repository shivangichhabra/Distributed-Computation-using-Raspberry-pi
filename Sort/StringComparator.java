/*
 *  Project 2
 *  Professor : Peizhao Hu
 *  Course :  CSCI - 759
 *  
 *  @author1 : Ruturaj Hagawane
 *  @author2 : FNU Shivangi
 * 
 * Comparator class for string comparison according to requirements
 * 
 */

import java.util.Comparator;

public class StringComparator implements Comparator<String>
{
	public int compare(String o1, String o2) 
	{
		if(o1.length() == o2.length())
		{
			return o1.compareTo(o2);
		}
		else
		{
			if(o1.charAt(0) == o2.charAt(0))
			{
				return o1.length()-o2.length();
			}
			else
			{
				return o1.compareTo(o2);
			}
		}
	}
}