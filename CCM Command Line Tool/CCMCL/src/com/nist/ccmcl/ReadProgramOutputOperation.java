package com.nist.ccmcl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadProgramOutputOperation extends ReadOperation {
	

	@Override
	public void readData() {
		Runtime rt = Runtime.getRuntime();
		String[] commands = null;
		if(Main.rtExPath.endsWith(".jar")){
			commands = new String[Main.rtExArgs.size() + 3];
			commands[0] = "java";
			commands[1] = "-jar";
			commands[2] = Main.rtExPath;
			File f = new File(commands[2]);
			if(!(f.exists() && !f.isDirectory())) { 
			    //file doesn't exist
				System.out.println("No executable program found at: " + commands[2]);
				System.exit(0);
			}
			for(int i = 3; i < commands.length; i++){
				commands[i] = Main.rtExArgs.get(i - 3);
			}
		}else if(Main.rtExPath.endsWith(".exe")){
			commands = new String[Main.rtExArgs.size() + 1];
			commands[0] = Main.rtExPath;
			File f = new File(commands[0]);
			if(!(f.exists() && !f.isDirectory())) { 
			    //file doesn't exist
				System.out.println("No executable program found at: " + commands[0]);
				System.exit(0);
			}
			for(int i = 1; i < commands.length; i++){
				commands[i] = Main.rtExArgs.get(i - 1);
			}
		}
		



		Process proc;

		try {
			proc = rt.exec(commands);
			Runtime.getRuntime().addShutdownHook(new Thread(){
				@Override
				public void run(){
					System.gc();
					proc.destroy();
				}
			});
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String s = "";
			while (true) {
			    s = stdInput.readLine();
			    if(s == null)
			    	continue;
			    else{
					if(s.replaceAll("\\s","").trim().split(",").length != TestData.get_columns()){
						System.out.println("Incorrect number of parameters...");
						System.out.print(s + " - Expected columns = " + TestData.get_columns());
						continue;
					}
					produce(s);
			    }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
