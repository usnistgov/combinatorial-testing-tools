package com.nist.ccmcl;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.search.ISolutionPool;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CSolver
{


    ArrayList<ChocoVariable> chocoVariables;
    ArrayList<String>        paraNames;
    List<Parameter>          lp;
    List<meConstraint>       lc;
    CPModel                  m;
    ArrayList                enumList;
    LinkedHashMap            parameters;
    ISolutionPool            solutions;
    String[][]               solutionMa;

    int[][]    resultIndex;
    String[][] resultmapped;
    String[]   _infile;
    int    _countRandomTests = 0;
    JFrame parentFrame       = null;

    public CSolver() //constructor, initialize arrays for parameters and constraints
    {

        lp = new ArrayList<Parameter>();
        lc = new ArrayList<meConstraint>();
    }

    //set list of parameters
    public void SetParameter(List<Parameter> l)
    {
        lp = l;
    }

    public int NoRandomTests()
    {
        return _countRandomTests;
    }

    //set list of constraints
    public void SetConstraints(List<meConstraint> l)
    {
        lc = l;
    }

    public void setFrame(JFrame f)
    {
        parentFrame = f;
    }

    public String[] infile()
    {
        return _infile;
    }

    //get solution by indexes
    public int[][] getSolutionIndex()
    {
        return resultIndex;
    }

    //get solution mapped
    public String[][] getSolutionMapped()
    {
        return resultmapped;
    }


    private void CreateCombinationsVariables(List<String[]> listParams)
    {
        java.util.LinkedHashMap rvalue = new LinkedHashMap();
        LinkedHashSet strings = new LinkedHashSet();
        parameters = new LinkedHashMap();

        chocoVariables = new ArrayList(lp.size());
        paraNames = new ArrayList(lp.size());


        for (Parameter p : lp)
        {
            if (p.getType() == Parameter.PARAM_TYPE_ENUM)//get all enum values from each parameter in a single list
            {
                strings.addAll(p.getValues());

            }
            parameters.put(p.getName(), p); //add all parameters

        }

        enumList = new ArrayList(strings);
        Collections.sort(enumList);


        //load parameters

        for (String[] parm : listParams)
        {

            Parameter p = (Parameter) parameters.get(parm[0]);

            rvalue.put(p.getName(), p);
            paraNames.add(p.getName());// create a list with parameters name

            ArrayList values = new ArrayList();


            // add in the single arrayList, values, all the values from each parameter
            if (p.getType() == Parameter.PARAM_TYPE_INT)// for  int type is the same value
            {
                if (!parm[1].equals(""))
                {
                    values.add(Integer.parseInt(parm[1]));
                }
                else
                {
                    for (String v : p.getValues())
                    {
                        values.add(new java.lang.Integer(v));
                    }

                }

                if (!values.isEmpty())
                {
                    chocoVariables.add(new ChocoVariable(p, values));
                }
            }
            else if (p.getType() == Parameter.PARAM_TYPE_BOOL) // for  bool type, true=1 and false=0
            {
                if (!parm[1].equals(""))
                {
                    if (parm[1].toLowerCase().equals("true"))
                    {
                        values.add(new java.lang.Integer(1));
                    }
                    else
                    {
                        values.add(new java.lang.Integer(0));
                    }
                }
                else
                {
                    values.add(new java.lang.Integer(1));
                    values.add(new java.lang.Integer(0));

                }
                if (!values.isEmpty())
                {
                    chocoVariables.add(new ChocoVariable(p, values));
                }
            }
            else if (p.getType()
                     == Parameter.PARAM_TYPE_ENUM) //for enum type is the index from the general list of enum values created above
            {
                if (!parm[1].equals(""))
                {
                    values.add(enumList.indexOf(parm[1]));
                }
                else
                {
                    for (String v : p.getValues())
                    {
                        values.add(new java.lang.Integer(enumList.indexOf(v)));
                    }
                }
                if (!values.isEmpty())
                {
                    chocoVariables.add(
                            new ChocoVariable(
                                    p, values
                            )
                                      );//add to the chocoVariable list all parameters with their values
                }
            }
        }


        //add all variables to the choco model
        for (ChocoVariable c : chocoVariables)
        {
            m.addVariable(c.getVar());

        }

    }

    //create variables for choco model with parameters
    public void CreateVariables()
    {

        java.util.LinkedHashMap rvalue = new LinkedHashMap();
        LinkedHashSet strings = new LinkedHashSet();
        parameters = new LinkedHashMap();

        chocoVariables = new ArrayList(lp.size());
        paraNames = new ArrayList(lp.size());


        for (Parameter p : lp)
        {

            if (p.getType() == Parameter.PARAM_TYPE_ENUM)//get all enum values from each parameter in a single list
            {
                strings.addAll(p.getValues());

            }
            parameters.put(p.getName(), p); //add all parameters

        }


        enumList = new ArrayList(strings);
        Collections.sort(enumList);

        //load parameters
        for (Parameter p : lp)
        {

            rvalue.put(p.getName(), p);
            paraNames.add(p.getName());// create a list with parameters name

            ArrayList values = new ArrayList();

            // add in the single arrayList, values, all the values from each parameter
            if (p.getType() == Parameter.PARAM_TYPE_INT)// for  int type is the same value
            {


                for (String v : p.getValues())
                {
                    values.add(new java.lang.Integer(v.trim()));
                }

                chocoVariables.add(new ChocoVariable(p, values));
            }
            else if (p.getType() == Parameter.PARAM_TYPE_BOOL) // for  bool type, true=1 and false=0
            {
                values.add(new java.lang.Integer(1));
                values.add(new java.lang.Integer(0));
                chocoVariables.add(new ChocoVariable(p, values));
            }
            else if (p.getType()
                     == Parameter.PARAM_TYPE_ENUM) //for enum type is the index from the general list of enum values created above
            {


                for (String v : p.getValues())
                {
                    values.add(new java.lang.Integer(enumList.indexOf(v)));
                }


                chocoVariables.add(
                        new ChocoVariable(
                                p, values
                        )
                                  );//add to the chocoVariable list all parameters with their values
            }
        }


        //add all variables to the choco model
        for (ChocoVariable c : chocoVariables)
        {
            m.addVariable(c.getVar());

        }

    }


    //create constraints for random tests
    public void CreateConstraintsForRandomTests()
    {
        //CONSTRAINT
        java.util.ArrayList p = new java.util.ArrayList();
        java.util.ArrayList stringConstList = new ArrayList();

        for (meConstraint i : lc)
        {
            stringConstList.add(i.get_cons());  //add all constraints to single ArrayList


            java.util.LinkedHashSet varInUse;

            varInUse = new java.util.LinkedHashSet();

            choco.kernel.model.constraints.Constraint c = null;

            //create parser object
            ConstraintChocoParser parser = new ConstraintChocoParser(
                    i.get_cons(), parameters, new Constants(
                    enumList, varInUse, chocoVariables
            )
            );
            varInUse.clear();
            try
            {
                //change constraint to valid format constraint for choco
                c = parser.parse();

            }
            catch (ParseException e)
            {
                //throw e;
            }
            if (c == null)
            {


                throw new Error();

            }

            m.addConstraint(c);  //add constraint to model
        }
    }

    //create choco constraints for invalid combinations

    public void CreateConstraints(meConstraint cons)
    {
        //CONSTRAINT
        java.util.ArrayList p = new java.util.ArrayList();
        java.util.ArrayList stringConstList = new ArrayList();

        //for (meConstraint i : lc)
        //{
        //   stringConstList.add(i.get_cons());  //add all constraints to single ArrayList


        java.util.LinkedHashSet varInUse;

        varInUse = new java.util.LinkedHashSet();

        choco.kernel.model.constraints.Constraint c = null;

        //create parser object
        ConstraintChocoParser parser = new ConstraintChocoParser(
                cons.get_cons(), parameters, new Constants(
                enumList, varInUse, chocoVariables
        )
        );
        varInUse.clear();
        try
        {
            //change constraint to valid format constraint for choco
            c = parser.parse();
            //obtain all the invalid combinations -> not constraints
            // c = choco.Choco.not(c);
        }
        catch (ParseException e)
        {
            //throw e;
        }
        if (c == null)
        {


            throw new Error();

        }

        m.addConstraint(c);  //add constraint to model

        //}

    }


    public void SolverRandomTests(int NoSolutions, BufferedWriter bw)
    {
        try
        {
            ArrayList<ArrayList<Integer>> solution = new ArrayList<ArrayList<Integer>>();

            m = new CPModel();

            CreateVariables();//crate variable for the model

            CreateConstraintsForRandomTests(); //create chco constraint

            CPSolver s = new CPSolver(); //create object solver

            s.read(m); //read model (contains variables and constraints)
            s.getConfiguration().putInt(Configuration.SOLUTION_POOL_CAPACITY, 1);
            s.setValIntSelector(new RandomIntValSelector());
            s.setRandomSelectors();

            int rootworld = s.getEnvironment().getWorldIndex();
            s.worldPush();

            s.solve();
            int count = 0;

            ISolutionPool pool = s.getSearchStrategy().getSolutionPool();


            ArrayList<Integer> t = new ArrayList<Integer>();
            int value;

            IntegerVariable var;


            String line = "";
            Object[] x;

            _infile = new String[NoSolutions];


            // for (Solution sol:pool.asList())
            //{
            //  t.clear();

            do
            {
                // s.worldPopUntil(rootworld); // restore the original state, where domains were as declared (not yet instantiated)
                //s.worldPush(); // backup the current state of the solver, to allow other solution restoration
                //s.restoreSolution(sol); // restore the solution

                count = 0;
                while (count < parameters.size())
                {
                    var = m.getIntVar(count++);
                    value = s.getVar(var).getVal();
                    line = line + value + ",";
                }

                while (line.endsWith(","))
                {
                    line = line.substring(0, line.length() - 1);
                }
                x = line.split(",");
                Parameter p;
                line = "";

                for (int j = 0; j < x.length; j++)
                {
                    p = lp.get(j);
                    if (p.getType() == Parameter.PARAM_TYPE_ENUM)
                    {
                        line = line + enumList.get(Integer.parseInt((String) x[j])).toString() + ",";
                    }

                    if (p.getType() == Parameter.PARAM_TYPE_BOOL)
                    {
                        line = line
                               + (x[j].equals("0") ? Boolean.toString(false).toUpperCase() : Boolean.toString(true)
                                                                                                    .toUpperCase())
                               + ",";
                    }

                    if (p.getType() == Parameter.PARAM_TYPE_INT)
                    {
                        line = line + x[j] + ",";
                    }

                }


                while (line.endsWith(","))
                {
                    line = line.substring(0, line.length() - 1);
                }
                _infile[_countRandomTests++] = line;

                try
                {
                    bw.write(line);
                    bw.newLine();
                    line = "";
                }
                catch (Exception ex)
                {
                }

            }
            while (s.nextSolution() && _countRandomTests < NoSolutions);


            try
            {
                bw.close();
            }
            catch (IOException e)
            {

                e.printStackTrace();
            }

        }
        catch (Exception ex)
        {
            if (ex.getMessage().equals(""))
            {
                JOptionPane.showMessageDialog(parentFrame, "Error getting random test!!");
            }
            else
            {
                JOptionPane.showMessageDialog(parentFrame, ex.getMessage());
            }
        }
    }


    public boolean Solver(JFrame screen, JProgressBar pb)
    {
        try
        {
            ArrayList<ArrayList<Integer>> solution = new ArrayList<ArrayList<Integer>>();


            //for each constraints get its solutions
            // for (meConstraint mc : lc)
            //{


            m = new CPModel();
            CreateVariables();//crate variable for the model

            // CreateConstraints(lc.indexOf(mc)); //create chco constraint
            // CreateConstraints();

            CPSolver s = new CPSolver(); //create object solver
            m.setDefaultExpressionDecomposition(true);
            s.read(m); //read model (contains variables and constraints)
            s.getConfiguration().putInt(Configuration.SOLUTION_POOL_CAPACITY, Integer.MAX_VALUE);


            IntegerVariable var;
            Iterator<IntegerVariable> variables = m.getIntVarIterator();


            s.solveAll(); //solve model
            // int rootworld = s.getEnvironment().getWorldIndex();
            // s.worldPush();


            // s.solveAll();
            // ISolutionPool pool = s.getSearchStrategy().getSolutionPool();
            // 
            if (!s.isFeasible())
            {
                JOptionPane.showMessageDialog(screen, "Constraints don't have a feasible solution!!");
                return false;
            }

            // pb.setIndeterminate(false);
            // pb.setStringPainted(true);
            // pb.setValue(0);
            // pb.setMaximum(s.getNbSolutions());

            File fileInvalid = new File("C:\\Users\\ind1\\Documents\\mendoza\\My Documents\\tmpInvalid.txt");
            FileWriter fwInvalid = new FileWriter(fileInvalid);
            BufferedWriter bwInvalid = new BufferedWriter(fwInvalid);

            Parameter p = null;
            //get first solution

            ArrayList<Integer> t = new ArrayList<Integer>();
            // StringBuilder line = null;
            String line;

            int value;
            String valueMap;


            do
            {


                line = "";//new StringBuilder();
                t.clear();
                variables = m.getIntVarIterator();


                while (variables.hasNext())
                {
                    var = variables.next();
                    value = s.getVar(var).getVal();
                    valueMap = "";
                    t.add(value);


                    p = lp.get(t.size() - 1);
                    if (p.getType() == Parameter.PARAM_TYPE_ENUM)
                    {
                        valueMap = enumList.get(value).toString();
                    }

                    if (p.getType() == Parameter.PARAM_TYPE_BOOL)
                    {
                        valueMap = value == 0 ? Boolean.toString(false).toUpperCase() : Boolean.toString(true)
                                                                                               .toUpperCase();
                    }

                    if (p.getType() == Parameter.PARAM_TYPE_INT)
                    {
                        valueMap = Integer.toString(value);
                    }

                    //if (line.length() > 0) line.append(", ");
                    //line.append(valueMap).append(",");
                    line = line + valueMap + ",";


                }


                bwInvalid.write(line);
                bwInvalid.newLine();
                bwInvalid.flush();

                variables = null;

            }
            while (s.nextSolution().booleanValue());


            bwInvalid.close();
             
            	
            /*  
             while (variables.hasNext())
             {
                 var = (IntegerVariable)variables.next();
                 value = s.getVar(var).getVal();
                 
                 
                  t.add(value);
                  
              
               
                     p = lp.get(t.size()-1);
                     if (p.getType() == Parameter.PARAM_TYPE_ENUM)
                    	 valueMap = enumList.get((int) value).toString();
                     
                     if (p.getType() == Parameter.PARAM_TYPE_BOOL)
                    	 valueMap = (int) value==0 ? Boolean.toString(false).toUpperCase() : Boolean.toString(true).toUpperCase();

                     if (p.getType() == Parameter.PARAM_TYPE_INT)
                    	 valueMap = Integer.toString((int) value);

                
                 
                
                 
                 line=line + valueMap + ",";
             }
             
             bwInvalid.write(line);
             bwInvalid.newLine();
             //bwInvalid.flush();
            
          //   solution.add(t);

             //get all solutions
             while (s.nextSolution().booleanValue() )
             {
                 //variables = m.getIntVarIterator();
                 var = null;
                 t = new ArrayList<Integer>();
                 line="";
                 p=null;
                 
                 while (variables.hasNext())
                 {
                     var = (IntegerVariable)variables.next();
                     value = s.getVar(var).getVal();
                     t.add(value);
                     
                   
                     
                     p = lp.get(t.size()-1);
                     if (p.getType() == Parameter.PARAM_TYPE_ENUM)
                    	 valueMap = enumList.get((int) value).toString();
                     
                     if (p.getType() == Parameter.PARAM_TYPE_BOOL)
                    	 valueMap = (int) value==0 ? Boolean.toString(false).toUpperCase() : Boolean.toString(true).toUpperCase();

                     if (p.getType() == Parameter.PARAM_TYPE_INT)
                    	 valueMap = Integer.toString((int) value);

                     
                   
                     line=line + valueMap + ",";
                 }
                 
             
                 bwInvalid.write(line);
                 bwInvalid.newLine();
                // bwInvalid.flush();
                 
               //  solution.add(t);
                 
                
                 
                 if (s.isFeasible()==null || s.isFeasible()==Boolean.FALSE)
                	 break;
             }
             
             bwInvalid.close();
               //  if (!solution.contains(t))
                    
         
             
          
             /* IntegerVariable var = null;
               int value;
               
               
               
             for (Solution sol:pool.asList())
             {
	             ArrayList<Integer> t = new ArrayList<Integer>();
	             Iterator<IntegerVariable> variables =m.getIntVarIterator();
	          
	            s.worldPopUntil(rootworld); // restore the original state, where domains were as declared (not yet instantiated)
	             s.worldPush(); // backup the current state of the solver, to allow other solution restoration
	              s.restoreSolution(sol); // restore the solution
	          
	             
	              
	             int count=0;
	             while (count<parameters.size())
                 {
                     var = m.getIntVar(count++);
                     value = sol.getSolver().getVar(var).getVal();
                     t.add(value);
                 }
	             solution.add(t);
	             
	          pb.setValue(pb.getValue()+1);
	             
         }
     

             
             
             
         int i = 0;
         Object[] x;
         if (solution.size()>0)
             resultmapped = new String[solution.size()][];

         //get mapped solution
         for (List<Integer> l : solution)
         {
             x = l.toArray(); 
      
             resultmapped[i] = new String[x.length]; 
            
       
             for (int j = 0; j < x.length; j++)
             {
                 p = lp.get(j);
                 if (p.getType() == Parameter.PARAM_TYPE_ENUM)
                     resultmapped[i][j] = enumList.get((int) x[j]).toString();
                 
                 if (p.getType() == Parameter.PARAM_TYPE_BOOL)
                     resultmapped[i][j] = (int) x[j]==0 ? Boolean.toString(false).toUpperCase() : Boolean.toString(true).toUpperCase();

                 if (p.getType() == Parameter.PARAM_TYPE_INT)
                     resultmapped[i][j] = Integer.toString((int) x[j]);

             }
             i++;
           
         }*/

            return true;
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            return false;

        }
    }

    public boolean EvaluateCombination(String[][] Parm_Value)
    {


        CPSolver s;
        List<String[]> listparams = new ArrayList<String[]>();

        for (meConstraint c : lc)
        {
            m = new CPModel();
            listparams.clear();

            for (String[] o : Parm_Value)
            {
                listparams.add(o);
            }

            for (String name : c.get_params())
            {
                boolean fnd = false;

                for (String[] a : listparams)
                {

                    if (a[0].trim().equals(name.trim()))
                    {
                        fnd = true;
                        break;
                    }
                }
                if (!fnd)
                {
                    String[] o = new String[2];
                    o[0] = name;
                    o[1] = "";
                    listparams.add(o);
                }
            }


            CreateCombinationsVariables(listparams);//crate variable for the model
            CreateConstraints(c);

            s = new CPSolver(); //create object solver
            m.setDefaultExpressionDecomposition(false);
            s.read(m); //read model (contains variables and constraints)
            s.getConfiguration().putInt(Configuration.SOLUTION_POOL_CAPACITY, Integer.MAX_VALUE);
            s.solveAll(); //solve model

            if (!s.isFeasible())
            {
                return false;
            }
            m = null;
            s = null;
        }

        return true;
    }

}
