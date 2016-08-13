package com.nist.ccmcl;

import choco.Choco;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;

import java.util.ArrayList;
import java.util.LinkedHashSet;


public class Constants
{

    public static final int    TYPE_INVALID    = -1;
    public static final int    TYPE_INT        = 0;
    public static final int    TYPE_STRING     = 1;
    public static final int    TYPE_BOOL       = 2;
    public static final String INT_TYPE        = "int";
    public static final String INTEGER_TYPE    = "integer";
    public static final String BOOL_TYPE       = "bool";
    public static final String BOOLEAN_TYPE    = "boolean";
    public static final String ENUM_TYPE       = "enum";
    static              String SYSTEM_MODIFIED = "Modify";
    static              String PARAM_TYPE_BOOL = "@bool@";
    static              String PARAM_TYPE_INT  = "@int@";
    static              String PARAM_TYPE_ENUM = "@enum@";
    static              String FALSE           = "false";
    static              String TRUE            = "true";
    static              String DIGIT           = "\\d*";
    static              String LETTER          = "\\w*";
    static              String MOD             = "%";
    static              String DIV             = "/";
    static              String MUL             = "*";
    static              String MINUS           = "-";
    static              String PLUS            = "+";
    static              String NOT_str         = "NOT";
    static              String NOT             = "!";
    static              String OR_str          = "OR";
    static              String OR              = "||";
    static              String OR_regex        = "|";
    static              String NE              = "-";
    static              String GE              = ">=";
    static              String GE_STR          = "GE";
    static              String LE              = "<=";
    static              String LE_STR          = "LE";
    static              String LT              = "<";
    static              String GT              = ">";
    static              String EQ2             = "==";
    static              String EQ              = "=";
    static              String AND             = "&&";
    static              String AND_str         = "AND";
    static              String AND_regex       = "&";
    static              String IMP             = "=>";
    static              String IMP_STR         = "\"=>\"";
    static              String NEQ             = "!=";
    static              String NEQ_STR         = "NEQ";
    static              String OPEN_PAR        = "(";
    static              String CLOSE_PAR       = ")";
    static              String BSLASH          = "\\";
    static              String SPACE           = " ";
    static              String DOUBLEQUOTE     = "\"";
    ArrayList<String>        enumList;
    ArrayList<ChocoVariable> chocoVariables;
    private LinkedHashSet<IntegerVariable> varInUse;

    public Constants(ArrayList<String> al_e, LinkedHashSet<IntegerVariable> lh_vu, ArrayList<ChocoVariable> al_cv)
    {
        enumList = al_e;
        chocoVariables = al_cv;
        varInUse = lh_vu;
    }

    public IntegerExpressionVariable mk_enum(String s)
    {
        int v = enumList.indexOf(s);
        if (v < 0)
        {
            enumList.add(s);
            System.out.println("Warning: No domain contains this enum value - " + s);
            return mk_int(enumList.size() - 1);
        }
        return mk_int(v);
    }

    public IntegerVariable mk_int(int v)
    {

        return Choco.constant(v);
    }

    public IntegerVariable mk_int_var(String s)
    {
        for (ChocoVariable cv : chocoVariables)
        {
            if (s.equals(cv.getName()))
            {

                varInUse.add(cv.getVar());
                return cv.getVar();
            }
        }
        return null;
    }

    public IntegerVariable mk_bool_var(String s)
    {
        return mk_int_var(s);
    }

    public IntegerVariable mk_true()
    {

        return Choco.constant(1);
    }

    public IntegerVariable mk_false()
    {

        return Choco.constant(0);
    }

    public IntegerExpressionVariable mk_op(String op, IntegerExpressionVariable oprnd1, IntegerExpressionVariable oprnd2)
    {

        if (op.equalsIgnoreCase(PLUS))
        {
            return Choco.plus(oprnd1, oprnd2);
        }
        if (op.equalsIgnoreCase(MUL))
        {
            return Choco.mult(oprnd1, oprnd2);
        }
        if (op.equalsIgnoreCase(MINUS))
        {
            return Choco.minus(oprnd1, oprnd2);
        }
        if (op.equalsIgnoreCase(DIV))
        {
            return Choco.div(oprnd1, oprnd2);
        }
        if (op.equalsIgnoreCase(MOD))
        {
            return Choco.mod(oprnd1, oprnd2);
        }
        return null;
    }

    public Constraint mk_comp(String op, IntegerExpressionVariable oprnd1, IntegerExpressionVariable oprnd2)
    {

        if (op.equalsIgnoreCase(EQ) || op.equalsIgnoreCase(EQ2))
        {
            return Choco.eq(oprnd1, oprnd2);
        }
        if (op.equalsIgnoreCase(NEQ))
        {
            return Choco.neq(oprnd1, oprnd2);
        }
        if (op.equalsIgnoreCase(LT))
        {
            return Choco.lt(oprnd1, oprnd2);
        }
        if (op.equalsIgnoreCase(LE))
        {
            return Choco.leq(oprnd1, oprnd2);
        }
        if (op.equalsIgnoreCase(GT))
        {
            return Choco.gt(oprnd1, oprnd2);
        }
        if (op.equalsIgnoreCase(GE))
        {
            return Choco.geq(oprnd1, oprnd2);
        }
        return null;
    }

    public Constraint mk_cst(String op, Constraint oprnd1, Constraint oprnd2)
    {

        if (op.equalsIgnoreCase(NOT))
        {
            return Choco.not(oprnd2);
        }
        if (op.equalsIgnoreCase(AND))
        {
            return Choco.and(oprnd1, oprnd2);
        }
        if (op.equalsIgnoreCase(OR))
        {
            return Choco.or(oprnd1, oprnd2);
        }
        if (op.equalsIgnoreCase(IMP))
        {
            return Choco.implies(oprnd1, oprnd2);
        }
        return null;
    }
}