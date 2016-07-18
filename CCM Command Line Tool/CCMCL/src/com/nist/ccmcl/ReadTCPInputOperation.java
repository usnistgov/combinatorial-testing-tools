package com.nist.ccmcl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ReadTCPInputOperation extends ReadOperation {

	@Override
	public void readData() {
		try {
			String inputLine = "";
			ServerSocket initial = new ServerSocket(Main.tcp_port);
			while(true){
				Socket connection = initial.accept();
				BufferedReader dataStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				inputLine = dataStream.readLine();
				System.out.println("Received: " + inputLine);
			    if(inputLine == null)
			    	continue;
			    else{
					if(inputLine.replaceAll("\\s","").trim().split(",").length != TestData.get_columns()){
						System.out.println("Incorrect number of parameters...");
						System.out.print(inputLine + " - Expected columns = " + TestData.get_columns());
						continue;
					}
					produce(inputLine);
			    }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
