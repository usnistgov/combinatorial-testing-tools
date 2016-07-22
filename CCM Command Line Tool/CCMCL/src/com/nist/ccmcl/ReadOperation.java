package com.nist.ccmcl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class ReadOperation implements Runnable {

	private static LinkedBlockingQueue<String> buffer = new LinkedBlockingQueue<String>();
	public static volatile boolean running;

	final protected void produce(String s) {
		buffer.add(s);
	}

	final static private void consume() throws InterruptedException {
		Thread load_data = new Thread(new Runnable() {

			@Override
			public void run() {
				running = true;
				try {
					Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);
					Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);
					String title = "NEW TEST CASES:";
					Files.write(Paths.get(Main.log_path), title.getBytes(), StandardOpenOption.APPEND);
					Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);
					Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						try {
							for(int i = 0; i < 5; i++)

							running = false;
							for(int i = 0; i < 5; i++){

								if(Main.tway_objects[i] != null){
									try{
										Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);
										Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);
										String title = "\n\nNew " + (i+2) + "-way invalid combinations: \n";
										Files.write(Paths.get(Main.log_path), title.getBytes(), StandardOpenOption.APPEND);
										Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);
										Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);
										for(String[][] str : Main.tway_objects[i].get_InvalidComb()){
											boolean newinvalid = false;
											String invalidCombString = "";
											for (int z = 0; z < str.length; z++) {
												String inval = str[z][0] + " = " + str[z][1] + " ; ";
												invalidCombString += inval;
											}
											if(!Main.initial_invalid.containsKey(invalidCombString)){
												Files.write(Paths.get(Main.log_path), invalidCombString.getBytes(), StandardOpenOption.APPEND);
												newinvalid = true;
											}
											if(newinvalid)
												Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);
											
											
										}
										Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);
										Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);

										
									}catch(IOException e){
										
									}
								}
							}
							
							
							
							
							
							
							
							Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(),
									StandardOpenOption.APPEND);
							Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(),
									StandardOpenOption.APPEND);
							String title1 = "NEW T-WAY COVERAGE RESULTS:";
							Files.write(Paths.get(Main.log_path), title1.getBytes(),
									StandardOpenOption.APPEND);
							Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(),
									StandardOpenOption.APPEND);
							Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(),
									StandardOpenOption.APPEND);

							for (int i = 0; i < 5; i++) {
								if (Main.tway_objects[i] != null) {

									String title = "";
									switch (i) {

									case 0:
										title = Main.real_time_cmd_results[0];
										break;
									case 1:
										title = Main.real_time_cmd_results[1];
										break;
									case 2:
										title = Main.real_time_cmd_results[2];
										break;
									case 3:
										title = Main.real_time_cmd_results[3];
										break;
									case 4:
										title = Main.real_time_cmd_results[4];
										break;
									}
									Files.write(Paths.get(Main.log_path), title.getBytes(), StandardOpenOption.APPEND);
									Files.write(Paths.get(Main.log_path),
											System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);

								}
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				});


				while (true) {
					String input;
					try {
						input = buffer.take();
						int position = Main.nrows;
						if (Main.nrows < 1) {
							Main.infile = new String[1];
							Main.test = new int[1][];
							Main.max_array_size = 0;
						}
						if (position == Main.max_array_size) {
							
							for(int i = 0; i < 5; i++)
								while(Main.tway_threads[i] != 0 && Main.tway_objects[i] != null){
									Thread.sleep(1000);
								}
						
							if (Main.max_array_size < 1000) {
								Main.max_array_size = 1000;
								Main.test = new int[Main.max_array_size][];
								Main.infile = new String[Main.max_array_size];

							} else {
								Main.test = new int[Main.test.length][];
								Main.infile = new String[Main.infile.length];
							}
							position = 0;
							System.gc();

						}

						Main.infile[position] = input;
						Main.test[position] = new int[Main.ncols];
						Main.nrows = position + 1;
						int status = Main.setupFile(position);
						if (status == -1) {
							continue;
						} else if (status != 0) {
							System.out.println("Error: Something went wrong.\nExiting...\n");
							System.exit(status);
						}

						while (java.lang.Thread.activeCount() > Main.threadmax) {
							System.out.println("Exceeded maximum threads specified... Waiting...");
							Thread.sleep(1000);
						}

						for (int i = 0; i < 5; i++) {
							if (Main.tway_objects[i] != null) {
								switch (i) {
								case 0:
									Main.Tway("2way");
									Main.tway_threads[0]++;
									break;
								case 1:
									Main.Tway("3way");
									Main.tway_threads[1]++;
									break;
								case 2:
									Main.Tway("4way");
									Main.tway_threads[2]++;
									break;
								case 3:
									Main.Tway("5way");
									Main.tway_threads[3]++;
									break;
								case 4:
									Main.Tway("6way");
									Main.tway_threads[4]++;
									break;
								}
							}
						}
						if(Main.logRT){
							if(running){
								try {
									Files.write(Paths.get(Main.log_path), input.getBytes(), StandardOpenOption.APPEND);
									Files.write(Paths.get(Main.log_path), System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
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
			running = true;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
