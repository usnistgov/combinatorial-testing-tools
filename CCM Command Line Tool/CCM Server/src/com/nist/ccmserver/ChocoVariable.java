package com.nist.ccmserver;


import java.util.ArrayList;

import choco.Choco;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.variables.integer.IntDomain;


public class ChocoVariable {


	String name;
	IntegerVariable var; //used for choco
	Parameter param; //related parameter
	ArrayList<Integer> values; //domain from parameter
		
	private IntDomain newDomain; //reduced domain
	private int groupID; //constraint group
	private int[] countSolved;
	
	public ChocoVariable(Parameter par, ArrayList<Integer> val){
		param=par;
		name=par.getName();
		values=new ArrayList<Integer>(val);
		var=Choco.makeIntVar(par.getName(),values);	
		groupID=-1;
		countSolved=new int[val.size()];
	}

	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public IntegerVariable getVar() {
		return var;
	}



	public void setVar(IntegerVariable var) {
		this.var = var;
	}

	


	public int getGroupID() {
		return groupID;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	
	public boolean checkValue(int v) {
		if(newDomain==null){
			if(values.contains(v))
				return true;
			else 
				return false;
		}			
		return newDomain.contains(v);
	}

	public void setNewDomain(IntDomain newDomain) {
		this.newDomain = newDomain;
	}
	
	public int getValue(int idx) {
		countSolved[idx]++;
		return values.get(idx);
	}
	
	public int getIdx(int value) {
		return values.indexOf(value);
	}
	
	public void printInfo(){
		System.out.print(name+": \t");
		for(int i:countSolved){
			if(i>0)	System.out.print(i+"\t");
		}
		System.out.println("\t"+newDomain);
		
		
	}
}
