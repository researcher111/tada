package FlowSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import Constraints.Constraints;

import soot.Value;
import soot.toolkits.scalar.AbstractFlowSet;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.Pair;

/*To be implemented instead of using HashMap<Value, Constraints> in MyDataFlowAnalysis*/

public class ConstraintFlowSet extends AbstractFlowSet{

	//map of the elements
	private HashMap<Value, Constraints> valueToConstraints; 
	
	public ConstraintFlowSet()
	{
		valueToConstraints = new HashMap<Value, Constraints>();
	}
	
	public ConstraintFlowSet(HashMap<Value, Constraints> map) {
		// TODO Auto-generated constructor stub
		valueToConstraints = map;
	}

	
	
	@Override
	public void add(Object obj) {
		// TODO Auto-generated method stub
		Pair<Value, Constraints>  pair = (Pair<Value, Constraints>) obj;
		if(valueToConstraints.containsKey(pair.getO1()))
		{
			Constraints cons = valueToConstraints.get(pair.getO1());
			/*Maybe its union in some cases check.......not used nor*/
			cons.concatConstraints(pair.getO2());
		}
		else
			valueToConstraints.put(pair.getO1(), pair.getO2());
		
	}

	@Override
	public AbstractFlowSet clone() {
		// TODO Auto-generated method stub
		/*Not cloning the keys for now..i think its not needed*/
		HashMap<Value, Constraints> clonedMap = new HashMap<Value, Constraints>();
		for (Value val : valueToConstraints.keySet()) {
			Constraints cons = valueToConstraints.get(val);
			/*implement clone*/
			Constraints clonedConstraints = cons.cloneConstraints();
			clonedMap.put(val, clonedConstraints);
		}
		return new ConstraintFlowSet(clonedMap);
	}

	@Override
	public boolean contains(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof Value)
			return valueToConstraints.containsKey(obj);
		else if(obj instanceof Pair){
			Constraints c1 = valueToConstraints.get(((Pair)obj).getO1());
			Constraints c2 = (Constraints) ((Pair)obj).getO2();
			return c1.equals(c2);
		}
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return valueToConstraints.isEmpty();
	}

	@Override
	public void remove(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof Value)
			valueToConstraints.remove(obj);
		else if(obj instanceof Pair){
			valueToConstraints.remove(((Pair)obj).getO1());
		}
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return valueToConstraints.size();
	}

	@Override
	public List toList() {
		// TODO Auto-generated method stub
		List listOfPairs = new ArrayList(valueToConstraints.size());
		for (Value value : valueToConstraints.keySet()) {
			Pair<Value, Constraints> pair = new Pair<Value, Constraints>(value, valueToConstraints.get(value));
			listOfPairs.add(pair);
		}
		return listOfPairs;
	}

	public ConstraintFlowSet mergeConstraints (ConstraintFlowSet flow2)
	{
		ConstraintFlowSet mergedConstraints = new ConstraintFlowSet();
		Iterator<Pair<Value, Constraints>> it1 =  this.toList().iterator();
    	
		while (it1.hasNext()) {
    		Pair<Value, Constraints> pair = it1.next();
    		Constraints cInFlow2 = flow2.valueToConstraints.get(pair.getO1());
    		//Constraints cInFlow2 = GetConstraintsInArray((ArraySparseSet) flow2, pair.getO1());
    		if(cInFlow2 == null)
    			mergedConstraints.add(pair);
    		else{
    			Constraints c = cInFlow2.mergeConstraintsAlongFlow(pair.getO2());
    			mergedConstraints.add(new Pair<Value, Constraints>(pair.getO1(), c));
    		}
		}
    	/*At constraints in flow1 but not in flow2*/
    	Iterator<Pair<Value, Constraints>> it2 =  flow2.toList().iterator();
    	
    	while (it2.hasNext()) {
    		Pair<Value, Constraints> pair = it2.next();
    		Constraints cAtFlow1 = 
        		this.valueToConstraints.get(pair.getO1());;
    		if(cAtFlow1 == null)
    			mergedConstraints.add(pair);
		}
    	return mergedConstraints;
	}
	
	public ConstraintFlowSet concatConstraints (ConstraintFlowSet toBeConcatenated)
	{
		ConstraintFlowSet concatinatedConstraints = new ConstraintFlowSet();
		Iterator<Pair<Value, Constraints>> it1 =  toBeConcatenated.toList().iterator();
		boolean found = false;
    	while (it1.hasNext()) {
    		Pair<Value, Constraints> pair = it1.next();
    		Constraints baseConstraints =this.valueToConstraints.get(pair.getO1());
    		if(baseConstraints != null){
    			found = true;
    			Constraints c = baseConstraints.concatConstraints(pair.getO2());
    			concatinatedConstraints.add(new Pair<Value, Constraints>(pair.getO1(), c));
    		}
    		else
    			concatinatedConstraints.add(pair);
		}
    		
    	/*At constraints in flow1 but not in flow2*/
    	Iterator<Pair<Value, Constraints>> it2 =  this.toList().iterator();
    	
    	while (it2.hasNext()) {
    		Pair<Value, Constraints> pair = it2.next();
    		Constraints cAtFlow1 = concatinatedConstraints.valueToConstraints.get(pair.getO1());
    		if(cAtFlow1 == null)
    			concatinatedConstraints.add(pair);
		}
    	
    	return concatinatedConstraints;
	}
	
	@Override
	public String toString()
	{
		String s = "";
		for (Object obj : this.toList()) {
			Pair<Value, Constraints> pair = (Pair<Value, Constraints>)obj;
			String constraints = pair.getO2().toString();
			String var = pair.getO1().toString();
			s = s+ "{" + var + "->"+ constraints + "}" + ",";
		}
		return s; 
	}

	

	public HashMap<Value, Constraints> getValueToConstraints() {
		return valueToConstraints;
	}
}
