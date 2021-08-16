import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Client extends Thread {
	Integer port;
	String ip; 
	Socket socketClient;
	
	ObjectOutputStream out;
	ObjectInputStream in;
	
	private Consumer<Serializable> callback;
	
	// Game Info
	int lengthOfWord;
	char currentGuess;
	ArrayList<Integer> locationInWord;
	
	Client(Consumer<Serializable> call){
		callback = call;
		locationInWord = new ArrayList<Integer>();
	}
	
	Client() {
        port = 5555;
        ip = "127.1.1.1";
        lengthOfWord = 10;
        currentGuess = 'a';
    }
	
	public void run() {
		try {
			socketClient= new Socket(ip,port);
		    out = new ObjectOutputStream(socketClient.getOutputStream());
		    in = new ObjectInputStream(socketClient.getInputStream());
		    socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) {System.out.println("Something wrong with input/outputStream exception");}

		while(true) {
			try {
				// Package Catch
				WordGuessInfo b = (WordGuessInfo)in.readObject();
				
				// Send to GUI
				callback.accept(b);
				
			}//end try
			
			catch(Exception e) {
				System.out.println("Except: Could not readObject from Instream!");
				break;
			}
			
		}// endWhile true
	
    }
	
	
	// Function to send package
	public void send(WordGuessInfo data) {
		try {
			// Send WordGuessInfo to Server
			out.writeObject(data);
			out.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	// Getters and Setters
	public void setPort(Integer data) {
		port = data;
	}
	
	public Integer getPort() {
		return port;
	}
	
	public void setIP(String data) {
		ip = data;
	}
	
	public String getIP() {
		return ip;
	}
	
	

	


}
