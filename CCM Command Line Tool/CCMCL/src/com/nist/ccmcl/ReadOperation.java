package com.nist.ccmcl;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class ReadOperation implements Runnable{
	
	private static LinkedBlockingQueue<String> buffer = new LinkedBlockingQueue<String>();
	
	final protected void produce(String s){
		buffer.add(s);
	}
	
	final static private void consume() throws InterruptedException{
		Thread load_data = new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true){
					String input;
					try {
						input = buffer.take();
						int position = TestData.get_rows();
						if(Main.infile.length <= TestData.get_rows()){
							Main.infile = Arrays.copyOf(Main.infile, Main.infile.length*2);
							Main.test = Arrays.copyOf(Main.test, Main.test.length*2);
						}
						
							Main.infile[position] = input;
							Main.test[position] = new int[TestData.get_columns()];
							TestData.set_rows(position + 1);
							int status = Main.setupFile(position);
							if(status == -1){
								continue;
							}else if(status != 0){
								System.out.println("Error: Something went wrong.\nExiting...\n");
								System.exit(status);
							}
							position++;
							for(int i = 0; i < 5; i++){
								if(Main.tway_objects[i] != null){
									switch(i){
									case 0:
										Main.Tway("2way");
										break;
									case 1:
										Main.Tway("3way");
										break;
									case 2:
										Main.Tway("4way");
										break;
									case 3:
										Main.Tway("5way");
										break;
									case 4:
										Main.Tway("6way");
										break;
									}
								}
							}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}


			}
			
		});
		load_data.start();

	}
	
	public abstract void readData();

	@Override
	public void run() {
		try {
			consume();
			readData();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	



	
}
