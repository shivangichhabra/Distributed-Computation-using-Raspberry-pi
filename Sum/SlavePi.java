/*
 *  Project 2
 *  Professor : Peizhao Hu
 *  Course :  CSCI - 759
 *  
 *  @author1 : Ruturaj Hagawane
 *  @author2 : FNU Shivangi
 * 
 * This class stores all information 
 * related to slaves
 * 
 */

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class SlavePi 
{
	
	ObjectOutputStream slaveoos = null;
	ObjectInputStream slaveois = null;
	Socket socket = null;
	
	public SlavePi(Socket socket,ObjectOutputStream objectOutputStream, ObjectInputStream objectinputStream) 
	{
		slaveois = objectinputStream;
		slaveoos = objectOutputStream;
		this.socket = socket;
	}
}
