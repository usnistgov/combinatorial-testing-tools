package com.nist.ccmcl;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main
{

    public final static int                NBINS      = 20;
    // boundaries specified
    public static final XYSeriesCollection chart_data = new XYSeriesCollection();
    public static final XYSeriesCollection bars       = new XYSeriesCollection();
    public static          List<String>               usedParams;
    public static          HashMap<String, Parameter> pars;
    public volatile static String[]                   infile;
    public static          String[]                   values;
    public volatile static int[][]                    test;
    public static          int[][]                    hm_colors2;
    public static          int[]                      nvals;
    public static          Boolean[]                  valSet;
    public static int prev_ncols = 0;
    public static double[][]       bnd;
    public static Boolean[]        rng;
    public static Boolean[]        grp;
    public static Object[][]       group;
    public static String[][]       map;
    // public String[][] SetTest;
    public static List<String[][]> aInvalidComb;
    public static List<String[][]> aInvalidNotIn;
    public static double           TotCov2way; // total coverage for 2-way combinations
    public static double           TotCov3way; // total coverage for 3-way combinations
    public static double           TotCov4way; // total coverage for 4-way combinations
    public static double           TotCov5way; // total coverage for 4-way combinations
    public static double           TotCov6way;
    public static int nbnds = 4;
    public static Boolean[] boundariesSet; // 1 = all parameters have had
    public static FileWriter     fwRandomFile  = null;
    public static BufferedWriter bwRandomTests = null;
    public static JFreeChart chart;
    public static JFreeChart chartcolumn;
    public static JFrame  frame                       = new JFrame("CCM");
    public static JPanel  jPanel                      = new JPanel();
    public static JLabel  lblStepChart                = new JLabel("");
    public static JLabel  lblColumnChart              = new JLabel("");
    public static JLabel  lblPointChart               = new JLabel("");
    public static JPanel  pointChartPanel             = new JPanel();
    public static boolean parallel                    = false;
    public static boolean heatmap                     = false;
    public static boolean stepchart                   = false;
    public static boolean barchart                    = false;
    public static boolean generateMissing             = false;
    public static boolean appendTests                 = false;
    public static boolean generateRandom              = false;
    public static String  random_path                 = "";
    public static int     numberOfRandom              = 0;
    public static String  ACTSpath                    = "";
    public static int     minCov                      = 100;
    public static String  tests_input_file_path       = "";
    public static String  missingCombinationsFilePath = "";
    public static String  constraints_path            = "";
    public static String ext;
    public static boolean[] initial_complete = new boolean[5];
    public static double[]  initial_coverage = new double[5];

    public static boolean            ACTSfilePresent = false;
    public static boolean            paramNames      = false;
    public static int                nrows           = 0;
    public static int                ncols           = 0;
    public static List<Parameter>    parameters      = new ArrayList<Parameter>();
    public static List<meConstraint> constraints     = new ArrayList<meConstraint>();

    public static boolean display_progress = false;

    public static int real_time_buffer_size = 0;

    public static int[] invalidCombIndex = new int[5];

    public static int[] tway_threads = new int[5];

    public static int tcp_port = 0;

    public static String[] real_time_cmd_results = new String[5];
    public static boolean  logRT                 = false;
    public static String   log_path              = "";

    public static HashMap<String, Boolean> initial_invalid = new HashMap<String, Boolean>();
    /*
     * Real time arguments
	 */
    public static char                     rtMode          = 'k';
    public static String                   rtExPath        = "";
    public static Vector<String>           rtExArgs        = new Vector<String>();

    public static volatile int[] progress = new int[5];

    public static int max_array_size = 0;
    public static int threadmax      = 500;

    // public static boolean[] tway_initialized = new boolean[5];
    public static Tway                        tway_objects[] = new Tway[5];
    public static boolean                     mode_realtime  = false;
    public static int                         tway_max       = 0;
    // can change this or user define it as cmd parameter
    public static int                         nmapMax        = 50;
    public static boolean                     all_complete   = false;
    public static String[]                    report         = new String[5];
    public        LinkedBlockingQueue<String> buffer_queue   = new LinkedBlockingQueue<String>();

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException
    {
        String tway_values[] = new String[5];

        for (int i = 0; i < 5; i++)
        {
            tway_threads[i] = 1;
            real_time_cmd_results[i] = ("Total " + String.valueOf((i + 2)) + "-way coverage: "
                                        + String.format("%.5f", 0.0f));
            initial_complete[i] = false;
            initial_coverage[i] = 0.0f;
            invalidCombIndex[i] = 0;
        }

		/*
         * Parse the command line arguments
		 */

        int arg_count = 0;
        boolean skip = false;
        for (String s : args)
        {
            if (s.equals("--help"))
            {
                // Bring up the menu to display possible input parameters.
                System.out.println(
                        "\n\n******************************************************************************\n"
                                  );
                System.out.println("                CCMCL: COMBINATORIAL COVERAGE MEASUREMENT TOOL                \n");
                System.out
                        .println("                             version COMMAND LINE                             \n\n");
                System.out
                        .println("                 National Institute of Standards & Technology                 \n\n");
                System.out
                        .println("******************************************************************************\n\n");

                System.out.println("USAGE: java -jar ccmcl.jar [param1] [param2] ...\n");
                System.out.println("EXAMPLE: java -jar ccmcl.jar -I input.csv -T 2,3,4,5,6 -P");
                System.out.println("EXAMPLE: java -jar ccmcl.jar -I i.csv -T 2 -A a.txt -G -m 50 -o m.txt -a -B -H");
                System.out.println("EXAMPLE: java -jar ccmcl.jar -A actsfile.txt -r -n 1000 -f random.txt -S -T 2");
                System.out.println("EXAMPLE: java -jar ccmcl.jar -A actsfile.txt -R -k -S -T 2,3,4");
                System.out.println("EXAMPLE: java -jar ccmcl.jar -A actsfile.txt -R -k gen_tests.exe -S -T 2,3,4");
                System.out.println("EXAMPLE: java -jar ccmcl.jar -A acts.xml -R -e gen.exe -B -T 2 -tm 1500");
                System.out.println("EXAMPLE: java -jar ccmcl.jar -A acts.txt -R -t 6789 -B -T 2 -d\n\n");

                System.out.println("==============================================================================\n");
                System.out.println("                         CCMCL: CLASSIC FUNCTIONALITY                         \n");
                System.out.println("==============================================================================\n");

                System.out.println("--inputfile (-I) : [path to test case file (.txt, .csv)]\n");
                System.out.println("--ACTSfile (-A): [path to .txt or .xml ACTS file]\n");

                System.out.println("--constraints (-C): [path to .txt file containing constraints]\n");
                System.out.println("--tway (-T): [2,3,4,5,6] *any order and any combination of these values*\n");
                System.out.println(
                        "--generate-missing (-G): *generates missing combinations not in test file.*\n"
                        + "                         *Must include  \"-m\"  and  \"-o\"  with this option.*\n"
                        + "                         *Not available for real time mode (-R).*\n"
                                  );
                System.out.println("--minimum-coverage (-m): *Minimum coverage for generating missing combinations*\n");
                System.out.println("--output-missing (-o): *output path for the missing combinations.*\n");
                System.out.println("--append-tests (-a): *appends original tests to missing combinations file.*\n");
                System.out.println("--parameter-names (-P): *parameter names are first line of test case file (-I)*\n");
                System.out.println("--parallel (-p): *Puts the program in parallel processing mode.*\n");
                System.out.println(
                        "--generate-random (-r): *Sets the program to generate a random set of inputs.*\n"
                        + "                        *Must include  \"-n\"  and  \"-f\"  with this option.*\n"
                        + "                        *Not available in real time mode (-R).*\n"
                                  );
                System.out.println("--number-random (-n): *Amount of random inputs to generate.*\n");
                System.out.println("--output-random (-f): *Path to output the random test cases to.*\n");
                System.out.println("--stepchart (-S): *Generates a step chart displaying t-way coverage.*\n");
                System.out.println("--barchart (-B): *Generates a bar chart displaying t-way coverage.*\n");
                System.out.println("--heatmap (-H): *Generates a 2-way coverage heatmap.*\n");
                System.out.println("--display-progress (-d): *Displays progress of coverage measurement*\n");

                System.out.println(
                        "\n\n==============================================================================\n"
                                  );
                System.out.println("                        CCMCL: REAL TIME FUNCTIONALITY                        \n");
                System.out
                        .println("==============================================================================\n\n");
                System.out.println("--realtime (-R) : *Sets the tool in Real Time Measurement Mode*");
                System.out.println("                  *Must specify input type (-k),(-e), or (-t)*");
                System.out.println("                  *Must specify ACTS configuration file (-A)*\n");
                System.out.println("-k : *Specifies real time mode to accept keyboard (stdin) input*\n");
                System.out.println("-e : [path to executable ,program argument 1,program argument 2,etc.]\n");
                System.out.print(
                        "\nNOTE: The (-e) option specifies real time mode to accept input from\n"
                        + "the standard output (console output) of another program.\n\n"
                                );

                System.out.println("-t : [port] *Specifies real time mode to accept TCP input on the port*\n");

                System.out.println("--thread-max (-tm): [max number of threads] *Default 500 threads*\n");
                System.out.print(
                        "\nNOTE: Setting a high thread rate can cause problems in certain situations.\n"
                        + "It is recommended to either throttle data coming into the program or use\n"
                        + "the recommended (default settings)... Unless you know what you are doing...\n\n"
                                );
                System.out.println("--log (-L): [log file path] *Logs incoming data and measurement information*");
                System.out.println(
                        "\nNOTE: --log creates a report holding invalid combinations, total coverage, new\n"
                        + "tests, etc. that occur while measuring in real time mode."
                                  );

                return;

            }

            String param = "";
            if (skip)
            {
                skip = false;
                arg_count++;
                continue;
            }
            String argument = "";
            if (s.equals("-I") || s.equals("-M") || s.equals("-o") || s.equals("-m") || s.equals("-C") || s.equals("-T")
                || s.equals("-n") || s.equals("-f") || s.equals("-A") || s.equals("--inputfile")
                || s.equals("--ACTSfile") || s.equals("--mode") || s.equals("--constraints") || s.equals("--tway")
                || s.equals("--output-missing") || s.equals("--output-random") || s.equals("--minimum-coverage")
                || s.equals("-e") || s.equals("--thread-max") || s.equals("-tm") || s.equals("--log")
                || s.equals("-L") || s.equals("-t"))
            {
                // Command Line parameter with an argument...
                arg_count++;
                if (arg_count >= args.length)
                {
                    System.out.println("Incorrect command line usage. Check --help\n");
                    System.exit(0);
                }
                argument = args[arg_count];
                if (argument.startsWith("-"))
                {
                    System.out.println("Incorrect command line usage: " + s + " " + argument);
                    System.exit(0);
                }
                param = s;
                skip = true;

            }
            else
            {
                arg_count++;
                param = s;
            }

            switch (param)
            {
                case "--realtime":
                case "-R":
                    mode_realtime = true;
                    break;
                case "-k":

                    break;
                case "-e":
                    rtMode = 'e';
                    // stepchart = true;
                    String[] execVals = argument.split(",");
                    rtExPath = execVals[0];
                    rtExArgs.addAll(Arrays.asList(execVals).subList(1, execVals.length));
                    break;
                case "--thread-max":
                case "-tm":
                    threadmax = Integer.parseInt(argument);
                    break;
                case "--log":
                case "-L":
                    log_path = argument;
                    logRT = true;
                    break;
                case "-t":
                    tcp_port = Integer.parseInt(argument);
                    rtMode = 't';
                case "--inputfile":
                case "-I":
                    tests_input_file_path = argument;
                    break;
                case "--constraints":
                case "-C":
                    constraints_path = argument;
                    break;
                case "--parameter-names":
                case "-P":
                    paramNames = true;
                    break;
                case "--ACTSfile":
                case "-A":
                    ACTSpath = argument;
                    ACTSfilePresent = true;
                    break;
                case "--parallel":
                case "-p":
                    parallel = true;
                    break;
                case "--stepchart":
                case "-S":
                    stepchart = true;
                    break;
                case "--barchart":
                case "-B":
                    barchart = true;
                    break;
                case "--heatmap":
                case "-H":
                    heatmap = true;
                    break;
                case "--display-progress":
                case "-d":
                    display_progress = true;
                    break;
                case "--generate-missing":
                case "-G":
                    generateMissing = true;
                    break;
                case "--append-tests":
                case "-a":
                    appendTests = true;
                    break;
                case "--output-missing":
                case "-o":
                    missingCombinationsFilePath = argument;
                    break;
                case "--generate-random":
                case "-r":
                    generateRandom = true;
                    break;
                case "--number-random":
                case "-n":
                    numberOfRandom = Integer.parseInt(argument);
                    break;
                case "--output-random":
                case "-f":
                    try
                    {
                        random_path = argument;
                        fwRandomFile = new FileWriter(argument);
                        bwRandomTests = new BufferedWriter(fwRandomFile);
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case "--minimum-coverage":
                case "-m":
                    minCov = Integer.parseInt(argument);
                    if (minCov <= 0 || minCov > 100)
                    {
                        System.out.println("Can't have a minimum coverage of " + argument);
                        appendTests = false;
                        generateMissing = false;
                    }
                    break;
                case "--tway":
                case "-T":
                    String[] vals = argument.split(",");
                    // tway_values = new String[vals.length];
                    tway_values = new String[5];
                    for (String val : vals)
                    {
                        switch (val)
                        {
                            case "2":
                                tway_values[0] = "2way";
                                if (2 > tway_max)
                                {
                                    tway_max = 2;
                                }
                                break;
                            case "3":
                                tway_values[1] = "3way";
                                if (3 > tway_max)
                                {
                                    tway_max = 3;
                                }
                                break;
                            case "4":
                                tway_values[2] = "4way";
                                if (4 > tway_max)
                                {
                                    tway_max = 4;
                                }
                                break;
                            case "5":
                                tway_values[3] = "5way";
                                if (5 > tway_max)
                                {
                                    tway_max = 5;
                                }
                                break;
                            case "6":
                                tway_values[4] = "6way";
                                tway_max = 6;
                                break;
                            default:
                                System.out.println("Invalid T-way combination parameter: " + argument);
                                return;
                        }
                    }
                    break;
                default:
                    System.out.println(
                            "USAGE: java -jar ccmcl.jar [param1] [param2] ...\n\nor type java -jar ccmcl.jar --help for more options."
                                      );
                    System.exit(0);
                    break;

            }
        }

		/*
         * End of command line arguments parsing
		 */

        // Classic mode based off of the GUI version
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        lblStepChart.setSize(500, 500);
        lblColumnChart.setSize(500, 500);
        lblPointChart.setSize(500, 300);
        pointChartPanel.add(lblPointChart);
        frame.add(lblStepChart, BorderLayout.WEST);
        frame.add(lblColumnChart, BorderLayout.EAST);
        frame.pack();

        frame.addWindowListener(
                new WindowListener()
                {

                    @Override
                    public void windowActivated(WindowEvent arg0)
                    {
                        // TODO Auto-generated method stub
                        frame.setFocusableWindowState(false);
                    }

                    @Override
                    public void windowClosed(WindowEvent arg0)
                    {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void windowClosing(WindowEvent arg0)
                    {
                        // TODO Auto-generated method stub
                        stepchart = false;
                        barchart = false;
                        heatmap = false;
                    }

                    @Override
                    public void windowDeactivated(WindowEvent arg0)
                    {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void windowDeiconified(WindowEvent arg0)
                    {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void windowIconified(WindowEvent arg0)
                    {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void windowOpened(WindowEvent arg0)
                    {
                        // TODO Auto-generated method stub

                    }

                }
                               );

        // Check and make sure the tests input file is present...

        for (int i = 0; i < 5; i++)
        {
            progress[i] = 0;
        }

        if (ACTSfilePresent)
        {
            // check ACTS file
            if (!readTestCaseConfigurationFile(ACTSpath))
            {
                System.exit(0);
                return;
            }
            if (generateRandom)
            {
                GetRandomTests();
            }
        }
        else
        {
            if (!tests_input_file_path.equals(""))
            {
                if (!readTestCaseInputFile(tests_input_file_path))
                {
                    System.out.println("Error: Something went wrong reading the test case .csv file.\n");
                    return;
                }
                else
                {
                    // Test Case input file has been processed and
                    // everything is fine.
                    if (!constraints_path.equals(""))
                    {
                        if (!readConstraintsFile(constraints_path))
                        {
                            System.exit(0);
                            return;
                        }
                    }
                    if (generateRandom)
                    {
                        if (fwRandomFile == null || numberOfRandom == 0)
                        {
                            System.out.println(
                                    "Can't generate random test cases without an output file "
                                    + "and number of test cases wanted specified.\n"
                                              );
                            System.exit(0);
                            return;
                        }
                        // Ok the user wants to generate random tests...
                        GetRandomTests();
                    }
//                    else
//                    {
//
//                        // In classic mode but the user doesn't have a test
//                        // case file
//                        // nor does the user want to generate random
//                        // tests... something is wrong.
//
//                    }
                }
            }
        }
        if (!missingCombinationsFilePath.equals(""))
        {
            System.out.println("\nAdding missing combinations to " + missingCombinationsFilePath);
        }

        // Generate T-way coverage maps
        // The user wants to measure the random tests also...
        System.out.println("\n\nCALCULATING T-WAY COVERAGE OF TEST CASES...\n");
        boolean measured = false;
        for (String tway_value : tway_values)
        {
            if (tway_value != null)
            {
                Tway(tway_value);
                measured = true;
            }
        }

        if (!measured)
        {
            System.out.println("\nNo t-way parameter specified. Use -T [1,2,3,4,5,6] to measure t-way coverage.\n");
            System.exit(0);
            return;
        }

        try
        {
            // Wait for t-way objects to initialize
            Thread.sleep(250);
            for (int i = 0; i < 5; i++)
            {
                if (tway_values[i] != null)
                {
                    while (!initial_complete[i])
                    {
                        Thread.sleep(1000);
                    }
                }
            }
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        frame.pack();
        max_array_size = nrows;

        if (mode_realtime)
        {
            parallel = false;
			/*
			 * This is for the report... Sorry for the weird output.
			 */
            if (logRT)
            {
                File f = new File(log_path);
                f.delete();
                f.createNewFile();
                String title = "";
                title = "***************************************************************************";
                Files.write(Paths.get(log_path), title.getBytes(), StandardOpenOption.APPEND);
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                title = "*  CCMCL: Combinatorial Coverage Measurement Tool (Command Line Version)  *";
                Files.write(Paths.get(log_path), title.getBytes(), StandardOpenOption.APPEND);
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                title = "*              National Institute of Standards and Technology             *";
                Files.write(Paths.get(log_path), title.getBytes(), StandardOpenOption.APPEND);
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                title = "***************************************************************************";
                Files.write(Paths.get(log_path), title.getBytes(), StandardOpenOption.APPEND);
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                title = "Initial tests:";
                Files.write(Paths.get(log_path), title.getBytes(), StandardOpenOption.APPEND);
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                if (max_array_size > 0)
                {

                    try
                    {
                        // Log initial tests...
                        for (String anInfile : infile)
                        {
                            Files.write(Paths.get(log_path), anInfile.getBytes(), StandardOpenOption.APPEND);
                            Files.write(
                                    Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                                    StandardOpenOption.APPEND
                                       );
                        }
                        Files.write(
                                Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                                StandardOpenOption.APPEND
                                   );
                        Files.write(
                                Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                                StandardOpenOption.APPEND
                                   );
                        Files.write(
                                Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                                StandardOpenOption.APPEND
                                   );

                    }
                    catch (IOException e)
                    {
                        // exception handling left as an exercise for the reader
                    }

                    // Log initial invalid combinations...
                    for (int i = 0; i < 5; i++)
                    {
                        if (tway_objects[i] != null)
                        {
                            try
                            {
                                title = "\n\nInitial " + (i + 2) + "-way invalid combinations: \n";
                                Files.write(Paths.get(log_path), title.getBytes(), StandardOpenOption.APPEND);
                                Files.write(
                                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                                        StandardOpenOption.APPEND
                                           );
                                Files.write(
                                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                                        StandardOpenOption.APPEND
                                           );

                                for (String[][] str : tway_objects[i].get_InvalidComb())
                                {
                                    String invalidCombString = "";
                                    for (String[] aStr : str)
                                    {
                                        String inval = aStr[0] + " = " + aStr[1] + " ; ";
                                        invalidCombString += inval;
                                    }
                                    Files.write(
                                            Paths.get(log_path), invalidCombString.getBytes(),
                                            StandardOpenOption.APPEND
                                               );
                                    initial_invalid.put(invalidCombString, true);
                                    invalidCombIndex[i]++;
                                    Files.write(
                                            Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                                            StandardOpenOption.APPEND
                                               );
                                }
                                Files.write(
                                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                                        StandardOpenOption.APPEND
                                           );
                                Files.write(
                                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                                        StandardOpenOption.APPEND
                                           );

                            }
                            catch (IOException ignored)
                            {

                            }
                        }
                    }
                }

				/*
				 * End of code for the reporting stuff...
				 */

                for (int i = 0; i < 5; i++)
                {
                    if (tway_objects[i] != null)
                    {
                        try
                        {
                            title = "";
                            switch (i)
                            {

                                case 0:
                                    title = "Initial 2-way coverage: " + initial_coverage[0];
                                    break;
                                case 1:
                                    title = "Initial 3-way coverage: " + initial_coverage[1];
                                    break;
                                case 2:
                                    title = "Initial 4-way coverage: " + initial_coverage[2];
                                    break;
                                case 3:
                                    title = "Initial 5-way coverage: " + initial_coverage[3];
                                    break;
                                case 4:
                                    title = "Initial 6-way coverage: " + initial_coverage[4];
                                    break;
                            }
                            Files.write(Paths.get(log_path), title.getBytes(), StandardOpenOption.APPEND);
                            Files.write(
                                    Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                                    StandardOpenOption.APPEND
                                       );

                        }
                        catch (IOException ignored)
                        {

                        }
                    }
                }
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                Files.write(
                        Paths.get(log_path),
                        ("=========================================="
                         + "=============================================================================")
                                .getBytes(),
                        StandardOpenOption.APPEND
                           );
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
                Files.write(
                        Paths.get(log_path), System.getProperty("line.separator").getBytes(),
                        StandardOpenOption.APPEND
                           );
            }
			/*
			 * Load the tests infile into a file...
			 * 
			 * Save beginning and end position on tests...
			 */

            // Real time mode.
            if (ACTSfilePresent)
            {

                // ACTS file checks out.
                // If constraints are present, they've already been processed.
                // If a previous test case file is present, it too has already
                // been
                // processed.

				/*
				 * Time to set up real time monitoring.
				 */
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                lblStepChart.setSize(500, 500);
                frame.add(lblStepChart, BorderLayout.WEST);
                frame.pack();

                ReadOperation readData = null;

                switch (rtMode)
                {
                    case 'k':
                        readData = new ReadStandardInputOperation();
                        break;
                    case 'e':
                        readData = new ReadProgramOutputOperation();
                        break;
                    case 't':
                        readData = new ReadTCPInputOperation();
                        break;
                    default:
                        readData = new ReadStandardInputOperation();
                }
                Thread read_input = new Thread(readData);
                read_input.start();

            }
            else
            {
                System.out.println("Must specify an ACTS configuration file.\n");
                System.exit(0);
            }
        }

    }

    /*
	 * Zach
	 * 
	 * Reads the test case configuration file. This file should follow the same
	 * format as the ACTS input files specified in the ACTS User manual.
	 * 
	 */
    public static boolean readTestCaseConfigurationFile(String path)
            throws ParserConfigurationException, SAXException, IOException
    {
		/*
		 * First read through the ACTS file and create all the parameters...
		 */
        List<String> constraints = new ArrayList<String>();
        ext = ACTSpath.substring(ACTSpath.lastIndexOf("."), ACTSpath.length());
        if (!ext.equals(".txt") && !ext.equals(".xml"))
        {
            System.out.println("ACTS file must either be a .txt or .xml file.\n");
            return false;
        }
        String line = "";

        // File is a .txt file
        text_file_loop:
        if (ext.equals(".txt"))
        {

            try
            {
                List<String> params = new ArrayList<String>();
                FileInputStream fstream = new FileInputStream(path);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                Pattern p = Pattern.compile("\\[(.*?)\\]");
                Matcher m;
                boolean in_param_section = false;
                boolean in_constraint_section = false;
                boolean in_tests_section = false;
                int num_rows = 0;
                while ((line = br.readLine()) != null)
                {
                    line = line.trim();
                    m = p.matcher(line);
                    if (line.contains("<?xml"))
                    {
                        break text_file_loop;
                    }

                    if (m.find())
                    {
                        switch (m.group(1))
                        {
                            case "Constraint":
                                in_constraint_section = true;
                                in_param_section = false;
                                in_tests_section = false;
                                // break;
                                continue;
                            case "Parameter":
                                in_param_section = true;
                                in_constraint_section = false;
                                in_tests_section = false;
                                // break;
                                continue;
                            case "Test Set":
                                in_tests_section = true;
                                in_param_section = false;
                                in_constraint_section = false;
                                continue;
                            default:
                                if (line.contains(","))
                                {
                                    // Range Value section... Interval notation
                                    break;
                                }
                                in_constraint_section = false;
                                in_param_section = false;
                                in_tests_section = false;
                                // break;
                                continue;
                        }
                    }
//                    if (line.equals(""))
//                    {
//                    }
//                    else if (line.startsWith("--"))
//                    {
//                        // comment section so continue
//                    }
                    /*else*/
                    if (in_constraint_section && !line.replaceAll("\\s", "").equals(""))
                    {
                        constraints.add(line);
                    }
                    else if (in_param_section && !line.replaceAll("\\s", "").equals(""))
                    {

                        String parameter_name = line.substring(0, line.indexOf("("));
                        params.add(parameter_name);
                    }
                    else if (in_tests_section && !line.replaceAll("\\s", "").equals(""))
                    {
                        // In tests section and tests are present... ignore
                        // input file if present.
                        num_rows++;
                    }
//                    else
//                    {
//                        // not in any of the right sections...
//                    }

                }
                // Create the initial parameters...
                String param_arg = "";
                for (String name : params)
                {
                    param_arg += name;
                    param_arg += ",";
                }
                param_arg = param_arg.trim().replaceAll("\\s", "");
                if (param_arg.equals(""))
                {
                    System.out.println(
                            "Make sure the parameter section is defined right in the ACTS configuration file.\n\nCheck the spelling.\n\n"
                                      );
                }
                param_arg = param_arg.substring(0, param_arg.length() - 1);

                CreateParameters(param_arg.split(",").length, param_arg);

                // Set the number of parameters and columns that should be
                // present in the input file...

                br.close();
                ncols = param_arg.split(",").length;
                values = new String[ncols];

                HashMapParameters();
                setupParams(true);
                if (num_rows > 0)
                {
                    nrows = num_rows;
                    infile = new String[nrows];
                    setupParams(false);
                }

            }
            catch (Exception ex)
            {
                System.out.println("Something went wrong reading the ACTS file.\n" + ex.getMessage());
                return false;
            }

            try
            {

                // Now we need to get all of the values and put them in the
                // correct parameter.
                int test_index = 0;
                int paramIndex = 0;
                List<String[]> values_array = new ArrayList<String[]>();
                List<Integer> types = new ArrayList<Integer>();
                FileInputStream fstream = new FileInputStream(path);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                line = "";
                Pattern p = Pattern.compile("\\[(.*?)\\]");
                boolean in_constraint_section = false;
                boolean in_param_section = false;
                boolean in_tests_section = false;
                while ((line = br.readLine()) != null)
                {
                    if (line.trim().length() == 0)
                    {
                        continue;
                    }
                    line = line.trim();

                    Matcher m = p.matcher(line);
                    if (m.find())
                    {
                        switch (m.group(1))
                        {
                            case "Constraint":
                                in_constraint_section = true;
                                in_param_section = false;
                                in_tests_section = false;
                                // break;
                                continue;
                            case "Parameter":
                                in_param_section = true;
                                in_constraint_section = false;
                                in_tests_section = false;
                                // break;
                                continue;
                            case "Test Set":
                                in_tests_section = true;
                                in_param_section = false;
                                in_constraint_section = false;
                                continue;
                            default:
                                if (line.contains(","))
                                {
                                    // Range Value section... Interval notation
                                    break;
                                }
                                in_constraint_section = false;
                                in_param_section = false;
                                in_tests_section = false;
                                // break;
                                continue;
                        }

                    }

                    // Checks to see if the line is a comment
                    if (line.startsWith("--"))
                    {
                        continue;
                    }

                    if (in_constraint_section)
                    {
                        // Constraints file isn't present
                        // add the line to the constraints...
                        // constraints.add(line);
                        continue;
                    }
                    else if (in_param_section && !line.replaceAll("\\s", "").equals(""))
                    {

						/*
						 * Need to parse range and boundary values if present.
						 * 
						 */

                        String value_line = line.substring(line.lastIndexOf(":") + 1, line.length()).trim().replaceAll(
                                "\\s", ""
                                                                                                                      );

                        // String[] vals = value_line.split(",");

                        if (value_line.contains("[") || value_line.contains("]") || value_line.contains("(")
                            || value_line.contains(")"))
                        {
                            // Range value in Interval notation
                            // Type = 3 for RANGES
                            types.add(3);
                            String param_name = line.substring(0, line.indexOf("(") - 1);
                            constraints.add(build_implicit_constraint(value_line, param_name));

							/*
							 * Get all the boundary values from the interval
							 * notation...
							 */
                            List<String> boundary_vals = new ArrayList<String>();
                            int interval_side = 1;
                            boolean include = false;
                            String current_number = "";
                            for (char c : value_line.trim().replaceAll("\\s", "").toCharArray())
                            {
                                boolean isDigit = (c >= '0' && c <= '9');
                                if (!isDigit)
                                {
                                    switch (c)
                                    {
                                        case '*':
                                            break;
                                        case ',':
                                            if (current_number.equals(""))
                                            {
                                                break;
                                            }
                                            if (interval_side == 1)
                                            {
                                                if (include && !boundary_vals
                                                        .contains(String.valueOf(Integer.parseInt(current_number) - 1)))
                                                {
                                                    boundary_vals.add(
                                                            String.valueOf(
                                                                    Integer.parseInt(current_number)
                                                                    - 1
                                                                          )
                                                                     );
                                                }
                                                else if (!include && !boundary_vals.contains(current_number))
                                                {
                                                    boundary_vals.add(current_number);
                                                }
                                            }
                                            else if (interval_side == 2)
                                            {
                                                if (include && !boundary_vals.contains(current_number))
                                                {
                                                    boundary_vals.add(current_number);
                                                }
                                                else if (!include && !boundary_vals
                                                        .contains(String.valueOf(Integer.parseInt(current_number) - 1)))
                                                {
                                                    boundary_vals.add(
                                                            String.valueOf(
                                                                    Integer.parseInt(current_number)
                                                                    - 1
                                                                          )
                                                                     );
                                                }
                                            }
                                            else if (current_number.equals(""))
                                            {
                                                // Between Range Intervals...
                                                continue;
                                            }
                                            current_number = "";
                                            break;
                                        case '[':
                                            interval_side = 1;
                                            include = true;
                                            break;
                                        case ']':
                                            interval_side = 2;
                                            include = true;
                                            break;
                                        case '(':
                                            interval_side = 1;
                                            include = false;
                                            break;
                                        case ')':
                                            interval_side = 2;
                                            include = false;
                                            break;
                                        case '-':
                                            if (current_number.equals(""))
                                            {
                                                current_number += '-';
                                            }
                                            break;
                                        case ' ':
                                            // its a whitespace character.
                                            break;
                                        default:
                                            System.out.println("Incorrect Range Value defintion in ACTS input file.\n");
                                            return false;
                                    }
                                }
                                else
                                {
                                    current_number += c;
                                }
                            }

                            if (!current_number.equals("") && !current_number.equals("*"))
                            {
                                // Add the last value in interval to boundary
                                // values...
                                if (include && !boundary_vals.contains(current_number))
                                {
                                    boundary_vals.add(current_number);
                                }
                                else if (!include && !boundary_vals
                                        .contains(String.valueOf(Integer.parseInt(current_number) - 1)))
                                {
                                    boundary_vals.add(String.valueOf(Integer.parseInt(current_number) - 1));
                                }
                            }

                            rng[paramIndex] = true;
                            int n = boundary_vals.size() + 1;

                            // This just makes sure that at least one value was
                            // present in the equivalence class
                            if (n < 2)
                            {
                                System.out.println("Must have at least 2 values when defining an equivalence class.\n");
                                return false;
                            }
                            Parameter parm = parameters.get(paramIndex);
                            parameters.remove(parm);
                            nbnds = (n - 1 > 0 ? n - 1 : 1);
                            nvals[paramIndex] = n;
                            if (bnd[paramIndex] == null)
                            {
                                bnd[paramIndex] = new double[nbnds];
                            }
                            boundariesSet = new Boolean[nbnds]; // new set of
                            // boundaries
                            // required
                            // since num of
                            // values
                            // changed
                            for (int j = 0; j < nbnds; j++)
                            {
                                boundariesSet[j] = false;
                            }

                            // Here is where we process the boundary values..
                            for (int x = 0; x < boundary_vals.size(); x++)
                            {

                                try
                                {
                                    bnd[paramIndex][x] = Double.parseDouble(boundary_vals.get(x));
                                    parm.addBound(bnd[paramIndex][x]);
                                }
                                catch (Exception ex)
                                {
                                    System.out.println("Invalid input for boundary value." + boundary_vals.get(x));
                                    return false;
                                }
                                boundariesSet[x] = true; // indicate this bound
                                // has been set
                                parm.setBoundary(true);
                            }

                            parm.removeAllValues();

                            String[] tempory = new String[parm.getBounds().size() + 1];
                            for (int b = 0; b <= parm.getBounds().size(); b++)
                            {
                                parm.addValue(Integer.toString(b));
                                tempory[b] = String.valueOf(b);
                            }

                            values_array.add(tempory);

                            parameters.add(paramIndex, parm);

                            paramIndex++;

                        }
                        else if (line.contains("{"))
                        {

                            // Its a group
                            // type = 4 for group
                            types.add(4);

                            grp[paramIndex] = true;

							/*
							 * get all the groups from the ACTS file line...
							 */
                            //
                            // Get the information
                            List<String> groupDeclarations = new ArrayList<String>();
                            String buffer = line.substring(line.indexOf("{"), line.length());
                            String temp_str = "";

                            for (char c : buffer.replaceAll("\\s", "").trim().toCharArray())
                            {
//                                if (c == '{')
//                                {
//                                    continue;
//
//                                }
                                /*else*/
                                if (c == '}')
                                {
                                    groupDeclarations.add(temp_str);
                                    temp_str = "";
                                }
                                else
                                {
                                    temp_str += c;
                                }
                            }

                            List<String[]> all_groups = new ArrayList<String[]>();
                            for (String st : groupDeclarations)
                            {
                                all_groups.add(st.split(","));
                            }

                            // go ahead and add all the values to the parameter
                            for (String[] all_group : all_groups)
                            {
                                for (String anAll_group : all_group)
                                {
                                    parameters.get(paramIndex).addValue(anAll_group);
                                }
                            }

                            Parameter tp = parameters.get(paramIndex);
                            nvals[paramIndex] = groupDeclarations.size();

                            group[paramIndex] = new Object[groupDeclarations.size()];

                            tp.setGroup(true);
                            tp.setValuesO(tp.getValues());
                            tp.removeAllValues();
                            String[] temp_vals = new String[groupDeclarations.size()];
                            for (int index = 0; index < groupDeclarations.size(); index++)
                            {
                                group[paramIndex][index] = groupDeclarations.get(index).trim()
                                                                            .replaceAll("\\s", "");
                                tp.addValue(Integer.toString(index));
                                tp.addGroup(groupDeclarations.get(index).trim().replaceAll("\\s", ""));
                                temp_vals[index] = String.valueOf(index);
                            }
                            values_array.add(temp_vals);

                            paramIndex++;

                        }
                        else
                        {
                            // Normal input definition with no groups or
                            // boundary values.
                            String type = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")"));
                            // values_array.add(value_line.replaceAll("\\s",
                            // "").trim().split(","));
							/*
							 * Need to add functionality that ensures the right
							 * data type is processed...
							 */
                            switch (type)
                            {
                                case "enum":
                                    types.add(1);
                                    break;
                                case "boolean":
                                    types.add(2);
                                    break;
                                case "bool":
                                    types.add(2);
                                    break;
                                case "int":
                                    types.add(0);
                                    break;
                                default:
                                    System.out.println("Incorrect data type : " + type);
                                    return false;
                            }
                            String[] vals = value_line.trim().split(",");
                            nvals[paramIndex] = vals.length;
                            values_array.add(vals);
                            paramIndex++;
                        }
                    }
                    else if (in_tests_section && !line.replaceAll("\\s", "").equals(""))
                    {
                        // Process the tests...
                        if (line.split(",").length != ncols)
                        {
                            System.out.println(
                                    "Test Set section of ACTS files has different number of parameters than specified in "
                                    + "Parameter section.\nExiting..."
                                              );
                            return false;
                        }
                        // Check here to make sure that each value matches up
                        // with the appropriate data type.
                        String[] testcase_vals = line.trim().split(",");
                        for (int i = 0; i < testcase_vals.length; i++)
                        {
                            if (!parameters.get(i).getBoundary() && !parameters.get(i).getGroup())
                            {
                                if (!Arrays.asList(values_array.get(i)).contains(testcase_vals[i]))
                                {
                                    System.out.println(
                                            "Undefined value in the test set\n(" + parameters.get(i).getName() + ") = "
                                            + testcase_vals[i] + " @ Test Set Line: " + (test_index + 1)
                                                      );

                                    return false;
                                }
                            }
                            else
                            {
                                if (parameters.get(i).getBoundary())
                                {
                                    if (!Tools.isNumeric(testcase_vals[i]))
                                    {
                                        System.out.println(
                                                "Undefined value in the test set\n("
                                                + parameters.get(i).getName() + ") = " + testcase_vals[i]
                                                + " @ Test Set Line: " + (test_index + 1)
                                                          );
                                        return false;
                                    }
                                }
                                else if (parameters.get(i).getGroup())
                                {
                                    if (!parameters.get(i).getValuesO().contains(testcase_vals[i]))
                                    {
                                        System.out.println(
                                                "Undefined value in the test set\n("
                                                + parameters.get(i).getName() + ") = " + testcase_vals[i]
                                                + " @ Test Set Line: " + (test_index + 1)
                                                          );

                                        return false;
                                    }
                                }
                                else
                                {
                                    System.out.println(
                                            "Something went wrong processing the ACTS input .txt file, while processing the test cases"
                                            + " at test case " + test_index
                                                      );
                                    return false;
                                }
                            }

                        }
                        infile[test_index] = line.trim();
                        test_index++;

                    }
//                    else
//                    {
//                        continue;
//                    }
                }
                br.close();

				/*
				 * ADD THE VALUES TO THE PARAMETER
				 */

                if (values_array.size() != parameters.size())
                {
                    System.out.println("Something went wrong reading the ACTS file.\n");
                    return false;
                }
                for (int z = 0; z < values_array.size(); z++)
                {
                    String[] temp_values = values_array.get(z);
                    Parameter tp = parameters.get(z);
                    try
                    {
                        for (int x = 0; x < temp_values.length; x++)
                        {
                            if (!temp_values[x].trim().equals(""))
                            {
                                if (types.get(z) == 0)
                                {
                                    tp.setType(Parameter.PARAM_TYPE_INT);
                                }
                                else
                                {
                                    if (types.get(z) == 2)
                                    {
                                        tp.setType(Parameter.PARAM_TYPE_BOOL);
                                    }
                                    else if (types.get(z) == 1)
                                    {
                                        tp.setType(Parameter.PARAM_TYPE_ENUM);
                                    }
                                    else
                                    {
                                        break;
                                    }
                                }
                                tp.addValue(temp_values[x].trim());
                                map[z][x] = temp_values[x];
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        System.out.println("\nError: " + e.getMessage());
                    }
                }

				/*
				 * END OF ADDING VALUES TO THE PARAMETER
				 */

				/*
				 * Ok now the ACTS File has processed all the parameters. Now
				 * let's check if constraints need to be processed from a
				 * separate file.
				 */

                // Finally add all of the constraints...
                if (!constraints.isEmpty())
                {
                    if (!constraints_path.equals(""))
                    {
                        System.out.println(
                                "Constraints defined in ACTS file. Using those instead of constraints text file.\n"
                                          );
                    }
                    System.out.println("PROCESSING CONSTRAINTS...");
                    for (String str : constraints)
                    {
                        // System.out.println(str);
                        if (!AddConstraint(str.trim()))
                        {
                            System.out.println("\nBad constraint... EXITING\n");
                            return false;
                        }
                    }
                    System.out.println("\n");

                }

                else
                {
                    if (!constraints_path.equals(""))
                    {
                        if (!readConstraintsFile(constraints_path))
                        {
                            return false;
                        }
                    }
                }
                if (test_index > 0)
                {
                    if (!tests_input_file_path.equals(""))
                    {
                        System.out.println("\nMESSAGE: Test cases defined in ACTS file. Using those instead.\n");
                    }
                    if (setupFile(0) != 0)
                    {
                        return false;
                    }
                }
                else
                {
                    if (!tests_input_file_path.equals(""))
                    {
                        if (!readTestCaseInputFile(tests_input_file_path))
                        {
                            return false;
                        }
                    }

                }

                return true;
            }
            catch (Exception ex)
            {
                System.out.println(ex.getMessage());
            }
        }
        if (ext.equals(".xml") || line.contains("<?xml"))
        {
            // File is an .xml file and we need to parse it differently...

			/*
			 * ==========================================================
			 * PARSING THE .XML FILE
			 * ==========================================================
			 */

            List<String> params = new ArrayList<String>();
            List<Integer> types = new ArrayList<Integer>();
            List<String[]> values_array = new ArrayList<String[]>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xml_doc = builder.parse(ACTSpath);
            NodeList topLevelNodes = xml_doc.getChildNodes().item(0).getChildNodes();
            NodeList paramList = null;
            NodeList constraintList = null;
            NodeList testList = null;
            // Get all the parameters
            for (int i = 0; i < topLevelNodes.getLength(); i++)
            {
                String nodename = topLevelNodes.item(i).getNodeName();
                switch (nodename)
                {
                    case "Parameters":
                        paramList = topLevelNodes.item(i).getChildNodes();
                        break;
                    case "Constraints":
                        constraintList = topLevelNodes.item(i).getChildNodes();
                        break;
                    case "Testset":
                        testList = topLevelNodes.item(i).getChildNodes();
//                    default:
//                        continue;
                }
            }

            // Create all the parameters first...
            for (int i = 0; i < paramList.getLength(); i++)
            {
                Node tempNode = paramList.item(i);
                if (!tempNode.getParentNode().getNodeName().equals("Parameters"))
                {
                    continue;
                }
                if (tempNode.hasAttributes())
                {

                    if (tempNode.getNodeType() == Node.ELEMENT_NODE)
                    {
                        Element e = (Element) tempNode;
                        String name = e.getAttribute("name");
                        params.add(name);
                        types.add(Integer.parseInt(e.getAttribute("type")));
                        NodeList values = e.getElementsByTagName("values").item(0).getChildNodes();
                        if (values.getLength() <= 0)
                        {
                            System.out
                                    .println("Something wrong with .xml file. Can't have 0 values for a parameter.\n");
                            return false;
                        }
                        List<String> temp_vals = new ArrayList<String>();
                        for (int z = 0; z < values.getLength(); z++)
                        {
                            if (!values.item(z).getTextContent().trim().equals(""))
                            {
                                temp_vals.add(values.item(z).getTextContent());
                            }
                        }
                        int m = 0;
                        String[] t = new String[temp_vals.size()];
                        for (int j = 0; j < temp_vals.size(); j++)
                        {
                            t[m] = temp_vals.get(m);
                            m++;
                        }
                        values_array.add(t);
                    }
                }
            }

            // Create the initial parameters...
            String param_arg = "";
            for (String name : params)
            {
                param_arg += name;
                param_arg += ",";
            }
            param_arg = param_arg.trim().replaceAll("\\s", "");
            param_arg = param_arg.substring(0, param_arg.length() - 1);
            ncols = param_arg.split(",").length;

            CreateParameters(param_arg.split(",").length, param_arg);
            values = new String[ncols];
            HashMapParameters();
            setupParams(true);
            int num_pars = 0;
            int num_rows = 0;
            if (!(testList == null))
            {
                for (int i = 0; i < testList.getLength(); i++)
                {
                    Node tempNode = testList.item(i);
                    if (!tempNode.getParentNode().getNodeName().equals("Testset"))
                    {
                        continue;
                    }

                    if (tempNode.hasAttributes())
                    {
                        if (tempNode.getNodeType() == Node.ELEMENT_NODE)
                        {
                            Element e = (Element) tempNode;
                            NodeList values = e.getChildNodes();
                            for (int z = 0; z < values.getLength(); z++)
                            {
                                if (!values.item(z).getTextContent().replaceAll("\\s", "").equals(""))
                                {
                                    num_pars++;
                                }
                            }
                            if (num_pars != parameters.size() + 1)
                            {
                                System.out.println(
                                        "Error: Test cases don't have the same number of parameters as the defined parameters in configuration"
                                        + " file.\nCheck Testset " + (num_rows + 1) + "\n"
                                                  );
                                return false;
                            }
                            num_rows++;
                            num_pars = 0;
                        }
                    }

                }
            }

            if (num_rows > 0)
            {
                nrows = num_rows;
                infile = new String[nrows];
                setupParams(false);
            }

            if (values_array.size() != parameters.size())
            {
                System.out.println("Something went wrong reading the ACTS file.\n");
                return false;
            }

            for (int z = 0; z < values_array.size(); z++)
            {
                String[] temp_values = values_array.get(z);
                Parameter tp = parameters.get(z);

                try
                {
                    for (int x = 0; x < temp_values.length; x++)
                    {

                        if (!temp_values[x].trim().equals(""))
                        {

                            if (types.get(z) == 0)
                            {
                                tp.setType(Parameter.PARAM_TYPE_INT);
                            }
                            else if (types.get(z) == 2)
                            {
                                tp.setType(Parameter.PARAM_TYPE_BOOL);
                            }
                            else if (types.get(z) == 1)
                            {
                                tp.setType(Parameter.PARAM_TYPE_ENUM);
                            }
                            else if (types.get(z) == 3)
                            {
                                // Range value
                                tp.setType(Parameter.PARAM_TYPE_INT);

                                if (values_array.get(z).length > 1)
                                {
                                    System.out.println(
                                            "Incorrectly defined group value in XML file: \n"
                                            + temp_values[x] + "\n" + temp_values[x + 1] + "\n...\n"
                                            + "\nUse \"<value>{1,2,3},{4,5,6}</value>\" format for groups (type = 4).\nOR\n"
                                            + "Use \"<value>(*,6],[7,*)</value>\" format for ranges (type = 3).\n"
                                            + "\nExiting"
                                                      );
                                    return false;
                                }

								/*
								 * Get all the boundary values from the interval
								 * notation...
								 */
                                List<String> boundary_vals = new ArrayList<String>();
                                int interval_side = 1;
                                boolean include = false;
                                String current_number = "";
                                for (char c : temp_values[x].trim().replaceAll("\\s", "").toCharArray())
                                {
                                    boolean isDigit = (c >= '0' && c <= '9');
                                    if (!isDigit)
                                    {
                                        switch (c)
                                        {
                                            case '*':
                                                break;
                                            case ',':
                                                if (current_number.equals(""))
                                                {
                                                    break;
                                                }
                                                if (interval_side == 1)
                                                {
                                                    if (include && !boundary_vals
                                                            .contains(
                                                                    String.valueOf(
                                                                            Integer.parseInt(current_number)
                                                                            - 1
                                                                                  )
                                                                     ))
                                                    {
                                                        boundary_vals
                                                                .add(
                                                                        String.valueOf(
                                                                                Integer.parseInt(current_number)
                                                                                - 1
                                                                                      )
                                                                    );
                                                    }
                                                    else if (!include && !boundary_vals.contains(current_number))
                                                    {
                                                        boundary_vals.add(current_number);
                                                    }
                                                }
                                                else if (interval_side == 2)
                                                {
                                                    if (include && !boundary_vals.contains(current_number))
                                                    {
                                                        boundary_vals.add(current_number);
                                                    }
                                                    else if (!include && !boundary_vals
                                                            .contains(
                                                                    String.valueOf(
                                                                            Integer.parseInt(current_number)
                                                                            - 1
                                                                                  )
                                                                     ))
                                                    {
                                                        boundary_vals
                                                                .add(
                                                                        String.valueOf(
                                                                                Integer.parseInt(current_number)
                                                                                - 1
                                                                                      )
                                                                    );
                                                    }
                                                }
                                                else if (current_number.equals(""))
                                                {
                                                    // Between Range Intervals...
                                                    continue;
                                                }
                                                current_number = "";
                                                break;
                                            case '[':
                                                interval_side = 1;
                                                include = true;
                                                break;
                                            case ']':
                                                interval_side = 2;
                                                include = true;
                                                break;
                                            case '(':
                                                interval_side = 1;
                                                include = false;
                                                break;
                                            case ')':
                                                interval_side = 2;
                                                include = false;
                                                break;
                                            case '-':
                                                if (current_number.equals(""))
                                                {
                                                    current_number += '-';
                                                }
                                                break;
                                            case ' ':
                                                // its a whitespace character.
                                                break;
                                            default:
                                                System.out.println(
                                                        "Incorrect Range Value defintion in ACTS input file.\n"
                                                                  );
                                                return false;
                                        }
                                    }
                                    else
                                    {
                                        current_number += c;
                                    }
                                }

                                if (!current_number.equals("") && !current_number.equals("*"))
                                {
                                    // Add the last value in interval to
                                    // boundary
                                    // values...
                                    if (include && !boundary_vals.contains(current_number))
                                    {
                                        boundary_vals.add(current_number);
                                    }
                                    else if (!include && !boundary_vals
                                            .contains(String.valueOf(Integer.parseInt(current_number) - 1)))
                                    {
                                        boundary_vals.add(String.valueOf(Integer.parseInt(current_number) - 1));
                                    }
                                }

                                rng[z] = true;
                                int n = boundary_vals.size() + 1;

                                // This just makes sure that at least one value
                                // was
                                // present in the equivalence class
                                if (n < 2)
                                {
                                    System.out.println(
                                            "Must have at least 2 values when defining an equivalence class.\n"
                                                      );
                                    return false;
                                }

                                parameters.remove(z);
                                nbnds = (n - 1 > 0 ? n - 1 : 1);
                                nvals[z] = n;
                                if (bnd[z] == null)
                                {
                                    bnd[z] = new double[nbnds];
                                }
                                boundariesSet = new Boolean[nbnds]; // new set
                                // of
                                // boundaries
                                // required
                                // since num
                                // of
                                // values
                                // changed
                                for (int j = 0; j < nbnds; j++)
                                {
                                    boundariesSet[j] = false;
                                }

                                // Here is where we process the boundary
                                // values..
                                for (int y = 0; y < boundary_vals.size(); y++)
                                {

                                    try
                                    {
                                        bnd[z][y] = Double.parseDouble(boundary_vals.get(y));
                                        tp.addBound(bnd[z][y]);
                                    }
                                    catch (Exception ex)
                                    {
                                        System.out.println("Invalid input for boundary value." + boundary_vals.get(y));
                                        return false;
                                    }
                                    boundariesSet[y] = true; // indicate this
                                    // bound
                                    // has been set
                                    tp.setBoundary(true);
                                }

                                tp.removeAllValues();

                                for (int b = 0; b <= tp.getBounds().size(); b++)
                                {
                                    tp.addValue(Integer.toString(b));
                                }

                                parameters.add(z, tp);
                                constraints.add(build_implicit_constraint(temp_values[x].trim(), tp.getName()));

                                continue;
                            }
                            else if (types.get(z) == 4)
                            {
                                // group value
                                // Make all group values ENUM type
                                tp.setType(Parameter.PARAM_TYPE_ENUM);
                                if (values_array.get(z).length > 1)
                                {
                                    System.out.println(
                                            "Incorrectly defined group value (type = 4) in XML file: "
                                            + temp_values[x] + "\n" + temp_values[x + 1] + "\n...\n"
                                            + "\nUse \"<value>{1,2,3},{4,5,6}</value>\" format." + "\nExiting"
                                                      );
                                    return false;
                                }
                                if (temp_values[x].contains("{"))
                                {

                                    // Its a group

                                    grp[z] = true;
                                    // Get the information
                                    List<String> groupDeclarations = new ArrayList<String>();
                                    String buffer = temp_values[x]
                                            .substring(temp_values[x].indexOf("{"), temp_values[x].length())
                                            .replaceAll("\\s", "");
                                    String temp_str = "";

                                    for (char c : buffer.toCharArray())
                                    {
//                                        if (c == '{')
//                                        {
//                                            continue;
//
//                                        }
                                        /*else */
                                        if (c == '}')
                                        {
                                            groupDeclarations.add(temp_str);
                                            temp_str = "";
                                        }
                                        else
                                        {
                                            temp_str += c;
                                        }
                                    }

                                    List<String[]> all_groups = new ArrayList<String[]>();
                                    for (String st : groupDeclarations)
                                    {
                                        all_groups.add(st.replaceAll("\\s", "").split(","));
                                    }

                                    // go ahead and add all the values to the
                                    // parameter
                                    for (String[] all_group : all_groups)
                                    {
                                        for (String anAll_group : all_group)
                                        {
                                            if (!anAll_group.trim().equals(""))
                                            {
                                                parameters.get(z).addValue(anAll_group.trim());
                                            }

                                        }
                                    }

                                    nvals[z] = groupDeclarations.size();

                                    group[z] = new Object[groupDeclarations.size()];

                                    tp.setGroup(true);
                                    tp.setValuesO(tp.getValues());
                                    tp.removeAllValues();
                                    for (int index = 0; index < groupDeclarations.size(); index++)
                                    {
                                        group[z][index] = groupDeclarations.get(index).trim()
                                                                           .replaceAll("\\s", "");
                                        tp.addValue(Integer.toString(index));
                                        tp.addGroup(
                                                groupDeclarations.get(index).trim().replaceAll("\\s", "")
                                                   );
                                    }
                                    continue;

                                }
                                else
                                {
                                    System.out.println(
                                            "Not a valid group value in .xml file: ("
                                            + parameters.get(z).getName() + ") value = " + temp_values[x]
                                                      );
                                    return false;
                                }
                            }
                            else
                            {
                                System.out.println(
                                        "Value type not supported in .xml file: ("
                                        + parameters.get(z).getName() + ") type = " + types.get(z)
                                                  );
                                return false;
                            }
                        }
                        nvals[z]++;
                        tp.addValue(temp_values[x].trim());
                        map[z][x] = temp_values[x];
                    }
                }
                catch (Exception e)
                {
                    System.out.println("\nError: " + e.getMessage());
                }
            }

            List<String> all_constraints = new ArrayList<String>();

            // Create the constraints...
            if (!(constraintList == null))
            {
                for (int i = 0; i < constraintList.getLength(); i++)
                {
                    Node tempNode = constraintList.item(i);
                    if (!tempNode.getParentNode().getNodeName().equals("Constraints"))
                    {
                        continue;
                    }
                    if (tempNode.hasAttributes())
                    {
                        if (tempNode.getNodeType() == Node.ELEMENT_NODE)
                        {
                            Element e = (Element) tempNode;
                            String constraint = e.getAttribute("text");
                            all_constraints.add(constraint.trim());
                        }
                    }
                }
            }

            if (!all_constraints.isEmpty())
            {
                System.out.println("PROCESSING CONSTRAINTS...\n");
                for (String str : all_constraints)
                {
                    System.out.println(str);
                    if (!AddConstraint(str.trim()))
                    {
                        System.out.println("\nBAD CONSTRAINT... EXITING...\n");
                        return false;
                    }
                }

            }

            else
            {
                if (!constraints_path.equals(""))
                {
                    if (!readConstraintsFile(constraints_path))
                    {
                        return false;
                    }
                }
            }

            // Parse the Test Cases
            int current = 0;
            if (!(testList == null))
            {
                for (int i = 0; i < nrows; )
                {
                    System.out.println("CHECKING test " + i);
                    Node tempNode = testList.item(current);
                    if (!tempNode.getParentNode().getNodeName().equals("Testset"))
                    {
                        continue;
                    }

                    if (tempNode.hasAttributes())
                    {
                        String test_line = "";
                        if (tempNode.getNodeType() == Node.ELEMENT_NODE)
                        {
                            Element e = (Element) tempNode;
                            NodeList values_list = e.getChildNodes();

                            int param = 0;
                            boolean first_value = true;
                            for (int z = 0; z < values_list.getLength(); z++)
                            {
                                if (!values_list.item(z).getTextContent().replaceAll("\\s", "").equals(""))
                                {
                                    if (first_value)
                                    {
                                        // The first value defines the test set
                                        // number according to ACTS format
                                        first_value = false;
                                        continue;
                                    }
                                    test_line += (values_list.item(z).getTextContent().trim() + ",");
                                    System.out.println(
                                            "ON parameter " + param + " | " + parameters.get(param).getName()
                                                      );
                                    Parameter mp = parameters.get(param);
                                    param++;
                                    if (!mp.getGroup() && !mp.getBoundary())
                                    {
                                        if (!mp.getValues().contains(values_list.item(z).getTextContent().trim()))
                                        {
                                            System.out.println(
                                                    "\nNot a valid value in test case: " + (i + 1)
                                                    + "\n(value) = " + values_list.item(z).getTextContent()
                                                    + " [TEST SET LINE: " + test_line.split(",").length + "]\nEXITING"
                                                              );

                                            System.out.println("Valid values are: ");
                                            for (String val : mp.getValues())
                                            {
                                                System.out.println(val);
                                            }
                                            return false;
                                        }
                                    }
                                    else if (mp.getGroup() || mp.getBoundary())
                                    {
                                        if (mp.getGroup())
                                        {
                                            if (!mp.getValuesO().contains(values_list.item(z).getTextContent().trim()))
                                            {
                                                System.out.println(
                                                        "\nNot a valid value in test case: " + (i + 1) + "\n(value) = "
                                                        + values_list.item(z).getTextContent() + " [TEST SET LINE: "
                                                        + test_line.split(",").length + "]\nEXITING"
                                                                  );
                                                return false;
                                            }
                                        }
                                        else
                                        {
                                            if (!Tools.isNumeric(values_list.item(z).getTextContent().trim()))
                                            {
                                                System.out.println(
                                                        "\nNot a valid value in test case: " + (i + 1) + "\n(value) = "
                                                        + values_list.item(z).getTextContent() + " [TEST SET LINE: "
                                                        + test_line.split(",").length + "]\nEXITING"
                                                                  );
                                                return false;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        System.out.println(
                                                "\nSomething went wrong processing the parameter value: "
                                                + values_list.item(z).getTextContent() + "\n"
                                                          );
                                        return false;
                                    }
                                }
                            }
                            infile[i] = test_line.substring(0, test_line.length() - 1).trim();
                            i++;
                        }
                    }
                    current++;
                }
            }
            if (!(infile == null))
            {
                if (setupFile(0) != 0)
                {
                    return false;
                }
            }

			/*
			 * ========================================================== END OF
			 * PARSING THE .XML FILE
			 * ==========================================================
			 */

            if (!tests_input_file_path.equals(""))
            {
                // Test case file is present...
                if (!(infile == null))
                {
                    System.out.println("\nTest cases defined in .xml configuration file... Using those instead.\n");
                }
                else if (!readTestCaseInputFile(tests_input_file_path))
                {
                    return false;
                }
            }

            return true;
        }
        else
        {
            System.out.println("File type not supported. Must use .xml or .txt file extension.");
        }
        return false;
    }

    /*
	 * Zach
	 * 
	 * This will read a constraints.txt file if the user provides it. If both a
	 * constraints file and an ACTS file are present, the constraints file will
	 * be used rather than the ACTS file.
	 * 
	 */
    public static boolean readConstraintsFile(String path)
    {
        // if(data.isActsFilePresent()){
        // return false;
        // }
        FileInputStream fstream;
        try
        {
            List<String> constraints = new ArrayList<String>();
            fstream = new FileInputStream(path);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = br.readLine()) != null)
            {
                constraints.add(line);
//                continue;
            }
            br.close();
            System.out.println("PROCESSING CONSTRAINTS...\n");
            for (String str : constraints)
            {
                // System.out.println(str);
                if (!AddConstraint(str.trim()))
                {
                    System.out.println("\nBAD CONSTRAINT... EXITING...\n");
                    return false;
                }
            }
            return true;
        }
        catch (IOException e)
        {
            System.out.println(
                    "ERROR: Something went wrong in the constraints .txt file you provided. " + e.getMessage()
                              );
            return false;
        }
    }

    /*
	 * Zach
	 * 
	 * Reads the test case input file. If the file doesn't exist, one is created
	 * and used for later.
	 * 
	 * If no ACTS file is present, the program enters auto-detect mode.
	 * 
	 */
    public static boolean readTestCaseInputFile(String path)
    {

        int lastlen = 0; // used to check that each line of the input file has
        // the same number of columns as the last.

		/*
		 * If the ACTS file is present we need to look at this differently
		 */
        if (ACTSfilePresent)
        {
            try
            {
                int i = 0;
                int cols = 0;
                int rows = 0;

                FileInputStream fstream = new FileInputStream(path);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                String line;
                // Read File Line By Line
                while ((line = br.readLine()) != null)
                {
                    values = line.split(",");
                    int columns = values.length;
                    if (columns != ncols)
                    {
                        System.out.println("\nTest case input file doesn't match up with ACTS file.\nExiting\n");
                        return false;
                    }
                    if (paramNames && cols == 0)
                    {
                        // Essentially auto detection mode for parameter values
                        // CreateParameters(columns, line);
                        int temp = 0;
                        for (Parameter pre : parameters)
                        {
                            ;
                            if (!values[temp].trim().replaceAll("\\s", "")
                                             .equals(pre.getName().trim().replaceAll("\\s", "")))
                            {
                                // The parameter names aren't in the same order
                                // as the ACTS file.
                                System.out.println(
                                        "Error: Make sure the test case file parameter names are in the same order as"
                                        + " the parameter names in the ACTS configuration file.\nIf the test case file has parameter "
                                        + "names in the first row, add a -P option to the command line arguments.\n"
                                                  );
                                return false;
                            }
                            temp++;
                        }
                        cols = columns;
                        continue;
                    }
                    if (line.contains(","))
                    {
                        values = line.split(",");
                        cols = values.length;
                        if (cols != ncols)
                        {
                            System.out.println("Test case input file doesn't match up with ACTS file.\n");
                            return false;
                        }
                        i++;
                    }

                }

                rows = i;
                br.close();
                nrows = rows;
                infile = new String[nrows];
                setupParams(false);
            }
            catch (Exception ignored)
            {

            }

            // Now set up the test cases.
            try
            {
                FileInputStream fstream = new FileInputStream(path);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line;
                int i = 0;
                boolean read_params = false;
                while ((line = br.readLine()) != null)
                {
                    line = line.trim();
                    values = line.split(",");
                    int columns = values.length;
                    if (paramNames && i == 0 && !read_params)
                    {
                        read_params = true;
                        continue;
                    }
                    else if (line.contains(","))
                    {
                        values = line.trim().split(",");
                        infile[i] = line;

                        for (int t = 0; t < values.length; t++)
                        {
                            if (parameters.get(t).getBoundary())
                            {
                                if (!Tools.isNumeric(values[t]))
                                {
                                    System.out
                                            .println(
                                                    "Undefined value in the test set\n(" + parameters.get(t).getName()
                                                    + ") = " + values[t] + " @ Test Set Line: " + (i + 1)
                                                    );
                                    return false;
                                }
                                continue;
                            }
                            else if (parameters.get(t).getGroup())
                            {
                                // Add error checking here to ensure only values
                                // defined in groups are here
                                if (!parameters.get(t).getValuesO().contains(values[t]))
                                {
                                    System.out
                                            .println(
                                                    "Undefined value in the test set\n(" + parameters.get(t).getName()
                                                    + ") = " + values[t] + " @ Test Set Line: " + (i + 1)
                                                    );
                                    return false;
                                }
                                continue;
                            }
                            if (!parameters.get(t).getValues().contains(values[t]))
                            {
                                System.out.println(
                                        "Undefined value in the test set\n(" + parameters.get(t).getName()
                                        + ") = " + values[t] + " @ Test Set Line: " + (i + 1)
                                                  );

                                return false;
                            }
                        }
                    }
                    i++;
                }

                br.close();
                return setupFile(0) == 0;
            }
            catch (Exception ex)
            {
                System.out.println("Error: Something went wrong reading the input.csv file.\n" + ex.getMessage());
                return false;
            }

        }

        try
        {

			/*
			 * Read through the file the first time to determine file size and
			 * allocate memory. This is for auto-detect mode
			 */

            int i = 0;
            int cols = 0;
            int rows = 0;
            FileInputStream fstream = new FileInputStream(path);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = "";
            // Read File Line By Line

            while ((line = br.readLine()) != null)
            {
                line = line.trim();
                values = line.split(",");
                int columns = values.length;
                if (paramNames && cols == 0)
                {
                    // Essentially auto detection mode for parameter values
                    CreateParameters(columns, line);
                    cols = columns;
                    continue;
                }
                else if (paramNames && cols == 0)
                {
                    CreateParameters(columns, "");
                    cols = columns;
                    rows++;
                    continue;
                }
                else if (paramNames && cols == 0)
                {
                    cols = parameters.size();
                    continue;
                }
                if (line.contains(","))
                {
                    values = line.split(",");
                    cols = values.length;
                    // Make sure that all rows have the same number of columns.
                    if ((i > 0 && cols != lastlen) || cols < 2)
                    {
                        System.out.println("Invalid input file.\n must have same number of columns all rows");
                        br.close();
                        return false;
                    }
                    else
                    {
                        lastlen = cols;
                    }
                    i++;
                    rows++;
                }

            }

            br.close();
            ncols = cols;
            nrows = rows;
            values = new String[ncols];
            HashMapParameters();
            infile = new String[nrows];
            setupParams(false);

        }
        catch (Exception ex)
        {
            System.out.println(
                    "File cannot be opened for initial test and parameter counting.\nFile may be open in another process."
                    + "\nError: " + ex.getMessage()
                              );
            return false;
        }
		/*
		 * Read through the file again to process tests.
		 */
        try
        {

            FileInputStream fstream = new FileInputStream(path);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = "";
            int i = 0;
            boolean read_params = false;
            while ((line = br.readLine()) != null)
            {
                line = line.trim();
                values = line.split(",");
                if (paramNames && i == 0 && !read_params)
                {
                    read_params = true;
                    continue;
                }
                else if (line.contains(","))
                {
                    values = line.split(",");
                    infile[i] = line;
                    AddValuesToParameters(values);
                }
                i++;
            }

            br.close();
            if (setupFile(0) != 0)
            {
                return false;
            }
        }
        catch (Exception ex)
        {
            System.out.println("Error: Something went wrong reading the input.csv file.\n" + ex.getMessage());
            return false;
        }
        return true;

    }

    /*
	 * Adds a list of values to the parameters.
	 */
    public static void AddValuesToParameters(String[] values)
    {
        if (values.length != parameters.size())
        {
            return;
        }
        Parameter p;
        try
        {
            for (int x = 0; x < values.length; x++)
            {
                if (!values[x].trim().equals(""))
                {
                    p = parameters.get(x);
                    if (Tools.isNumeric(values[x]))
                    {
                        p.setType(Parameter.PARAM_TYPE_INT);
                    }
                    else
                    {
                        if (values[x].trim().toUpperCase().equals("TRUE")
                            || values[x].trim().toUpperCase().equals("FALSE"))
                        {
                            p.setType(Parameter.PARAM_TYPE_BOOL);
                        }
                        else
                        {
                            p.setType(Parameter.PARAM_TYPE_ENUM);
                        }
                    }
                    p.addValue(values[x].trim());
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("\nError: " + e.getMessage());
        }
    }

    /*
	 * Create parameter names
	 */
    public static void CreateParameters(int number, String parameterNames)
    {
        Parameter p;
        String[] names = parameterNames.split(",");
        for (int i = 0; i < number; i++)
        {
            // if names are not in file, by default parameters will be named P1,
            // P2, p3...
            if (ACTSfilePresent)
            {
                p = new Parameter(names[i].trim());
            }
            if (!paramNames && !ACTSfilePresent)
            {
                p = new Parameter("P" + (i + 1));
            }
            else
            {
                p = new Parameter(names[i].trim());
            }
            parameters.add(p);
        }
    }

    // Add constraint to a list of constraints
    public static boolean AddConstraint(String str)
    {

        try
        {

            String[] x = ConstraintManager.JustParameterValues(str.trim());
			/*
			 * Need to modify constraint if it has a boolean operator in front
			 * of parameter name (This is mainly to support parsing of ACTS .xml
			 * files)
			 */

            if (Checker(x) && !ConstraintExists(str))
            {
                // create parser object

                ConstraintParser cp = new ConstraintParser(str, pars);
                // parse constraint to format choco constraint
                cp.parse();

                // add constraint to list
                constraints.add(new meConstraint(str, usedParams));
                usedParams = null;
                return true;
            }
            return ConstraintExists(str);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    // Key/Value list for parameters, used to solve constraints
    public static void HashMapParameters()
    {
        if (pars == null)
        {
            pars = new HashMap<String, Parameter>();
        }
        if (pars.size() == 0)
        {
            for (Parameter p : parameters)
            {
                pars.put(p.getName(), p);
            }
        }
    }

    /*
	 * This will make sure that the constraint is formed with real elements
	 * (parameters and its values)
	 */
    protected static Boolean Checker(String[] str)
    {
        Boolean r = false;

        HashMap<String, Parameter> lp = new HashMap<String, Parameter>();
        List<String> used_parameters = new ArrayList<String>();

        for (Parameter p : parameters)
        {
            lp.put(p.getName(), p);
        }

        int ant = 0;
        for (int i = 0; i < str.length; i++)
        {
            if (!str[i].trim().equals(""))
            {

                // Check if its a variable value
                if (ConstraintManager.isVariableValue(str[i].trim()))
                {
                    if (i > 0)
                    {
                        ant = i - 1;
                    }
                    String key = str[ant];

                    Parameter p = lp.get(key.trim());

                    if (!ConstraintManager.isValidParameterValue(str[i].trim(), p))
                    {
                        System.out.println("Invalid Value: " + p.getName() + " = " + str[i].trim());
                        return false;
                    }

                }
                else
                {

                    // Its a parameter
                    if (ConstraintManager.isParameter(str[i].trim(), lp))
                    {
                        if (!used_parameters.contains(str[i].trim()))
                        {
                            used_parameters.add(str[i].trim());
                        }
                    }
                    else
                    {
                        System.out.println("Invalid Parameter/Value!: " + str[i].trim());
                        return false;
                    }
                }

            }

        }

        usedParams = used_parameters;
        if (usedParams.size() > 0)
        {
            r = true;
        }

        return r;
    }

    // Check to make sure constraints are not repeated
    public static Boolean ConstraintExists(String str)
    {

        for (meConstraint constraint : constraints)
        {

            if (constraint.get_cons().equalsIgnoreCase(str))
            {
                System.out.println("The constriant already exists!!");
                return true;
            }

        }

        return false;

    }

    /*
	 * =========================================================================
	 * ================================
	 * 
	 * Set up arrays for in memory storage of tests
	 * 
	 * =========================================================================
	 * ================================
	 */
    protected static void setupParams(boolean procACTS)
    {// creates arrays
        // needed for
        // processing
        try
        {
            if (ACTSfilePresent)
            {
                if (procACTS)
                {
                    // allocate array for 2-way heat map values
                    hm_colors2 = new int[ncols][];
                    for (int i = 0; i < ncols; i++)
                    {
                        hm_colors2[i] = new int[ncols];
                    }

                    if (nvals == null || ncols != prev_ncols)
                    {
                        nvals = new int[ncols];
                    }

                    if (valSet == null || ncols != prev_ncols)
                    {
                        valSet = new Boolean[ncols];
                        for (int i = 0; i < ncols; i++)
                        {
                            valSet[i] = false;
                        }
                    }

                    if (bnd == null || ncols != prev_ncols)
                    {
                        bnd = new double[ncols][];
                    }

                    if (rng == null || ncols != prev_ncols)
                    {
                        rng = new Boolean[ncols];
                        Arrays.fill(rng, Boolean.FALSE);
                    }

                    if (group == null || ncols != prev_ncols)
                    {
                        group = new Object[ncols][];
                    }

                    if (grp == null || ncols != prev_ncols)
                    {
                        grp = new Boolean[ncols];
                        Arrays.fill(grp, Boolean.FALSE);
                    }

                    if (map == null || ncols != prev_ncols)
                    {
                        map = new String[ncols][];
                        for (int i = 0; i < ncols; i++)
                        {
                            map[i] = new String[nmapMax];
                        }
                    }
                    prev_ncols = ncols;
                }
                else
                {

                    // allocate array for in-memory storage of tests
                    test = new int[nrows][];
                    for (int i = 0; i < nrows; i++)
                    {
                        test[i] = new int[ncols];
                    }

                }
            }
            else
            {
				/*
				 * No ACTS File present... Set up everything here.
				 */
                // allocate array for in-memory storage of tests
                test = new int[nrows][];
                for (int i = 0; i < nrows; i++)
                {
                    test[i] = new int[ncols];
                }

                // allocate array for 2-way heat map values
                hm_colors2 = new int[ncols][];
                for (int i = 0; i < ncols; i++)
                {
                    hm_colors2[i] = new int[ncols];
                }

                if (nvals == null || ncols != prev_ncols)
                {
                    nvals = new int[ncols];
                }

                if (valSet == null || ncols != prev_ncols)
                {
                    valSet = new Boolean[ncols];
                    for (int i = 0; i < ncols; i++)
                    {
                        valSet[i] = false;
                    }
                }

                if (bnd == null || ncols != prev_ncols)
                {
                    bnd = new double[ncols][];
                }

                if (rng == null || ncols != prev_ncols)
                {
                    rng = new Boolean[ncols];
                    Arrays.fill(rng, Boolean.FALSE);
                }

                if (group == null || ncols != prev_ncols)
                {
                    group = new Object[ncols][];
                }

                if (grp == null || ncols != prev_ncols)
                {
                    grp = new Boolean[ncols];
                    Arrays.fill(grp, Boolean.FALSE);
                }

                if (map == null || ncols != prev_ncols)
                {
                    map = new String[ncols][];
                    for (int i = 0; i < ncols; i++)
                    {
                        map[i] = new String[nmapMax];
                    }
                }
                prev_ncols = ncols;
            }

        }
        catch (Exception ex)
        {
            System.out.println("Problem with file or parameter/value specs.\n" + ex.getMessage());
        }
    }

	/*
	 * ======================END OF SETUP PARAMS
	 * FUNCTION==================================================
	 */

	/*
	 * 
	 * 
	 * 
	 * /* Use this function as a means of allocating memory
	 */

    public static int setupFile(int start)
    {
        final int ERR = 5;
        if (infile == null)
        {
            System.out.println("Test file must be loaded.");
            return ERR;
        }
        if (rng == null)
        {
            System.out.println("Set parameter values.");
            return ERR;
        }

        int i = start;
        while (i < nrows)
        {
            values = infile[i].split(",");

            // locate this value in mapping array; if not found, create and
            // return value
            // determine the index of each input row field and use it in the
            // test array
            // first read in the parameter values for this row as trings

            int j;
            int m;
            for (m = 0; m < ncols; m++)
            {

                if (!rng[m] && !grp[m])
                { // discrete values, not range

                    // find value in map array and store its index as value for
                    // coverage calculation

                    Boolean fnd = false;
                    int locn = 0;
                    for (j = 0; j < nmapMax && !fnd && map[m][j] != null; j++)
                    {
                        if (map[m][j].equals(values[m]))
                        {
                            fnd = true;
                            locn = j;
                        }

                    }
                    if (parameters.get(m).getValues().size() > nmapMax)
                    {
                        System.out.println(
                                "Maximum parameter values exceeded for parameter " + parameters.get(m).getName() + "="
                                + parameters.get(m).getValues().size() + " values." + "\n"
                                          );

                        return ERR;
                        // nmapMax = parameters[m].getValues().size();
                    }
                    else
                    {// if matching value not in map array, add it and
                        // save index

                        if (!fnd)
                        {
                            boolean knowAllValues = false;
                            for (Tway ob : tway_objects)
                            {
                                if (ob != null)
                                {
                                    knowAllValues = true;
                                }
                            }
                            if (!knowAllValues)
                            {
                                map[m][j] = values[m];
                                locn = j;
                            }
                            else
                            {
                                System.out.println("Invalid value: " + values[m] + " in parameter " + m + "\n");
                                return -1;
                            }

                        }
                        test[i][m] = locn;
                    }
                }
                else
                {

                    if (rng[m])
                    {
                        // range value, set test[i][m] to input value's interval
                        // in range
                        // test[i][m] = r where boundary[r-1] <= v1 <
                        // boundary[r]
                        double v1 = 0;
                        try
                        { // read in continuous valued variable value
                            v1 = Double.parseDouble(values[m]);
                        }
                        catch (Exception ex)
                        {
                            System.out
                                    .println("Invalid value for parameter " + m + ".  Value = " + values[m]);
                            return ERR;
                        }
                        try
                        {
                            Boolean fnd = false;

                            // nbnds = 4;
                            for (int k = 0; k < nvals[m] - 1 && k < nbnds && !fnd; k++)
                            {
                                if (v1 < bnd[m][k])
                                {
                                    test[i][m] = k;
                                    fnd = true;
                                }
                            }
                            if (!fnd)
                            {
                                test[i][m] = nvals[m] - 1; // value > last
                                // boundary
                            }

                        }
                        catch (Exception ex)
                        {
                            System.out.println("Problem with file or parameter/value specs in setupfile.");
                            return ERR;
                        }
                    }

                    if (grp[m])
                    {
                        Object v1;
                        try
                        {

                            v1 = values[m];
                        }
                        catch (Exception ex)
                        {
                            System.out
                                    .println("Invalid value for parameter " + m + ". Value = " + values[m]);
                            return 2;
                        }
                        try
                        {
                            Boolean fnd = false;
                            for (int k = 0; k < nvals[m] && !fnd; k++)
                            {
                                String[] vals = group[m][k].toString().split(",");
                                for (String val : vals)
                                {

                                    if (val.equals(v1))
                                    {
                                        test[i][m] = k;
                                        fnd = true;
                                    }
                                }

                            }

                        }
                        catch (Exception ex)
                        {
                            System.out.println("Problem with file or parameter/value specs in setupfile.");
                            return ERR;
                        }

                    }

                }
            }
            i++;
        }

        if (!ACTSfilePresent)
        {
            int j;
            for (i = 0; i < ncols; i++)
            { // set up number of
                // values for
                // automatically
                // detected parms
                if (!rng[i] && !grp[i])
                { // count how many value mappings used
                    for (j = 0; j < nmapMax && map[i][j] != null; j++)

                    {
                        nvals[i] = j; // j = # of value mappings have been
                    }
                    // established
                }
            }
        }

        return 0;

    }

	/*
	 * ==============================END OF SETUP FILE
	 * FUNCTION================================================
	 */
    // }

    /*
	 * Tway function which handles alot of the heavy lifting...
	 * 
	 */
    public static void Tway(final String t_way)
    {

        int max = 0;
        int tway_index = 0;
        switch (t_way)
        {
            case "2way":
                tway_index = 0;
                max = ncols - 1;
                break;
            case "3way":
                tway_index = 1;
                max = ncols - 2;
                break;
            case "4way":
                tway_index = 2;
                max = ncols - 3;
                break;
            case "5way":
                tway_index = 3;
                max = ncols - 4;
                break;
            case "6way":
                tway_index = 4;
                max = ncols - 5;
                break;

        }

        if (!ValidateTway(t_way))
        {
            return;
        }

        final int temp_max = max;
        final int tIndex = tway_index;
        Thread way = new Thread()
        {
            @Override
            public void run()
            {

                Long timeCons1 = System.currentTimeMillis();

                Long timeCons2 = System.currentTimeMillis();

                Long timeway1 = System.currentTimeMillis();

                if (tway_objects[tIndex] == null)
                {
                    tway_objects[tIndex] = new Tway(
                            t_way, 0, temp_max, test, nvals, nrows, ncols, parameters,
                            constraints, map
                    );
                }
                else
                {

                    switch (t_way)
                    {
                        case "2way":
                            // fix start and end here...
                            tway_objects[tIndex].updateTwoWay(nrows - 1, nrows, test, 0, temp_max);
                            break;
                        case "3way":
                            tway_objects[tIndex].updateThreeWay(nrows - 1, nrows, test, 0, temp_max);
                            break;
                        case "4way":
                            tway_objects[tIndex].updateFourWay(nrows - 1, nrows, test, 0, temp_max);
                            break;
                        case "5way":
                            tway_objects[tIndex].updateFiveWay(nrows - 1, nrows, test, 0, temp_max);
                            break;
                        case "6way":
                            tway_objects[tIndex].updateSixWay(nrows - 1, nrows, test, 0, temp_max);
                            break;
                    }

                }

                // address later with parallel processing
                tway_objects[tIndex].set_Parallel(parallel);
                tway_objects[tIndex].set_bnd(bnd);
                tway_objects[tIndex].set_Rng(rng);
                tway_objects[tIndex].set_group(group);
                tway_objects[tIndex].set_grp(grp);

				/*
				 * For generating missing tests... incorporate later...
				 * 
				 */
                if (generateMissing)
                {
                    tway_objects[tIndex].set_appendTests(appendTests);
                    tway_objects[tIndex].set_GenTests(generateMissing);
                    tway_objects[tIndex].set_FileNameMissing(missingCombinationsFilePath);
                    tway_objects[tIndex].set_appendFile(tests_input_file_path);
                    tway_objects[tIndex].set_rptMissingCom(false);
                    tway_objects[tIndex].set_minCov(minCov);
                    tway_objects[tIndex].set_NGenTests(10000);
                    tway_objects[tIndex].set_map(map);
                    if (paramNames)
                    {
                        tway_objects[tIndex].set_parmName(1);
                    }
                    else
                    {
                        tway_objects[tIndex].set_parmName(0);
                    }

                    // if (rptMissingCom.isSelected())
                    // way.set_FileNameReport(fileReport.getPath());

                }

                // End of generating missing tests
                ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
                pool.invoke(tway_objects[tIndex]);

                Long timeway2 = System.currentTimeMillis();
                Long timewaytotal = timeway2 - timeway1;

                Long timeConsTotal = timeCons2 - timeCons1;

                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSSSSS");
                df.setTimeZone(TimeZone.getTimeZone("GMT+0"));

                String results = "";

                aInvalidComb = tway_objects[tIndex].get_InvalidComb();
                aInvalidNotIn = tway_objects[tIndex].get_InvalidNotin();

				/*
				 * Print invalid combinations
				 */

                if (!initial_complete[tIndex])
                {
                    System.out.println("\n" + t_way + " invalid combinations: ");
                    for (String[][] str : aInvalidComb)
                    {
                        for (String[] aStr : str)
                        {
                            System.out.print(aStr[0] + " = " + aStr[1] + " ; ");
                        }
                        System.out.println();
                    }

                }
                synchronized (results)
                {

                    switch (t_way)
                    {
                        case "2way":

                            hm_colors2 = tway_objects[tIndex].hm_colors2;

                            results = graph2way(
                                    tway_objects[tIndex]._n_tot_tway_cov, tway_objects[tIndex]._varvalStatN,
                                    tway_objects[tIndex]._nComs, tway_objects[tIndex]._tot_varvalconfig, results,
                                    timeConsTotal, timewaytotal, aInvalidComb.size(), aInvalidNotIn.size()
                                               );
                            initial_complete[tIndex] = true;
                            // FillInvalidDataTable(2);
                            break;
                        case "3way":
                            // hm_colors3 = way.hm_colors3;
                            results = graph3way(
                                    tway_objects[tIndex]._n_tot_tway_cov, tway_objects[tIndex]._varvalStatN,
                                    tway_objects[tIndex]._nComs, tway_objects[tIndex]._tot_varvalconfig, results,
                                    timeConsTotal, timewaytotal, aInvalidComb.size(), aInvalidNotIn.size()
                                               );
                            initial_complete[tIndex] = true;
                            // FillInvalidDataTable(3);
                            break;
                        case "4way":

                            results = graph4way(
                                    tway_objects[tIndex]._n_tot_tway_cov, tway_objects[tIndex]._varvalStatN,
                                    tway_objects[tIndex]._nComs, tway_objects[tIndex]._tot_varvalconfig, results,
                                    timeConsTotal, timewaytotal, aInvalidComb.size(), aInvalidNotIn.size()
                                               );
                            initial_complete[tIndex] = true;
                            // FillInvalidDataTable(4);
                            break;
                        case "5way":

                            results = graph5way(
                                    tway_objects[tIndex]._n_tot_tway_cov, tway_objects[tIndex]._varvalStatN,
                                    tway_objects[tIndex]._nComs, tway_objects[tIndex]._tot_varvalconfig, results,
                                    timeConsTotal, timewaytotal, aInvalidComb.size(), aInvalidNotIn.size()
                                               );
                            initial_complete[tIndex] = true;
                            // FillInvalidDataTable(5);
                            break;
                        case "6way":

                            results = graph6way(
                                    tway_objects[tIndex]._n_tot_tway_cov, tway_objects[tIndex]._varvalStatN,
                                    tway_objects[tIndex]._nComs, tway_objects[tIndex]._tot_varvalconfig, results,
                                    timeConsTotal, timewaytotal, aInvalidComb.size(), aInvalidNotIn.size()
                                               );
                            initial_complete[tIndex] = true;
                            // FillInvalidDataTable(6);
                            break;

                    }
                }

                if (!stepchart && !barchart && !heatmap)
                {
                    if (mode_realtime)
                    {
                        System.out.println(
                                "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                          );
                        for (int i = 0; i < 5; i++)
                        {
                            if (tway_objects[i] != null)
                            {
                                if (initial_complete[i])
                                {
                                    System.out.println(
                                            real_time_cmd_results[i] + " | Measuring " + tway_threads[i] + " new tests"
                                                      );
                                }
                            }
                        }
                        System.out.println("Tests in Queue: " + real_time_buffer_size);
                    }
                }

            }
        };

        way.start();
    }

    private static boolean ValidateTway(String tway)
    {

        switch (tway)
        {
            case "2way":
                if (ncols < 2)
                {
                    System.out.println("Cannot compute 2-way coverage for less than 2 parameters");
                    return false;
                }
                break;
            case "3way":
                if (ncols < 3)
                {
                    System.out.println("Cannot compute 3-way coverage for less than 3 parameters");
                    return false;
                }
                break;
            case "4way":
                if (ncols < 4)
                {
                    System.out.println("Cannot compute 4-way coverage for less than 4 parameters");
                    return false;
                }
                break;
            case "5way":
                if (ncols < 5)
                {
                    System.out.println("Cannot compute 5-way coverage for less than 5 parameters");
                    return false;
                }
                break;
            case "6way":
                if (ncols < 6)
                {
                    System.out.println("Cannot compute 6-way coverage for less than 6 parameters");
                    return false;
                }
                break;
        }

        return true;
    }

	/*
	 * =========================================================================
	 * ================================== For calculating and graphing the
	 * various t-way coverages
	 * =========================================================================
	 * ==================================
	 * 
	 */

    private static String graph2way(long n_tot_tway_cov, long[] varvalStats2, long nComs, long tot_varvalconfigs2,
                                    String results, long tc, long tw, long InvalidIn, long InvalidNot)
    {
        // ======= display summary statistics ==================

        TotCov2way = ((double) n_tot_tway_cov / (double) tot_varvalconfigs2);

		/*
		 * This is for real time combinatorial measurement updates.
		 */
        if (!chart_data.getSeries().isEmpty())
        {
            for (int p = 0; p < chart_data.getSeriesCount(); p++)
            {
                if (chart_data.getSeriesKey(p).equals("2way"))
                {
                    chart_data.removeSeries(p);
                }
            }
        }

        if (barchart)
        {
            ColumnChart("2-way", TotCov2way);
        }
        results = "";

        try
        {

            XYSeries series = new XYSeries("2way", false, true);

            for (int b = NBINS; b >= 0; b--) // drk141007
            {

                double tmpf = (double) (varvalStats2[b]) / (double) (nComs);

                double tmpfx = (double) b / (double) (NBINS);

                String tmps = String.format("Cov >= %.2f = %s/%s = %.5f", tmpfx, varvalStats2[b], nComs, tmpf);

                results += tmps;
                series.add(tmpfx, tmpf);

                results += "\n";
            }

            report[0] = results;

            if (!initial_complete[0])
            {
                synchronized (report)
                {
                    System.out.println(
                            "\n2-way Coverage Results:\n" + "Total 2-way coverage: "
                            + String.format("%.5f", TotCov2way) + "\n" + results
                                      );
                    initial_coverage[0] = TotCov2way;
                }
            }
            else
            {
                // System.out.println("Total 2-way coverage: " +
                // String.format("%.5f", TotCov2way));
                real_time_cmd_results[0] = ("Total 2-way coverage: " + String.format("%.5f", TotCov2way));
            }

            chart_data.setIntervalWidth(1.00);

            chart_data.addSeries(series);
            if (stepchart)
            {
                StepChart(series);
            }

            // point chart

            if (!heatmap)
            {
                tway_threads[0]--;
                return results;
            }

            if (ncols > 100)
            {
                System.out.println("Heat mat will not shown. Max of 100 parameters for heat map.");
            }
            else
            {
                XYSeries red = new XYSeries("red", false, true);
                XYSeries orange = new XYSeries("orange", false, true);
                XYSeries yellow = new XYSeries("yellow", false, true);
                XYSeries green = new XYSeries("green", false, true);
                XYSeries blue = new XYSeries("blue", false, true);

                for (int i = 0; i < ncols - 1; i++)
                {
                    for (int j = i + 1; j < ncols; j++)
                    {
                        if (hm_colors2[i][j] == 0)
                        {
                            red.add(i, j);
                        }

                        if (hm_colors2[i][j] == 1)
                        {
                            orange.add(i, j);
                        }
                        if (hm_colors2[i][j] == 2)
                        {
                            yellow.add(i, j);
                        }
                        if (hm_colors2[i][j] == 3)
                        {
                            green.add(i, j);
                        }
                        else
                        {
                            blue.add(i, j);
                        }

                    }
                }

                final XYSeriesCollection data2 = new XYSeriesCollection();
                data2.removeAllSeries();

                data2.addSeries(red);
                data2.addSeries(orange);
                data2.addSeries(yellow);
                data2.addSeries(green);
                data2.addSeries(blue);

                final JFreeChart my_chart = ChartFactory.createScatterPlot(
                        "", "", "", data2, PlotOrientation.VERTICAL,
                        true, false, false
                                                                          );

                XYPlot plot = (XYPlot) my_chart.getPlot();
                LegendItemCollection chartLegend = new LegendItemCollection();

                Shape shape = new Rectangle(10, 10);
                chartLegend.add(new LegendItem("0-20", null, null, null, shape, Color.RED));
                chartLegend.add(new LegendItem("20-40", null, null, null, shape, Color.ORANGE));
                chartLegend.add(new LegendItem("40-60", null, null, null, shape, Color.YELLOW));
                chartLegend.add(new LegendItem("60-80", null, null, null, shape, Color.GREEN));
                chartLegend.add(new LegendItem("80-100", null, null, null, shape, Color.BLUE));
                plot.setFixedLegendItems(chartLegend);
                plot.getRenderer().setSeriesPaint(data2.indexOf("red"), Color.red);
                plot.getRenderer().setSeriesPaint(data2.indexOf("orange"), Color.orange);
                plot.getRenderer().setSeriesPaint(data2.indexOf("yellow"), Color.yellow);
                plot.getRenderer().setSeriesPaint(data2.indexOf("green"), new Color(34, 177, 76));
                plot.getRenderer().setSeriesPaint(data2.indexOf("blue"), Color.blue);

                Shape cross = ShapeUtilities.createRegularCross(
                        lblPointChart.getWidth() / ncols,
                        lblPointChart.getWidth() / ncols
                                                               );

                plot.getRenderer().setSeriesShape(data2.indexOf("red"), cross);
                plot.getRenderer().setSeriesShape(data2.indexOf("orange"), cross);
                plot.getRenderer().setSeriesShape(data2.indexOf("yellow"), cross);
                plot.getRenderer().setSeriesShape(data2.indexOf("green"), cross);
                plot.getRenderer().setSeriesShape(data2.indexOf("blue"), cross);

                plot.setBackgroundPaint(new Color(255, 255, 196));

                final NumberAxis domainAxis = new NumberAxis("");

                domainAxis.setRange(0.00, ncols);
                domainAxis.setTickUnit(new NumberTickUnit(ncols > 10 ? 10 : 1));

                final NumberAxis rangeAxis = new NumberAxis("");
                rangeAxis.setRange(0, ncols);

                rangeAxis.setVisible(false);

                plot.setDomainAxis(0, domainAxis);
                plot.setRangeAxis(0, rangeAxis);

                plot.setDomainCrosshairVisible(true);
                plot.setRangeCrosshairVisible(true);

                BufferedImage image = my_chart.createBufferedImage(500, 400);
                ImageIcon imagen = new ImageIcon(image);

                lblPointChart.setIcon(imagen);
                lblPointChart.repaint();
                pointChartPanel.add(lblPointChart, BorderLayout.SOUTH);

                frame.add(pointChartPanel, BorderLayout.CENTER);
                frame.pack();
                if (!stepchart && !barchart)
                {
                    if (mode_realtime)
                    {
                        System.out.println(
                                "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                          );
                        for (int i = 0; i < 5; i++)
                        {
                            if (tway_objects[i] != null)
                            {
                                if (initial_complete[i])
                                {
                                    System.out.println(
                                            real_time_cmd_results[i] + " | Measuring " + tway_threads[i] + " new tests"
                                                      );
                                }
                            }
                        }
                        System.out.println("Tests in Queue: " + real_time_buffer_size);
                    }
                    frame.setVisible(true);

                }


            }

        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());

        }
        tway_threads[0]--;
        initial_complete[0] = true;

        return results;

    }

	/*
	 * =============================================== END GRAPH 2 - WAY
	 * ===============================================
	 *
	 */

	/*
	 * =============================================== GRAPH 3 - WAY
	 * ==============================================
	 * 
	 */

    private static String graph3way(long n_tot_tway_cov, long[] varvalStats3, long nComs, long tot_varvalconfigs3,
                                    String results, long tc, long tw, long invalidIn, long invalidNot) // drk121109
    {
        // ======= display summary statistics ==================
        TotCov3way = ((double) n_tot_tway_cov / (double) tot_varvalconfigs3);

		/*
		 * This is for real time combinatorial measurement updates.
		 */
        if (!chart_data.getSeries().isEmpty())
        {
            for (int p = 0; p < chart_data.getSeriesCount(); p++)
            {
                if (chart_data.getSeriesKey(p).equals("3way"))
                {
                    chart_data.removeSeries(p);
                }
            }
        }

        // Add row to dtResults

        if (barchart)
        {
            ColumnChart("3-way", TotCov3way);
        }

        results = "";

        try
        {

            XYSeries series = new XYSeries("3way", false, true);

            // for (int b = 0; b <= NBINS; b++)
            for (int b = NBINS; b >= 0; b--) // drk141007
            {
                double tmpf = (double) varvalStats3[b] / (double) (nComs);
                double tmpfx = (double) b / (double) (NBINS);
                String tmps = String.format("Cov >= %.2f = %s/%s = %.5f", tmpfx, varvalStats3[b], nComs, tmpf);

                results += tmps;

                series.add(tmpfx, tmpf);

                results += "\n";
            }

            report[1] = results;
            if (!initial_complete[1])
            {
                synchronized (report)
                {
                    System.out.println(
                            "\n3-way Coverage Results:\n" + "Total 3-way coverage: "
                            + String.format("%.5f", TotCov3way) + "\n" + results
                                      );
                    initial_coverage[1] = TotCov3way;

                }
            }
            else
            {
                // System.out.println("Total 3-way coverage: " +
                // String.format("%.5f", TotCov3way));
                real_time_cmd_results[1] = ("Total 3-way coverage: " + String.format("%.5f", TotCov3way));
            }

            // if (chart_data.getSeriesIndex(series.getKey()) > -1) {
            // chart_data.removeSeries(chart_data.getSeriesIndex(series.getKey()));
            // }

            chart_data.setIntervalWidth(1.00);

            // XYSeriesCollection s = (XYSeriesCollection) chart_data.clone();

            // chart_data.removeAllSeries();

            chart_data.addSeries(series);

            if (stepchart)
            {
                StepChart(series);
            }
			/*
			 * if (s.getSeriesIndex("6way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("6way"));
			 * chart_data.addSeries(serie); StepChart(serie); }
			 * 
			 * if (s.getSeriesIndex("5way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("5way"));
			 * chart_data.addSeries(serie); StepChart(serie); } if
			 * (s.getSeriesIndex("4way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("4way"));
			 * chart_data.addSeries(serie); StepChart(serie); } if
			 * (s.getSeriesIndex("3way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("3way"));
			 * chart_data.addSeries(serie); StepChart(serie); }
			 * 
			 * if (s.getSeriesIndex("2way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("2way"));
			 * chart_data.addSeries(serie); StepChart(serie); }
			 */
            tway_threads[1]--;

        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }

        return results;

    }
	/*
	 * ============================================================== END OF
	 * GRAPH 3-WAY
	 * ==============================================================
	 */

	/*
	 * 
	 * =============================================================== GRAPH
	 * 4-WAY ===============================================================
	 */

    private static String graph4way(long n_tot_tway_cov, long[] varvalStats4, long nComs, long tot_varvalconfigs4,
                                    String results, long tc, long tw, long invalidIn, long invalidNot) // drk121109
    {

        // ======= display summary statistics ==================
        TotCov4way = ((double) n_tot_tway_cov / (double) tot_varvalconfigs4);

        if (!chart_data.getSeries().isEmpty())
        {
            for (int p = 0; p < chart_data.getSeriesCount(); p++)
            {
                if (chart_data.getSeriesKey(p).equals("4way"))
                {
                    chart_data.removeSeries(p);
                }
            }
        }

        // Add row to dtResults
        if (barchart)
        {
            ColumnChart("4-way", TotCov4way);
        }

        results = "";

        try
        {

            XYSeries series = new XYSeries("4way", false, true);

            for (int b = NBINS; b >= 0; b--)
            {
                double tmpf = (double) varvalStats4[b] / (double) (nComs);
                double tmpfx = (double) b / (double) (NBINS);
                String tmps = String.format("Cov >= %.2f = %s/%s = %.5f", tmpfx, varvalStats4[b], nComs, tmpf);

                results += tmps;

                series.add(tmpfx, tmpf);

                results += "\n";
            }
            report[2] = results;
            if (!initial_complete[2])
            {
                synchronized (report)
                {
                    System.out.println(
                            "\n4-way Coverage Results:\n" + "Total 4-way coverage: "
                            + String.format("%.5f", TotCov4way) + "\n" + results
                                      );
                    initial_coverage[2] = TotCov4way;

                }
            }
            else
            {
                // System.out.println("Total 4-way coverage: " +
                // String.format("%.5f", TotCov4way));
                real_time_cmd_results[2] = ("Total 4-way coverage: " + String.format("%.5f", TotCov4way));
            }

            // if (chart_data.getSeriesIndex(series.getKey()) > -1) {
            // chart_data.removeSeries(chart_data.getSeriesIndex(series.getKey()));
            // }

            chart_data.setIntervalWidth(1.00);

            // XYSeriesCollection s = (XYSeriesCollection) chart_data.clone();

            // chart_data.removeAllSeries();

            chart_data.addSeries(series);

            if (stepchart)
            {
                StepChart(series);
            }
			/*
			 * if (s.getSeriesIndex("6way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("6way"));
			 * chart_data.addSeries(serie); StepChart(serie); }
			 * 
			 * if (s.getSeriesIndex("5way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("5way"));
			 * chart_data.addSeries(serie); StepChart(serie); } if
			 * (s.getSeriesIndex("4way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("4way"));
			 * chart_data.addSeries(serie); StepChart(serie); } if
			 * (s.getSeriesIndex("3way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("3way"));
			 * chart_data.addSeries(serie); StepChart(serie); }
			 * 
			 * if (s.getSeriesIndex("2way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("2way"));
			 * chart_data.addSeries(serie); StepChart(serie); }
			 */

        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        tway_threads[2]--;

        return results;
    }

	/*
	 * ========================================================================
	 * END GRAPH 4-WAY
	 * =======================================================================
	 * 
	 */

	/*
	 * ========================================================================
	 * GRAPH 5-WAY
	 * ========================================================================
	 */

    private static String graph5way(long n_tot_tway_cov, long[] varvalStats5, long nComs, long tot_varvalconfigs5,
                                    String results, long tc, long tw, long invalidIn, long invalidNot)
    {

        // ======= display summary statistics ==================
        TotCov5way = ((double) n_tot_tway_cov / (double) tot_varvalconfigs5);

        if (!chart_data.getSeries().isEmpty())
        {
            for (int p = 0; p < chart_data.getSeriesCount(); p++)
            {
                if (chart_data.getSeriesKey(p).equals("5way"))
                {
                    chart_data.removeSeries(p);
                }
            }
        }

        if (barchart)
        {
            ColumnChart("5-way", TotCov5way);
        }

        results = "";

        try
        {

            XYSeries series = new XYSeries("5way", false, true);

            for (int b = NBINS; b >= 0; b--)
            {
                double tmpf = (double) varvalStats5[b] / (double) nComs;
                double tmpfx = (double) b / (double) (NBINS);
                String tmps = String.format("Cov >= %.2f = %s/%s = %.5f", tmpfx, varvalStats5[b], nComs, tmpf);

                results += tmps;

                series.add(tmpfx, tmpf);

                results += "\n";
            }
            report[3] = results;
            if (!initial_complete[3])
            {
                synchronized (report)
                {
                    System.out.println(
                            "\n5-way Coverage Results:\n" + "Total 5-way coverage: "
                            + String.format("%.5f", TotCov5way) + "\n" + results
                                      );
                    initial_coverage[3] = TotCov5way;

                }
            }
            else
            {
                // System.out.println("Total 5-way coverage: " +
                // String.format("%.5f", TotCov5way));
                real_time_cmd_results[3] = ("Total 5-way coverage: " + String.format("%.5f", TotCov5way));
            }

            // if (chart_data.getSeriesIndex(series.getKey()) > -1) {

            // chart_data.removeSeries(chart_data.getSeriesIndex(series.getKey()));
            // }

            chart_data.setIntervalWidth(1.00);

            // XYSeriesCollection s = (XYSeriesCollection) chart_data.clone();

            // chart_data.removeAllSeries();

            chart_data.addSeries(series);

            if (stepchart)
            {
                StepChart(series);
            }
			/*
			 * if (s.getSeriesIndex("6way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("6way"));
			 * chart_data.addSeries(serie); StepChart(serie); }
			 * 
			 * if (s.getSeriesIndex("5way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("5way"));
			 * chart_data.addSeries(serie); StepChart(serie); } if
			 * (s.getSeriesIndex("4way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("4way"));
			 * chart_data.addSeries(serie); StepChart(serie); } if
			 * (s.getSeriesIndex("3way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("3way"));
			 * chart_data.addSeries(serie); StepChart(serie); }
			 * 
			 * if (s.getSeriesIndex("2way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("2way"));
			 * chart_data.addSeries(serie); StepChart(serie); }
			 * 
			 */
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        tway_threads[3]--;

        return results;
    }

	/*
	 * ================================================================ END OF
	 * GRAPH 5-WAY
	 * ================================================================
	 */

	/*
	 * ================================================================= GRAPH
	 * 6-WAY =================================================================
	 */

    private static String graph6way(long n_tot_tway_cov, long[] varvalStats6, long nComs, long tot_varvalconfigs6,
                                    String results, long tc, long tw, long invalidIn, long invalidNot) // drk121109
    {

        // ======= display summary statistics ==================
        TotCov6way = ((double) n_tot_tway_cov / (double) tot_varvalconfigs6);

        if (!chart_data.getSeries().isEmpty())
        {
            for (int p = 0; p < chart_data.getSeriesCount(); p++)
            {
                if (chart_data.getSeriesKey(p).equals("6way"))
                {
                    chart_data.removeSeries(p);
                }
            }
        }

        if (barchart)
        {
            ColumnChart("6-way", TotCov6way);
        }

        results = "";

        try
        {

            XYSeries series = new XYSeries("6way", false, true);

            for (int b = NBINS; b >= 0; b--)
            {
                double tmpf = (double) varvalStats6[b] / (double) nComs;
                double tmpfx = (double) b / (double) (NBINS);
                String tmps = String.format("Cov >= %.2f = %s/%s = %.5f", tmpfx, varvalStats6[b], nComs, tmpf);

                results += tmps;

                series.add(tmpfx, tmpf);

                results += "\n";
            }
            report[4] = results;
            if (!initial_complete[4])
            {
                synchronized (report)
                {
                    System.out.println(
                            "\n6-way Coverage Results:\n" + "Total 6-way coverage: "
                            + String.format("%.5f", TotCov6way) + "\n" + results
                                      );
                    initial_coverage[4] = TotCov6way;

                }
            }
            else
            {
                // System.out.println("Total 6-way coverage: " +
                // String.format("%.5f", TotCov6way));
                real_time_cmd_results[4] = ("Total 6-way coverage: " + String.format("%.5f", TotCov6way));
            }

            // if (chart_data.getSeriesIndex(series.getKey()) > -1) {
            // chart_data.removeSeries(chart_data.getSeriesIndex(series.getKey()));
            // }

            chart_data.setIntervalWidth(1.00);

            // XYSeriesCollection s = (XYSeriesCollection) chart_data.clone();

            // chart_data.removeAllSeries();

            chart_data.addSeries(series);

            if (stepchart)
            {
                StepChart(series);
            }
			/*
			 * if (s.getSeriesIndex("6way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("6way"));
			 * chart_data.addSeries(serie); StepChart(serie); }
			 * 
			 * if (s.getSeriesIndex("5way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("5way"));
			 * chart_data.addSeries(serie); StepChart(serie); } if
			 * (s.getSeriesIndex("4way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("4way"));
			 * chart_data.addSeries(serie); StepChart(serie); } if
			 * (s.getSeriesIndex("3way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("3way"));
			 * chart_data.addSeries(serie); StepChart(serie); }
			 * 
			 * if (s.getSeriesIndex("2way") > -1) { XYSeries serie =
			 * s.getSeries(s.getSeriesIndex("2way"));
			 * chart_data.addSeries(serie); StepChart(serie); }
			 */
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        tway_threads[4]--;
        return results;
    }

	/*
	 * ========================================================================
	 * END OF GRAPH 6-WAY
	 * ========================================================================
	 */

	/*
	 * ================================================= PLOTTING THE STEP CHART
	 * =================================================
	 */

	/*
	 * ========================================================================
	 * END OF GRAPH 6-WAY
	 * ========================================================================
	 */

	/*
	 * ================================================= PLOTTING THE STEP CHART
	 * =================================================
	 */

    protected static void StepChart(XYSeries serie)
    {

        chart = ChartFactory.createXYStepChart(
                "", "Coverage", "Combinations", chart_data, PlotOrientation.HORIZONTAL,
                true, false, false
                                              );

        // LegendTitle legend = chart.getLegend();
        // legend.setPosition(RectangleEdge.RIGHT);
        // legend.setVisible(false);
        // chart.removeLegend();

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainAxis(0, new NumberAxis("Combinations"));
        plot.setRangeAxis(0, new NumberAxis("Coverage"));

        // plot.setRenderer(new XYStepAreaRenderer()); //FILL UNDER THE CURVE

        if (chart_data.indexOf("2way") >= 0)
        {
            plot.getRenderer().setSeriesPaint(chart_data.indexOf("2way"), new Color(237, 28, 36));
            plot.getRenderer().setSeriesStroke(chart_data.indexOf("2way"), new BasicStroke(3.0f));
        }

        if (chart_data.indexOf("3way") >= 0)
        {
            plot.getRenderer().setSeriesPaint(chart_data.indexOf("3way"), new Color(63, 72, 204));
            plot.getRenderer().setSeriesStroke(
                    chart_data.indexOf("3way"), new BasicStroke(
                            3.0f, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_MITER, 1.0f, new float[]{6.0f, 6.0f}, 0.0f
                    )
                                              );

        }

        if (chart_data.indexOf("4way") >= 0)
        {
            plot.getRenderer().setSeriesPaint(chart_data.indexOf("4way"), new Color(34, 177, 76));
            plot.getRenderer().setSeriesStroke(
                    chart_data.indexOf("4way"), new BasicStroke(
                            3.0f, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_ROUND, 1.0f, new float[]{10.0f, 3.0f, 2.0f, 3.0f}, 0.0f
                    )
                                              );
        }
        if (chart_data.indexOf("5way") >= 0)
        {
            plot.getRenderer().setSeriesPaint(chart_data.indexOf("5way"), new Color(210, 105, 30));
            plot.getRenderer().setSeriesStroke(
                    chart_data.indexOf("5way"), new BasicStroke(
                            3.0f, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_ROUND, 1.0f, new float[]{10.0f, 6.0f, 2.0f, 6.0f, 2.0f, 10.0f}, 0.0f
                    )
                                              );
        }
        if (chart_data.indexOf("6way") >= 0)
        {
            plot.getRenderer().setSeriesPaint(chart_data.indexOf("6way"), new Color(139, 0, 139));
            plot.getRenderer().setSeriesStroke(
                    chart_data.indexOf("6way"), new BasicStroke(
                            3.0f, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_ROUND, 1.0f, new float[]{4.0f, 4.0f, 4.0f, 4.0f, 4.0f}, 2.0f
                    )
                                              );
        }

		/*
		 * Create Static Legend here...
		 */
        LegendItemCollection chartLegend = new LegendItemCollection();
        LegendItem[] items = new LegendItem[5];
        for (int i = 0; i < plot.getLegendItems().getItemCount(); i++)
        {
            String key = plot.getLegendItems().get(i).getLabel();
            switch (key)
            {
                case "2way":
                    items[0] = plot.getLegendItems().get(i);
                    break;
                case "3way":
                    items[1] = plot.getLegendItems().get(i);
                    break;
                case "4way":
                    items[2] = plot.getLegendItems().get(i);
                    break;
                case "5way":
                    items[3] = plot.getLegendItems().get(i);
                    break;
                case "6way":
                    items[4] = plot.getLegendItems().get(i);
                    break;
            }
        }

        for (LegendItem it : items)
        {
            if (!(it == null))
            {
                chartLegend.add(it);
            }
        }

        plot.setFixedLegendItems(chartLegend);

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.setBackgroundPaint(new Color(255, 255, 196));
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.black);
        plot.setRangeGridlinePaint(Color.black);

        NumberAxis axisY = (NumberAxis) plot.getDomainAxis();

        axisY.setTickUnit(new NumberTickUnit(0.1d));

        axisY.setRange(0D, 1d);

        NumberAxis axisX = (NumberAxis) plot.getRangeAxis();

        axisX.setRange(0D, 1d);
        axisX.setTickUnit(new NumberTickUnit(0.1d));

        BufferedImage image = chart.createBufferedImage(500, 300);
        ImageIcon imagen = new ImageIcon(image);

        lblStepChart.setIcon(imagen);
        lblStepChart.repaint();

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setVisible(true);
        frame.pack();
        frame.setVisible(true);

        if (mode_realtime)
        {
            System.out.println(
                    "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                    + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                              );
            for (int i = 0; i < 5; i++)
            {
                if (tway_objects[i] != null)
                {
                    if (initial_complete[i])
                    {
                        System.out.println(real_time_cmd_results[i] + " | Measuring " + tway_threads[i] + " new tests");
                    }
                }
            }
            System.out.println("Tests in Queue: " + real_time_buffer_size);
        }

    }

	/*
	 * ========================================================= END OF PLOTTING
	 * STEP CHART =========================================================
	 */

	/*
	 * ========================================================= PLOTTING THE
	 * COLUMN CHART =========================================================
	 */

    private static void ColumnChart(String tway, double coverage)
    {

        XYSeries twaySerie = new XYSeries(tway, false, true);

        double x = 0;
        switch (tway)
        {
            case "2-way":
                x = 1;
                break;
            case "3-way":
                x = 2;
                break;
            case "4-way":
                x = 3;
                break;
            case "5-way":
                x = 4;
                break;
            case "6-way":
                x = 5;
                break;
        }
        twaySerie.add(x, (coverage * 10));

        if (bars.indexOf(tway) >= 0)
        {
            bars.removeSeries(bars.indexOf(tway));
        }

        bars.addSeries(twaySerie);

        chartcolumn = ChartFactory.createXYBarChart(
                "", "t-way", false, "% Coverage", bars, PlotOrientation.VERTICAL,
                true, false, false
                                                   );

        // LegendTitle legend = chart.getLegend();
        // legend.setPosition(RectangleEdge.RIGHT);

        chartcolumn.removeLegend();

        XYPlot plot = (XYPlot) chartcolumn.getPlot();
        plot.setDomainAxis(0, new NumberAxis("t-way"));

        plot.setRangeAxis(0, new NumberAxis("% Coverage"));

        if (bars.indexOf("2-way") >= 0)
        {
            plot.getRenderer().setSeriesPaint(bars.indexOf("2-way"), new Color(237, 28, 36));
        }

        if (bars.indexOf("3-way") >= 0)
        {
            plot.getRenderer().setSeriesPaint(bars.indexOf("3-way"), new Color(63, 72, 204));
        }

        if (bars.indexOf("4-way") >= 0)
        {
            plot.getRenderer().setSeriesPaint(bars.indexOf("4-way"), new Color(34, 177, 76));
        }

        if (bars.indexOf("5-way") >= 0)
        {
            plot.getRenderer().setSeriesPaint(bars.indexOf("5-way"), new Color(210, 105, 30));
        }

        if (bars.indexOf("6-way") >= 0)
        {
            plot.getRenderer().setSeriesPaint(bars.indexOf("6-way"), new Color(139, 0, 139));
        }

        SymbolAxis saX = new SymbolAxis("t-ways", new String[]{"", "2-way", "3-way", "4-way", "5-way", "6-way"});

        SymbolAxis saY = new SymbolAxis(
                "% Coverage",
                new String[]{"0%", "10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%", "100%"}
        );

        plot.setDomainAxis(saX);
        plot.setRangeAxis(saY);

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.setBackgroundPaint(new Color(255, 255, 196));
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.black);
        plot.setRangeGridlinePaint(Color.black);

        NumberAxis axisY = (NumberAxis) plot.getRangeAxis();

        axisY.setTickUnit(new NumberTickUnit(1d));

        axisY.setRange(0D, 10d);

        NumberAxis axisX = (NumberAxis) plot.getDomainAxis();

        axisX.setRange(0D, 6d);
        axisX.setTickUnit(new NumberTickUnit(1d));

        BufferedImage image = chartcolumn.createBufferedImage(500, 300);
        ImageIcon imagen = new ImageIcon(image);

        lblColumnChart.setIcon(imagen);
        lblColumnChart.repaint();

        final ChartPanel chartPanel = new ChartPanel(chartcolumn);

        chartPanel.setVisible(true);

        frame.pack();
        if (!stepchart)
        {
            frame.setVisible(true);
            if (mode_realtime)
            {
                System.out.println(
                        "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                        + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                                  );
                for (int i = 0; i < 5; i++)
                {
                    if (tway_objects[i] != null)
                    {
                        if (initial_complete[i])
                        {
                            System.out.println(
                                    real_time_cmd_results[i] + " | Measuring " + tway_threads[i] + " new tests"
                                              );
                        }
                    }
                }
                System.out.println("Tests in Queue: " + real_time_buffer_size);
            }
        }


    }

	/*
	 * ========================================================= END OF PLOTTING
	 * THE COLUMN CHART
	 * =========================================================
	 */

	/*
	 * ========================================================= GENERATING
	 * RANDOM TESTS =========================================================
	 */

    private static void GetRandomTests()
    {

        try
        {

			/*
			 * if (txtInputFileRand.getText().equals("")) {
			 * JOptionPane.showMessageDialog(frame,
			 * "File with parameters isn't loaded!"); return; } if
			 * (txtOutputFileRand.getText().equals("")) {
			 * JOptionPane.showMessageDialog(frame, "Output file isn't set!");
			 * return; } if ((int) spnNoTestRand.getValue() == 0) {
			 * JOptionPane.showMessageDialog(frame,
			 * "Number of tests must be grather than 0!"); return; }
			 */
            // Thread RandomTests = new Thread() {
            // @Override
            // public void run() {
            CSolver solver = new CSolver();

            solver.SetConstraints(constraints); // set constraint to
            // solver model
            solver.SetParameter(parameters); // set parameters to
            // solver
            // model

            solver.SolverRandomTests((int) numberOfRandom, bwRandomTests);

            // if (chkMeasureCoverage.isSelected()) {
            infile = solver.infile();
            nrows = numberOfRandom;
            setupParams(false);
            setupFile(0);
            System.out.println("\n\nAdded randomly generated tests to " + random_path);
            // }
            // };

            // RandomTests.start();

        }
        catch (Exception ex)
        {
            System.out.println("Error solving constraints");
        }

    }

	/*
	 * ========================================================= END OF
	 * GENERATING RANDOM TESTS
	 * =========================================================
	 */

    public static synchronized void increment_progress(int prog_id)
    {
        progress[prog_id]++;
    }

	/*
	 * FOR SET NOTATION
	 * 
	 * This can be changed. It was thrown together fairly quick as a means of
	 * building a constraint from interval notation... It works though sooo,
	 * yeah.
	 * 
	 */

    public static String build_implicit_constraint(String interval, String parameter)
    {
        String constraint = "(";
        int grp_index = 0;
        int side = 0;
        String charbuilder = "";
        int num1 = 0;
        int num2 = 0;
        String sides = "";
        boolean checknums = false;
        boolean nextgroup = false;
        for (char c : interval.toCharArray())
        {
            if (c == '[' || c == '(')
            {
                if (sides.length() == 1)
                {
                    sides += c;
                }
                side = 0;
                continue;
            }

            else if (c == ')' || c == ']')
            {

                side = 1;
                if (nextgroup)
                {
                    nextgroup = false;
                    sides = String.valueOf(c);
                    if (!charbuilder.equals("*"))
                    {
                        num1 = Integer.parseInt(charbuilder);
                    }
                    charbuilder = "";
                }

                continue;
            }

//            if (charbuilder.equals("") && c == ',')
//            {
//            }

            /*else*/
            if (c == ',')
            {
                if (charbuilder.equals("*"))
                {
                    if (side == 0)
                    {
                        constraint += parameter + " = " + String.valueOf(grp_index) + " || ";
                        grp_index++;
                        charbuilder = "";
                        nextgroup = true;
//                        continue;
                    }
                    else
                    {
                        constraint += parameter + " = " + String.valueOf(grp_index) + ")";
//                        continue;
                    }

                }
                else
                {
                    if (charbuilder.equals(""))
                    {
                        continue;
                    }
                    if (side == 0)
                    {
                        if (sides.length() == 2)
                        {
                            num2 = Integer.parseInt(charbuilder);
                            charbuilder = "";
                            switch (sides)
                            {
                                case "][":

                                    if ((num2 - num1) != 1)
                                    {
                                        grp_index++;
                                    }
                                    break;
                                case ")[":
                                    if ((num2 - num1) != 0)
                                    {
                                        grp_index++;
                                    }
                                    break;
                                case "](":
                                    if ((num2 - num1) != 0)
                                    {
                                        grp_index++;
                                    }
                                    break;
                                case ")(":
                                    if ((num2 - num1) != -1)
                                    {
                                        grp_index++;
                                    }
                                    break;
                            }
                            constraint += parameter + " = " + String.valueOf(grp_index) + " || ";
                            grp_index++;
                            sides = "";
                            nextgroup = true;
                            continue;
                        }
                        grp_index++;
                        constraint += parameter + " = " + String.valueOf(grp_index) + " || ";
                        charbuilder = "";
                        nextgroup = true;
                        continue;
                    }
                    num1 = Integer.parseInt(charbuilder);
                    charbuilder = "";
//                    checknums = true;
                }
            }
            else
            {
                charbuilder += c;
            }
        }

        constraint = constraint.substring(0, constraint.lastIndexOf("|") - 2);
        constraint = constraint.trim() + ")";
        return constraint.trim();
    }

}
