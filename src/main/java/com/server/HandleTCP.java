package com.server;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

class HandleTCP extends Thread{
	private final static int BYTES_IN_MEGABYTES = 1048576;
	private Socket client;
	private final int whichPort;
	private BufferedReader is;
	private DataOutputStream os;
	private InputStream ins;

	public HandleTCP(Socket client, int whichPort){
		this.client = client;
		this.whichPort = whichPort;
		
		try {
            //Build the necessary streams from the client. 
            ins = client.getInputStream();
            is = new BufferedReader(new InputStreamReader
            (ins));
            os = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {
            System.out.println("Exception: "+e.getMessage());
        }
		
		this.start();
	}

	public void run(){
		try{
			if (whichPort == 8080)
				blackHole(ins);
			else if (whichPort == 8000)
				dataDump(os);
			client.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	private void dataDump(DataOutputStream out) throws IOException {
		double currentBytes = 0.0;
        int i = 0;
        System.out.println("Data dumping...");
        try {
            //make a byte buffer of the proper size, fill it with a random byte array.
            ByteBuffer buf = ByteBuffer.allocate(BYTES_IN_MEGABYTES);
            byte[] b = new byte[BYTES_IN_MEGABYTES];
            new Random().nextBytes(b);
            buf.put(b);
            //don't forget to flip it for writing!
            buf.flip();
            //Keep writing the same buffer over and over until we've written 100 MB
            while (currentBytes <= (BYTES_IN_MEGABYTES * 100)){
                out.write(buf.array(), 0, BYTES_IN_MEGABYTES);
                buf.clear();
                buf.put(b);
                buf.flip();
                i++;
                currentBytes = (i * BYTES_IN_MEGABYTES);
                System.out.println("current iteration:" +  i);
            }
            out.writeBytes("Content-Type: random/bytes\r\n\r\n");
        } catch (Exception e) {
            //write a standard error message out if something goes wrong, IE: IOException
            //or the client closes the connection, etc. etc. 
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
	}
	private void blackHole(InputStream ins){
		int i = 0;
        System.out.println("Waiting for awesome data");
        //make the byteBuffer and back it with a large enough byte array.
        byte[] b = new byte[BYTES_IN_MEGABYTES];
        ByteBuffer buf = ByteBuffer.allocate(BYTES_IN_MEGABYTES);
        try {
            //while the input stream has something available, keep filling, emptying and re-filling. 
            while (ins.read() != -1){
                System.out.println("reading!");
                ins.read(b);
                buf.put(b);
                Arrays.fill(b, (byte)0);
                buf.clear();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        finally{
            try{
                ins.close();
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
	}
}