# CCMCL - Combinatorial Coverage Measurement Command Line Tool#
A real time combinatorial coverage measurement tool. Based off of [CCM](http://csrc.nist.gov/groups/SNS/acts/download_tools.html#measure), but with enhanced functionality. 

### Supported Operating Systems: ###

- Windows
- Mac OS
- Linux

### Key Features:###

- Measure the combinatorial coverage of static test case files
- Generate missing combinations from given test cases
- Generate random tests
- Measure combinatorial coverage in real time via various input modes
  - Standard Input
  - Output of external programs
  - TCP/IP 
- Specify robust constraints
- Support for equivalence classes and groups via SET notation definitions
- Read ACTS configuration files via `.txt` or `.xml` 


### Preview: ###
![CCMCL Graphs](/../master/Images/ccmcl_graphs.png)

##Classic Mode##

Classic mode is the command line version of the original CCM tool. It provides all the same functionality as the GUI version, but with some extra functionality added in. By default, classic mode is enabled, and gives the user a lot of power in static analysis of test cases. In this section we will explain the various command line parameter options for classic mode of CCMCL. 

###Command Line Arguments Pertaining to Classic Mode:###
To see a list of all the possible command line arguments run:

`java -jar ccmcl.jar  --help`

- `--inputfile (-I)` : [path to test case file (`.txt`, `.csv`)].
- `--ACTSfile (-A)`: [path to `.txt` or `.xml` ACTS file].
- `--constraints (-C)`: [path to `.txt` file containing constraints].
- `--tway (-T)`: [2,3,4,5,6] Any order and any combination of these values.
- `--generate-missing (-G)`: Generates missing combinations not in test file.
  - Must include `-m` and `-o` with this option.
  - Not available for real time mode (`-R`).
- `--minimum-coverage (-m)`: Minimum coverage for generating missing combinations.
- `--output-missing (-o)`: Output path for the missing combinations.
- `--append-tests (-a)`: Appends original tests to missing combinations file.
- `--parameter-names (-P)`: Parameter names are first line of test case file (`-I`).
- `--parallel (-p)`: Puts the program in parallel processing mode.
- `--generate-random (-r)`: Sets the program to generate a random set of inputs.
  - Must include `-n` and `-f` with this option. 
  - Not available in real time mode (`-R`).
- `--number-random (-n)`: Amount of random inputs to generate.
- `--output-random (-f)`: Path to output the random test cases to.
- `--stepchart (-S)`: Generates a step chart displaying t-way coverage.
- `--barchart (-B)`: Generates a bar chart displaying t-way coverage.
- `--heatmap (-H)`: Generates a 2-way coverage heatmap.
- `--display-progress (-d)`: Displays progress of coverage measurement.


### Example: ###
`java -jar ccmcl.jar -I input_test.csv -A ACTSfile.txt -P -T 2,3,4,5,6 -S -H -B`

Below is an explanation for each command line argument and what exactly it is doing:

1. `java -jar ccmcl.jar`
 - This launches the ccmcl.jar program. (Be sure you have Java installed.)
2. `-I input_test.csv`
 - This input parameter specifies that the test cases will come from a comma separated value file.
 - ![input_test.csv image](/../master/Images/input_test.csv.png)
3. `-A ACTSfile.txt`
 - This input parameter specifies that an ACTS configuration file will used to define the input domain. ACTS files can be either `.txt` or `.xml` format.
 - ![ACTSfile image](/../master/Images/ACTSfile.txt.png)
4. `-P`
 - This option simply specifies that in the test case file (`input_test.csv`), the first row is the parameter names. Include this option if this is the case. 
5. `-T 2,3,4,5,6`
 - This option specifies the t-way levels you would like to measure. Notice only the values (2,3,4,5,6) are available. In this case, we selected to measure all of them.
6. `-S`
 - This tells the CCMCL program to display a stepchart of the measured combinatorial coverage.
7. `-B`
 - This tells the CCMCL program to display a barchart of the measured combinatorial coverage.
8. `-H`
 - This tells the CCMCL program to display a heatmap of the 2-way coverage measured. Notice, the heatmap option is only available for 2-way coverage.