package Constraints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;


import soot.Value;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLineNumberTag;
import soot.tagkit.SourceLnNamePosTag;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.Pair;


public class Constraints {
	/*Constraints for a variable along different paths*/
	private Value variable;
	//For now we have a list of statements .... constraints need to be extracted from these TODO
	private HashSet<ArrayList<Stmt>> constraints;
	
	
	
	public Constraints()
	{
		setConstraints(new HashSet<ArrayList<Stmt>>());
	}
	
	public Constraints(Value var)
	{
		setConstraints(new HashSet<ArrayList<Stmt>>());
		this.variable = var;
	}
	
	public void addStatementToAllConstraints(Stmt statement)
	{
		for (ArrayList<Stmt> stmtList : getConstraints()) {
			stmtList.add(statement);
		}
	}

	public ArrayList<Stmt> addNewContstraint(Stmt s)
	{
		ArrayList<Stmt> constraint = new ArrayList<Stmt>();
		constraint.add(s);
		getConstraints().add(constraint);
		return constraint;
	}
	
	public ArrayList<Stmt> addNewContstraint(ArrayList<Stmt> s)
	{
		getConstraints().add(s);
		return s;
	}
	
	
	public void setVariable(Value variable) {
		this.variable = variable;
	}
	
	public Value getVariable() {
		return variable;
	}

	public Constraints mergeConstraintsAlongFlow(Constraints mergeWith)
	{
		if(!this.variable.toString().equals(mergeWith.variable.toString()))
		{
			return null;
		}
		Constraints merged = new Constraints(this.variable);
		HashSet<ArrayList<Stmt>> mergedConstraints = new HashSet<ArrayList<Stmt>>();
		mergedConstraints.addAll(constraints);
		for (ArrayList<Stmt> constraintToMerge : mergeWith.getConstraints()) {
			if(this.contains(constraintToMerge))
				continue;
			else
				mergedConstraints.add(constraintToMerge);
			
		}
		merged.setConstraints(mergedConstraints);
		
		return merged;
	}
	
	
	
	public Constraints concatConstraints(Constraints c2) {
		if(!this.variable.toString().equals(c2.variable.toString()))
		{
			return null;
		}
		Constraints concatinated = new Constraints(this.variable);
		HashSet<ArrayList<Stmt>> concatinatedConstraints = new HashSet<ArrayList<Stmt>>();
		concatinatedConstraints.addAll(constraints);
		for (ArrayList<Stmt> constraint : c2.getConstraints()) {
			if(this.contains(constraint))
				continue;
			else
			{
				for (ArrayList<Stmt> concatenatedCons : concatinatedConstraints) {
					for (Stmt stmt2 : constraint) {
						if(!concatenatedCons.contains(stmt2))
						{
							concatenatedCons.add(stmt2);
						}
						else
						{
							//TODO its probably a loop there => handle it	
						}
					}
				}
			}
		}
		concatinated.setConstraints(concatinatedConstraints);
		
		return concatinated;
	}
	

	private boolean contains(ArrayList<Stmt> constraint) {
		// TODO Auto-generated method stub
		for (ArrayList<Stmt> c : constraints) {
			if(c.size() != constraint.size()) continue;
			boolean eqFlag= true;
			for (Stmt stmt : c) {
				eqFlag= true;
				if(!constraint.contains(stmt))
					eqFlag = false;
			}
			if(eqFlag)
				return true;
		}
		return false;
	}

	public void setConstraints(HashSet<ArrayList<Stmt>> constraints) {
		this.constraints = constraints;
	}

	public HashSet<ArrayList<Stmt>> getConstraints() {
		return constraints;
	}
	
	@Override
	public String toString(){
		String s = new String();
		s = "[";
		for (ArrayList<Stmt> constraint : constraints) {
			s = s + "<";
				for (Stmt stmt : constraint) {
					if(stmt.getTags().get(0) instanceof SourceLnNamePosTag){
						SourceLnNamePosTag line = (SourceLnNamePosTag) stmt.getTags().get(0);
						int start = line.startLn();
						int end = line.endLn();
						s = s + start + "-" + end + ",";
					}
					else if(stmt.getTags().get(0) instanceof SourceLineNumberTag)
					{
						SourceLineNumberTag line = (SourceLineNumberTag) stmt.getTags().get(0);
						int start = line.getLineNumber();
						s = s + start + ",";
					}
					
			}
			s = s+ ">";
		}
		s = s +"]";
		return s;
	}

	public Constraints cloneConstraints()
	{
		/*Does not clones statements*/
		Constraints clonedConstraints = new Constraints(this.variable);
		for (ArrayList<Stmt> constraint : this.constraints) {
			ArrayList<Stmt> clonedConstraint = new ArrayList<Stmt>();
			for (Stmt stmt : constraint)
				clonedConstraint.add(stmt);
			clonedConstraints.addNewContstraint(clonedConstraint);
		}
		return clonedConstraints;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Constraints)) return false;
		Constraints that = (Constraints) obj;
		if (!that.variable.toString().equals(this.variable.toString()))
			return false;
		if(this.constraints.size() != that.constraints.size())
			return false;
		for (ArrayList<Stmt> thisConstrint : this.constraints) {
			if(!that.contains(thisConstrint))
				return false;
		}
		return true;
	}
	
}
