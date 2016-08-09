package com.nist.ccmcl;

import java.util.ArrayList;
import java.util.HashMap;


public class ConstraintManager
{

    public static String[] JustParameterValues(String text)
    {


        for (String s : Tools.ArithmeticsOperators)
        {
            text = text.replace(s, "?");
        }
        for (String s : Tools.RelationalOperators)
        {
            text = text.replace(s, "?");
        }
        for (String s : Tools.BooleanOperators)
        {
            text = text.replace(s, "?");
        }

        for (String s : Tools.GroupOperators)
        {
            text = text.replace(s, "?");
        }


        return text.split("\\?");
    }

    public static Boolean isParameter(String text, HashMap parameters)
    {
        if (parameters.containsKey(text))
        {
            return true;
        }
        return false;
    }


    public static Boolean isVariableValue(String text)
    {

        if (text.isEmpty())
        {
            return false;
        }
        if (Tools.isNumeric(text))
        {
            return true;
        }
        if (text.toUpperCase().equals(Boolean.toString(true).toUpperCase()) || text.toUpperCase().equals(
                Boolean.toString(false).toUpperCase()
                                                                                                        ))
        {
            return true;
        }
        if (text.startsWith("\"") && text.endsWith("\""))
        {
            return true;
        }

        return false;
    }

    public static Boolean isValidParameterValue(String text, Parameter anterior)
    {

        ArrayList<String> p = anterior.getValues();

        if (p.contains(Tools.WithoutQuoations(text.trim())))
        {
            return true;
        }
        return false;
    }


}
