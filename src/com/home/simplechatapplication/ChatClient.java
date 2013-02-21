package com.home.simplechatapplication;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import android.util.Log;

public class ChatClient {
    private String serverHostname = null;
    private int serverPort = 0;
    private DatagramSocket sock = null;
    private MessageParser parser = new MessageParser();
    public boolean isThreadStopped = false;
    private ChatController controller;
    private String key;
    private String iv;
    ObjectCrypter crypter;
    public static final String TAG = "SimpleClient";

    public ChatClient(String serverHostname, int serverPort,ChatController controller){
        this.serverHostname =  serverHostname;
        this.serverPort = serverPort;
        this.controller = controller;
        this.key = MainActivity.getConfiguration().getValueFromSection("UIConfiguration", "deskey", "0000000000");
        this.iv = MainActivity.getConfiguration().getValueFromSection("UIConfiguration", "desiv", "0000000000");
        Log.i(TAG,"DESKey = "+this.key + ", DESIv = "+this.iv);
        crypter = new ObjectCrypter(key.getBytes(),iv.getBytes());
    }
    private static Object resizeArray (Object oldArray, int newSize) {
 	   int oldSize = java.lang.reflect.Array.getLength(oldArray);
 	   Class elementType = oldArray.getClass().getComponentType();
 	   Object newArray = java.lang.reflect.Array.newInstance(
 	         elementType, newSize);
 	   int preserveLength = Math.min(oldSize, newSize);
 	   if (preserveLength > 0)
 	      System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
 	   return newArray; }
    public void readSomeMessages()
    {
        Log.i(TAG,"Client:About to start reading from socket.");
        byte[] buf = new byte[256];
        int bytes_read = 0;
        String message = "";
        DatagramPacket packet;
        while(true)
        {
        	packet = new DatagramPacket(new byte[65536],65536);
	        try {
	        	Log.i(TAG,"reading from socket:");
	            sock.receive(packet);
	            Log.i(TAG,"bytes_read = " + bytes_read);
	        }
	        catch (IOException e){
	            e.printStackTrace(System.err);
	        }
	        byte[] recData = (byte[])resizeArray(packet.getData(),packet.getLength());
	        Log.i(TAG,"Recieved data length: "+packet.getLength());
	        message = crypter.Decrypt(recData);
	        if(message.contains("sound")!=true) 
	        	Log.i(TAG,"Received data="+message);
			controller.handleMessage(message);
	        if(isThreadStopped)
	        	break;
        }
        Thread.currentThread().interrupt();
    }
    public int initiateSocket(String username)
    {
    	Log.i(TAG,"Client:Opening connection to "+serverHostname+" port "+serverPort);
        try {
            sock = new DatagramSocket();
        }
        catch (IOException e){
            e.printStackTrace(System.err);
            this.controller.getUiUpdater().makeToastNotification("Error hapened: check your connection");
            return -1;
        }
        this.isThreadStopped = false;
        return 1;
    }
    public void destroyConnection(String username)
    {
        tMessage tMes = new tMessage(username,"deregister","data");
        String mes = parser.createMessage(tMes); 
        this.sendSomeMessages(mes);
    }
    public void sendSomeMessages(String data) {
        
        Log.i(TAG,"Client:About to start reading/writing to/from socket.");
        byte[] dataByte = null;
        try {
        	InetAddress host = InetAddress.getByName(serverHostname);
        	dataByte = crypter.Encrypt(data);//data.getBytes("UTF-8");
        	Log.i(TAG,"Byte length = "+dataByte.length);
        	DatagramPacket packet = new DatagramPacket(dataByte,dataByte.length,host,serverPort);
        	sock.send(packet);
        }
        catch (IOException e){
        	e.printStackTrace(System.err);
        }
        Log.i(TAG,"Client:run: Sent "+dataByte.length);
        System.err.println("Exiting.");
    }
    
    public void Destroy()
    {
    	if(sock!=null)
    	{
    		sock.close();
    	}
    }
}
