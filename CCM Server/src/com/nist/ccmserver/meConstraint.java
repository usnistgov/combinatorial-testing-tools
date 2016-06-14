package com.nist.ccmserver;
import java.util.List;

import choco.kernel.model.constraints.Constraint;


//object constraint
public class meConstraint {
	String _cons;
	Constraint _chocoConstraint; 
	List<String> _params;
	
 	//constructor specifying the constraint with a string
	public meConstraint(String c, List<String> p)
	{
		_cons=c;
		_params=p;
	}
	
	//get the constraint
	public String get_cons() {
		return _cons;
	}
	
	public List<String> get_params()
	{
		return _params;
	}
	
	

	
	
}
