package com.nist.ccmcl;

import java.util.Scanner;

public class ReadStandardInputOperation extends ReadOperation
{

    private Scanner input_scanner;

    @Override
    public void readData()
    {
        input_scanner = new Scanner(System.in);
        while (true)
        {
            try
            {
                String inputLine = input_scanner.nextLine();
                if (inputLine.replaceAll("\\s", "").trim().split(",").length != Main.ncols)
                {
                    System.out.println("Incorrect number of parameters...");
                    continue;
                }
                produce(inputLine);
            }
            catch (Exception ex)
            {
                //do nothing for now...
            }

        }
    }

}
