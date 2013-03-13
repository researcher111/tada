package FlowSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import Constraints.Constraints;

import soot.Value;
import soot.jimple.Stmt;
import soot.toolkits.scalar.AbstractFlowSet;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.Pair;

/*To be implemented instead of using HashMap<Value, Constraints> in MyDataFlowAnalysis*/

public class ControlFlowSet extends AbstractFlowSet{

	//map of the elements
	/*An if (or switch) statement -> set of variables controlling the condition in the statement*/
	private HashMap<Stmt, HashSet<Value>> cntrlTaintFlow; 
	/*the innermost condition*/
	public ArrayList<Stmt> stmtStack = new ArrayList<Stmt>(1);
	//Stmt innerMost; //maybe a stack is needed!!
	
	public ControlFlowSet()
	{
		cntrlTaintFlow = new HashMap<Stmt, HashSet<Value>>(1);
	}
	
	public ControlFlowSet(HashMap<Stmt, HashSet<Value>> map)
	{
		this.cntrlTaintFlow = map;
	}
	
	@Override
	public void clear(){
		cntrlTaintFlow = new HashMap<Stmt, HashSet<Value>>(1);
		stmtStack = new ArrayList<Stmt>(1);
	}
	
	@Override
	public void add(Object obj) {
		// TODO Auto-generated method stub
		Pair<Stmt, HashSet<Value>>  pair = (Pair<Stmt, HashSet<Value>>) obj;
		if(cntrlTaintFlow.containsKey(pair.getO1()))
		{
			HashSet<Value> vars =  cntrlTaintFlow.get(pair.getO1());
			vars.addAll(pair.getO2());
			
		}
		else{
			cntrlTaintFlow.put(pair.getO1(), pair.getO2());
			stmtStack.add(pair.getO1());
		}
	}

	@Override
	public AbstractFlowSet clone() {
		// TODO Auto-generated method stub
		/*Not cloning the keys for now..i think its not needed*/
		HashMap<Stmt, HashSet<Value>> clonedMap = new HashMap<Stmt, HashSet<Value>>();
		
		for (Stmt stmt : cntrlTaintFlow.keySet()) {
			HashSet<Value> vars =  cntrlTaintFlow.get(stmt);
			/*implement clone*/
			clonedMap.put(stmt, new HashSet<Value>(vars));
		}
		ControlFlowSet clonedSet = new ControlFlowSet(clonedMap);
		clonedSet.stmtStack =  new ArrayList<Stmt>(this.stmtStack); 
		return clonedSet;
	}

	@Override
	public boolean contains(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof Value){
			for (Stmt s : cntrlTaintFlow.keySet()) {
				return cntrlTaintFlow.get(s).contains(obj);
			}
			
		}
		else if(obj instanceof Pair){
			HashSet<Value> v1 = (HashSet<Value>) cntrlTaintFlow.get(((Pair)obj).getO1());
			HashSet<Value> v2 = (HashSet<Value>) ((Pair)obj).getO2();
			if(v1 == null && v2!=null)
				return false;
			return v1.equals(v2);
		}
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return cntrlTaintFlow.isEmpty();
	}

	@Override
	public void remove(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof Pair){
			cntrlTaintFlow.remove(((Pair)obj).getO1());
			stmtStack.remove(((Pair)obj).getO1());
		} else
			try {
				throw new Exception("Not handled type : " + obj.getClass());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return cntrlTaintFlow.size();
	}

	@Override
	public List toList() {
		// TODO Auto-generated method stub
		List listOfPairs = new ArrayList(cntrlTaintFlow.size());
		for (Stmt s : cntrlTaintFlow.keySet()) {
			Pair<Stmt, HashSet<Value>> pair = new Pair<Stmt, HashSet<Value>>(s, cntrlTaintFlow.get(s));
			listOfPairs.add(pair);
		}
		return listOfPairs;
	}
	
	private Stmt getLastStatementInStack(){
		if(stmtStack.size()>0)
			return stmtStack.get(stmtStack.size() -1);
		else
			return null;
	}

	public ControlFlowSet mergeConstraints (ControlFlowSet flow2)
	{
		ControlFlowSet mergedFlow = new ControlFlowSet();
		Iterator<Pair<Stmt, HashSet<Value>>> it1 =  this.toList().iterator();

		if(this.getLastStatementInStack() != null){
	    	if(!this.getLastStatementInStack().equals(flow2.getLastStatementInStack())){
	    		try {
					throw new Exception("Innermost not equal!!!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
		}
		while (it1.hasNext()) {
    		Pair<Stmt, HashSet<Value>> pair = it1.next();
    		HashSet<Value> cInFlow2 = flow2.getCntrlTaintFlow().get(pair.getO1());
    		if(cInFlow2 == null)
    			mergedFlow.add(pair);
    		else{
    			HashSet<Value> mergedValues = new HashSet<Value>(1);
    			mergedValues.addAll(pair.getO2());
    			mergedValues.addAll(cInFlow2);
    			mergedFlow.add(new Pair<Stmt, HashSet<Value>>(pair.getO1(), mergedValues));
    		}
		}
    	/*At constraints in flow1 but not in flow2*/
    	Iterator<Pair<Value, Constraints>> it2 =  flow2.toList().iterator();
    	
    	while (it2.hasNext()) {
    		Pair<Value, Constraints> pair = it2.next();
    		HashSet<Value> cAtFlow1 = 
        		this.cntrlTaintFlow.get(pair.getO1());;
    		if(cAtFlow1 == null)
    			mergedFlow.add(pair);
		}
    	return mergedFlow;
	}
	
	@Override
	public void intersection(FlowSet otherFlow, FlowSet dest)
	{
		
		ControlFlowSet intersection = new ControlFlowSet();
		ControlFlowSet flow2 = (ControlFlowSet) otherFlow;
		
		Iterator<Pair<Stmt, HashSet<Value>>> it1 =  this.toList().iterator();
		Pair<Stmt, HashSet<Value>> lastAdded = null;
		if(flow2.getStmtStack().size()!=0 && this.getStmtStack().size()!=0 && !flow2.getLastStatementInStack().equals(this.getLastStatementInStack())){
    		for (Stmt s : flow2.getStmtStack()) {
				if(this.getStmtStack().contains(s)){
					if(this.getStmtStack().indexOf(s) != flow2.getStmtStack().indexOf(s)){
						try {
							//throw new Exception("index should be same!!");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					lastAdded = new Pair<Stmt, HashSet<Value>>(s, flow2.getCntrlTaintFlow().get(s));
					intersection.add(lastAdded);
				}
				//else
				//	intersection.add(new Pair<Stmt, HashSet<Value>>(s, flow2.getCntrlTaintFlow().get(s)));
			}
    		if(lastAdded != null)
    			intersection.remove(lastAdded);
    		/*for (Stmt s : this.getStmtStack()) {
				if(!flow2.getStmtStack().contains(s)){
					intersection.add(new Pair<Stmt, HashSet<Value>>(s, this.getCntrlTaintFlow().get(s)));
				}
			}*/
    	}
    	else if(flow2.getStmtStack().size()!=0 && this.getStmtStack().size()!=0){
			while (it1.hasNext()) {
	    		Pair<Stmt, HashSet<Value>> pair = it1.next();
				if(pair.getO1().equals(this.getLastStatementInStack())){
					continue;
				}	
	    		HashSet<Value> cInFlow2 = flow2.getCntrlTaintFlow().get(pair.getO1());
	    		if(cInFlow2 != null){
	    			HashSet<Value> mergedValues = new HashSet<Value>(1);
	    			for (Value val : cInFlow2) {
						if(pair.getO2().contains(val))
							mergedValues.add(val);
					}
	    			intersection.add(new Pair<Stmt, HashSet<Value>>(pair.getO1(), mergedValues));
	    		}
			}
    	}
    	else if(flow2.getStmtStack().size()==0 && this.getStmtStack().size()==0){
    		return;
    	}
    	else if(flow2.getStmtStack().size()==0){
    		//intersection = this;
    	}
    	else if(this.getStmtStack().size()==0){
    		//intersection = flow2;
    	}
		
    	for (Stmt s : intersection.getStmtStack()) {
			dest.add(new Pair<Stmt, HashSet<Value>>(s, intersection.cntrlTaintFlow.get(s)));
		}
		if(intersection.cntrlTaintFlow.keySet().size() != intersection.stmtStack.size())
			try {
				throw new Exception("Invarient broken");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	@Override
	public String toString()
	{
		String s = "";
		for (Object obj : this.toList()) {
			Pair<Stmt, HashSet<Value>> pair = (Pair<Stmt, HashSet<Value>>)obj;
			String constraints = pair.getO2().toString();
			String var = pair.getO1().toString();
			s = s+ "{" + var + "->"+ constraints + "}" + ",";
		}
		return s; 
	}

	public HashSet<Value> getAllVars(){
		HashSet<Value> vars = new HashSet<Value>(1);
		for (Object obj : this.toList()) {
			Pair<Stmt, HashSet<Value>> pair = (Pair<Stmt, HashSet<Value>>)obj;
			vars.addAll(pair.getO2());
		}
		return vars;
	}
	

	public void setCntrlTaintFlow(HashMap<Stmt, HashSet<Value>> cntrlTaintFlow) {
		this.cntrlTaintFlow = cntrlTaintFlow;
	}

	public HashMap<Stmt, HashSet<Value>> getCntrlTaintFlow() {
		return cntrlTaintFlow;
	}

	public void addFlow(ControlFlowSet flowAtUnit, ControlFlowSet outFlow) {
		// TODO Auto-generated method stub
		//ControlFlowSet newFlow = new ControlFlowSet();
		if(cntrlTaintFlow.keySet().size() != stmtStack.size())
			try {
				throw new Exception("Invarient broken");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		for (Stmt s : this.stmtStack) {
			if(outFlow.getCntrlTaintFlow().get(s) != null){
				try {
					throw new Exception("Null expected here");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				outFlow.getCntrlTaintFlow().put(s, new HashSet<Value>(cntrlTaintFlow.get(s)));
				outFlow.stmtStack.add(s);
			}
		}
		
		for (Stmt s : flowAtUnit.stmtStack) {
			if(outFlow.getCntrlTaintFlow().get(s) != null){
				//maybe a loop
				outFlow.getCntrlTaintFlow().get(s).addAll(cntrlTaintFlow.get(s));
			}
			else{
				outFlow.getCntrlTaintFlow().put(s, new HashSet<Value>(flowAtUnit.cntrlTaintFlow.get(s)));
				outFlow.stmtStack.add(s);
			}
		}
	}

	public void setStmtStack(ArrayList<Stmt> stmtStack) {
		this.stmtStack = stmtStack;
	}

	public ArrayList<Stmt> getStmtStack() {
		return stmtStack;
	}
}
