package com.nist.ccmcl;

import java.io.BufferedReader;
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
			for(int i = 3; i < commands.length; i++){
				commands[i] = Main.rtExArgs.get(i - 3);
			}
		}else if(Main.rtExPath.endsWith(".exe")){
			commands = new String[Main.rtExArgs.size() + 1];
			commands[0] = Main.rtExPath;
			for(int i = 1; i < commands.length; i++){
				commands[i] = Main.rtExArgs.get(i - 1);
			}
		}


		Process proc;
		try {
			proc = rt.exec(commands);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String s = "";
			while (true) {
			    s = stdInput.readLine();
			    if(s == null)
			    	continue;
			    else{
					if(s.replaceAll("\\s","").trim().split(",").length != TestData.get_columns()){
						System.out.println("Incorrect number of parameters...");
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
