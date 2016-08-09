package com.nist.ccmcl;

import java.util.ArrayList;


public class Parameter
{

    //types
    /**
     * Indicates an integer parameter
     */
    public static final int PARAM_TYPE_INT = 0;

    /**
     * Indicates an  parameter
     */
    public static final int PARAM_TYPE_ENUM = 1;

    /**
     * indicates a boolean parameter
     */
    public static final int PARAM_TYPE_BOOL = 2;

    private String            _name;
    private int               _type;
    private ArrayList<String> _values;
    private ArrayList<String> _realValues;
    private ArrayList<Double> _bound;
    private ArrayList<Object> _groups;
    private boolean           _boundary;
    private boolean           _group;


    //constructor specifying the name, and initializing values and boundaries
    public Parameter(String name)
    {
        _name = name;
        _values = new ArrayList<String>();
        _bound = new ArrayList<Double>();
        _groups = new ArrayList<Object>();
    }

    //get type
    public int getType()
    {
        return _type;
    }

    //set type
    public void setType(int t)
    {
        _type = t;
    }

    //set name
    public String getName()
    {
        return _name;
    }

    //get if the parameter has or not boundaries specified, this property does not get the boundaries
    public boolean getBoundary()
    {
        return _boundary;
    }

    //set if the parameter has or not boundaries specified, this property does not set boundaries
    public void setBoundary(boolean b)
    {
        _boundary = b;
    }

    public boolean getGroup()
    {
        return _group;
    }

    public void setGroup(boolean b)
    {
        _group = b;
    }

    //Add a single value
    public void addValue(String v)
    {
        if (!_values.contains(v))
        {
            _values.add(v);
        }
    }

    //remove a single value by value
    public void removeValue(String v)
    {
        _values.remove(v);
    }

    //remove a single value by index
    public void removeByIndex(int i)
    {
        _values.remove(i);
    }

    //remove all values
    public void removeAllValues()
    {
        _values = new ArrayList<String>();
    }

    //get the list of values
    public ArrayList<String> getValues()
    {
        return _values;
    }

    //set the list of values for the parameter
    public void setValues(ArrayList<String> v)
    {
        _values = v;
    }

    //get list of boundaries
    public ArrayList<Double> getBounds()
    {
        return _bound;
    }

    public ArrayList<Object> getGroups()
    {
        return _groups;
    }

    public void setGroups(ArrayList<Object> b)
    {
        _groups = b;
    }

    //add boundary
    public void addBound(Double i)
    {

        if (!_bound.contains(i))
        {
            _bound.add(i);
        }

    }

    public void addGroup(Object i)
    {
        if (!_groups.contains(i))
        {
            _groups.add(i);
        }
    }

    //remove boundary
    public void removeBound(Double b)
    {
        _bound.remove(b);
    }

    public void removeGroup(Object i)
    {
        _groups.remove(i);
    }

    //remove boundary by index
    public void removeBoundByIndex(int i)
    {
        _bound.remove(i);
    }

    //remove all boundaries
    public void removeAllBoundaries()
    {
        _bound = new ArrayList<Double>();
    }

    public void removeAllGroups()
    {
        _groups = new ArrayList<Object>();
    }

    //set list of boundaries
    public void setBoundaries(ArrayList<Double> b)
    {
        _bound = b;
    }

    //get original values, in case of using boundaries
    public ArrayList<String> getValuesO()
    {
        return _realValues;
    }

    //set original values, in case of specifying boundaries
    public void setValuesO(ArrayList<String> o)
    {
        _realValues = o;
    }

    //remove original values
    public void removeAllValuesO()
    {
        _realValues.clear();
    }
}
