package com.nist.ccmcl;

import java.util.Scanner;

public class ReadStandardInputOperation extends ReadOperation{

	@Override
	public void readData() {
		// TODO Auto-generated method stub
		Scanner input_scanner = new Scanner(System.in);
		while(true){
			String inputLine = input_scanner.nextLine();
			if(inputLine.replaceAll("\\s","").trim().split(",").length != TestData.get_columns()){
				System.out.println("Incorrect number of parameters...");
				continue;
			}
			produce(inputLine);
		}
	}

}
