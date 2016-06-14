package com.nist.ccmserver;

import java.util.ArrayList;
import java.util.List;

public class TestData {
	private int number_of_rows;
	private int number_of_columns;
	private boolean paramNames;
	private boolean acts_file_present;
	private boolean constraints_file_present;
	private List<Parameter> parameters;
	private List<meConstraint> constraints;
	
	
	public TestData(){
		parameters = new ArrayList<Parameter>();
		constraints = new ArrayList<meConstraint>();
	}
	
	public void set_rows(int rows){
		number_of_rows = rows;
	}
	
	public void set_columns(int columns){
		number_of_columns = columns;
	}
	
	public int get_rows(){
		return number_of_rows;
	}
	
	public int get_columns(){
		return number_of_columns;
	}
	
	public void set_paramNames(boolean b){
		paramNames = b;
	}
	
	public void set_acts_file_present(boolean b){
		acts_file_present = b;
	}
	
	public void set_constraints_file_present(boolean b){
		constraints_file_present = true;
	}
	
	public boolean isActsFilePresent(){
		return acts_file_present;
	}
	
	public boolean isConstraintsFilePresent(){
		return constraints_file_present;
	}
	
	public boolean hasParamNames(){
		return paramNames;
	}
	
	public void add_parameter(Parameter p){
		parameters.add(p);
	}
	
	public List<Parameter> get_parameters(){
		return parameters;
	}
	
	
	public List<meConstraint> get_constraints(){
		return constraints;
	}

}
