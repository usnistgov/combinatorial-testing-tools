package com.nist.ccmcl;


import choco.Choco;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;


/*
 * This class is used for type checking in constraint parser.
 */
public class TypeInfo {

	private String text;
	private Constraint constraint;
	private IntegerExpressionVariable variable;
	private Object obj;
	
	int type;
	
	public void setType (int type) {
		this.type = type;
	}
	public int getType () {
		return type;
	}
	
	public void setText (String text) {
		this.text = text;
	}
	public String getText () {
		return text;
	}

	public Constraint getConstraint() {
		if(this.constraint==null&&variable!=null&&type==Constants.TYPE_BOOL){
			this.constraint=Choco.eq(variable, 1);// this is important to promote a boolean variable to a constraint!!
		}
		return this.constraint;
	}
	
	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;
	}
	
    public	IntegerExpressionVariable getVariable() {
		return variable;
	}
	
	public void setVariable(IntegerExpressionVariable variable) {
		this.variable = variable;
	}
	
	public Object getObj() {
		if(this.constraint!=null)
			return this.constraint;
		return variable;
	}
	
	public void setObj(Object obj) {
		this.obj = obj;
		if(obj instanceof Constraint)
			this.constraint=(Constraint)obj;
		else if(obj instanceof Variable)
			variable=(IntegerExpressionVariable)obj;
	}
}
