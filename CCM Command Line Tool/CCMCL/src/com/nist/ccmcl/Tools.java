package com.nist.ccmcl;

import java.util.Arrays;


public class Tools
{


    //operator for constraints
    public static String[] ArithmeticsOperators = {"+", "-", "*", "%", "/"};
    public static String[] RelationalOperators  = {"!=", ">=", "<=", ">", "<", "="};
    public static String[] BooleanOperators     = {"&&", "||", "=>", "!"};
    public static String[] GroupOperators       = {"(", ")"};


    //know if the string is numeric
    public static Boolean isNumeric(String text)
    {
        //This checks for negative numbers...
        if (text.startsWith("-"))
        {
            text = text.substring(1, text.length());
        }

        for (char c : text.toCharArray())
        {
            if (!Character.isDigit(c))
            {
                return false;
            }
        }
        return true;
    }


    //take off all quotations from a string
    public static String WithoutQuoations(String text)
    {
        text = text.replace("\"", "");
        return text;
    }

    //remove extra quotations
    public static String CleanString(String str)
    {
        String rvalue = "";

        if (str.startsWith("\""))
        {
            str = str.substring(1);
        }
        if (str.endsWith("\""))
        {
            str.substring(0, str.length() - 1);
        }

        Boolean ant = false;

        for (char c : str.toCharArray())
        {
            if (c == '"' && ant == false)
            {
                ant = true;
            }
            else
            {
                rvalue = rvalue + c;
                ant = false;
            }
        }


        return rvalue;
    }

    //find out if parameter a is inside b and c
    public static Boolean InsideBoundary(Double a, Double b, Double c)
    {
        if (a <= b && a > c)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //find out if the element[array] is inside the array[array of arrays]
    public static Boolean findInArray(Object[][] array, Object[] element)
    {
        for (int i = 0; i < array.length; i++)
        {
            if (Arrays.equals(array[i], element))
            {
                return true;
            }

        }
        return false;

    }


}
