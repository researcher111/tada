package FlowAnalysis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Constraints.Constraints;



import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.internal.InvokeExprBox;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JNopStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.toolkits.scalar.Pair;

/**
 * Data Flow analysis starting from a given program point at which dummyMethod is invoked
 **/
public class MyDataFlowAnalysis extends ForwardFlowAnalysis{

	FlowSet emptySet = new ArraySparseSet();
    private HashMap<Unit, FlowSet> unitToConstraintSet;
    
	public MyDataFlowAnalysis(UnitGraph graph) {
		super(graph);
		unitToConstraintSet =new HashMap<Unit, FlowSet>();
        {
            Iterator unitIt = graph.iterator();
            while(unitIt.hasNext()){
                Unit s = (Unit) unitIt.next();
                FlowSet genSet = emptySet.clone();
                List<ValueBox> uses = s.getUseBoxes();
                List<ValueBox> defs = s.getDefBoxes();
                Pair<Value, Constraints> constraintValuePair = null;
                
                if (s instanceof AssignStmt) {
                	constraintValuePair = updateConstraintsForAssignment(s, uses, defs);
				}
                else if (s instanceof InvokeStmt) {
                	/*still to write code for static invocations and invocations without a receiver object*/
                	constraintValuePair = updateConstraintForInvocation(s);
				}
                else if ( s instanceof JIdentityStmt){
                	constraintValuePair = updateConstraintForIdentity(s);
                } 
                else if(s instanceof JReturnVoidStmt || s instanceof JGotoStmt || s instanceof JIfStmt || s instanceof JReturnStmt || s instanceof JNopStmt);
                else
					try {
						throw new Exception("Not Implemented for statement : " + s.getClass().toString());
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                
                
                if(constraintValuePair != null)
                	genSet.add(constraintValuePair, genSet);
                unitToConstraintSet.put(s, genSet);
                //unitToGuaranteedDefs.put(s, Collections.unmodifiableList(set.toList()));
            }
        }
		doAnalysis();
	}
	
	
	
	private Pair<Value, Constraints> updateConstraintForIdentity(Unit s) {
		JIdentityStmt stmt = (JIdentityStmt)s;
		List<ValueBox> defs = stmt.getDefBoxes();
		Pair<Value, Constraints> valueConstraintPair = null;
		for (ValueBox valueBox : defs) {
			Value val = valueBox.getValue();
			Constraints consForDef = new Constraints(val);
			consForDef.addNewContstraint(stmt);
			valueConstraintPair = new Pair<Value, Constraints>(val, consForDef);
		}
		return valueConstraintPair;
	}



	private Pair updateConstraintForInvocation(Unit s) {
		InvokeStmt invocation = (InvokeStmt) s;
		InvokeExpr expr = invocation.getInvokeExpr();
		InvokeExprBox box = (InvokeExprBox) invocation.getInvokeExprBox();
		if (box.getValue() instanceof JSpecialInvokeExpr) {
			JSpecialInvokeExpr invokeExpr = (JSpecialInvokeExpr) box.getValue();
			Value receiverObject = invokeExpr.getBase();
			String name = receiverObject.toString();
			Constraints consForReceiverObject = new Constraints(receiverObject);
			consForReceiverObject.addNewContstraint(invocation);
			Pair<Value, Constraints> valueConstraintPair = new Pair<Value, Constraints>(receiverObject, consForReceiverObject);
			return valueConstraintPair;
			//constraintsAtUnit.put(s, map);
		} 
		else if(box.getValue() instanceof JVirtualInvokeExpr)
		{
			JVirtualInvokeExpr invokeExpr = (JVirtualInvokeExpr) box.getValue();
			Value receiverObject = invokeExpr.getBase();
			String name = receiverObject.toString();
			Constraints consForReceiverObject = new Constraints(receiverObject);
			consForReceiverObject.addNewContstraint(invocation);
			Pair<Value, Constraints> valueConstraintPair = new Pair<Value, Constraints>(receiverObject, consForReceiverObject);
			return valueConstraintPair;
		}
		else
			try {
				throw new Exception("Not implemented for : " + box.getValue().getClass().toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	}

	private Pair updateConstraintsForAssignment(Unit s, List<ValueBox> uses, List<ValueBox> defs) {
		
		AssignStmt assignment = (AssignStmt) s;
		
		if(defs.size() != 1){
			try {
				throw new Exception("More than 1 definitions!!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Value val =  defs.get(0).getValue();
		
		Constraints consForDef = new Constraints(val);
		consForDef.addNewContstraint(assignment);
		Pair<Value, Constraints> valueConstraintPair = new Pair<Value, Constraints>(val, consForDef);
		return valueConstraintPair;
	}
	
	
	private Constraints GetConstraintsInArray(ArraySparseSet flow, Value v)
	{
		for (Object object : flow) {
			Pair<Value, Constraints> pair = (Pair<Value, Constraints>) object;
			if(v.toString().equals(pair.getO1().toString()))
					return pair.getO2();
		}
		return null;
	}
	
	private ArraySparseSet mergeConstraints (ArraySparseSet flow1, ArraySparseSet flow2)
	{
		ArraySparseSet mergedConstraints = new ArraySparseSet();
		Iterator<Pair<Value, Constraints>> it1 =  flow1.iterator();
    	while (it1.hasNext()) {
    		Pair<Value, Constraints> pair = it1.next();
    		Constraints cInFlow2 = GetConstraintsInArray((ArraySparseSet) flow2, pair.getO1());
    		if(cInFlow2 == null)
    			mergedConstraints.add(pair);
    		else{
    			Constraints c = cInFlow2.mergeConstraintsAlongFlow(pair.getO2());
    			mergedConstraints.add(new Pair<Value, Constraints>(pair.getO1(), c));
    		}
		}
    	/*At constraints in flow1 but not in flow2*/
    	Iterator<Pair<Value, Constraints>> it2 =  flow2.iterator();
    	
    	while (it2.hasNext()) {
    		Pair<Value, Constraints> pair = it2.next();
    		Constraints cAtFlow1 = GetConstraintsInArray((ArraySparseSet) flow1, pair.getO1());
    		if(cAtFlow1 == null)
    			mergedConstraints.add(pair);
		}
    	return mergedConstraints;
	}
	
	
	
	private ArraySparseSet concatConstraints (ArraySparseSet base, ArraySparseSet toBeConcatenated)
	{
		ArraySparseSet concatinatedConstraints = new ArraySparseSet();
		Iterator<Pair<Value, Constraints>> it1 =  toBeConcatenated.iterator();
		boolean found = false;
    	while (it1.hasNext()) {
    		Pair<Value, Constraints> pair = it1.next();
    		Constraints baseConstraints = GetConstraintsInArray((ArraySparseSet) base, pair.getO1());
    		if(baseConstraints != null){
    			found = true;
    			Constraints c = baseConstraints.concatConstraints(pair.getO2());
    			concatinatedConstraints.add(new Pair<Value, Constraints>(pair.getO1(), c));
    		}
    		else
    			concatinatedConstraints.add(pair);
		}
    		
    	/*At constraints in flow1 but not in flow2*/
    	Iterator<Pair<Value, Constraints>> it2 =  base.iterator();
    	
    	while (it2.hasNext()) {
    		Pair<Value, Constraints> pair = it2.next();
    		Constraints cAtFlow1 = GetConstraintsInArray((ArraySparseSet) concatinatedConstraints, pair.getO1());
    		if(cAtFlow1 == null)
    			concatinatedConstraints.add(pair);
		}
    	
    	
    	return concatinatedConstraints;
	}
	
	
	private boolean testFlow(ArraySparseSet flow)
	{
		ArrayList<String> vars = new ArrayList<String>();
		for (Object object : flow) {
			Pair<Value, Constraints> pair = (Pair<Value, Constraints>) object;
			if(vars.contains(pair.getO1().toString()))
				return false;
			vars.add(pair.getO1().toString());
		}
		return true;
	}
	
	/*Overridden methods*/
    
    @Override
	protected void flowThrough(Object in, Object unit, Object out) {
		// TODO Auto-generated method stub
    	ArraySparseSet
        inFlow = (ArraySparseSet) in,
    	outFlow = (ArraySparseSet) out;
    	ArraySparseSet ConstraintsAtUnit = (ArraySparseSet) getUnitToConstraintSet(unit);
    	//ArraySparseSet merged = mergeConstraints(inFlow, ConstraintsAtUnit);
    	
    	ArraySparseSet concatenated = (ArraySparseSet) concatConstraints(inFlow, ConstraintsAtUnit);
    	outFlow.clear();
    	if(outFlow.size() != 0)
    	{
    		int a=0;
    	}
    	for (Object object : concatenated) {
			outFlow.add(object);
		}
    	if(!testFlow(outFlow))
    	{
    		int a=0;
    	}
	}
    
	@Override
	protected void copy(Object source, Object dest) {
		// TODO Auto-generated method stub
		/*used for providing a "deeper" copy of the objects in the flow*/
		ArraySparseSet 
        sourceSet = (ArraySparseSet) source,
        destSet = (ArraySparseSet) dest;
		destSet.clear();

		for (Object object : sourceSet) {
			Pair<Value, Constraints> srcPair = (Pair<Value, Constraints>) object;
			Constraints destConst = new Constraints(srcPair.getO1());
			Constraints srcConst = srcPair.getO2();
			for (ArrayList<soot.jimple.Stmt> srcStmtList : srcConst.getConstraints()){
				ArrayList<soot.jimple.Stmt> destStmtList = new ArrayList<soot.jimple.Stmt>();
				for (soot.jimple.Stmt stmt : srcStmtList) {
					destStmtList.add(stmt);
				}
				destConst.addNewContstraint(destStmtList);
			}
			
			Pair<Value, Constraints> destPair = new Pair<Value, Constraints>(srcPair.getO1(), destConst);
			destSet.add(destPair);
		}
		if(!testFlow(destSet))
    	{
    		int a=0;
    	}
	}
	@Override
	protected Object entryInitialFlow() {
		// TODO Auto-generated method stub
		return emptySet.clone();
	}
	@Override
	protected void merge(Object in1, Object in2, Object out) {
		// TODO Auto-generated method stub
		
		ArraySparseSet flow = (ArraySparseSet)in1;
		ArraySparseSet outFlow = (ArraySparseSet)out;
		if(!testFlow(flow))
    	{
    		int a=0;
    	}
		//HashMap<Value, Constraints> flow1 = (HashMap<Value, Constraints>)in1;
		//HashMap<Value, Constraints> flow2 = (HashMap<Value, Constraints>)in2;
		//HashMap<Value, Constraints> mergedFlow = new HashMap<Value, Constraints>();
		
		ArraySparseSet flow1 = (ArraySparseSet)in1;
		ArraySparseSet flow2 = (ArraySparseSet)in2;
		ArraySparseSet merged = mergeConstraints(flow1, flow2);
		for (Object object : merged) {
			outFlow.add(object);
		}
		if(!testFlow(outFlow))
    	{
    		int a=0;
    	}
	}
	@Override
	protected Object newInitialFlow() {
		return emptySet.clone();
	}


	public FlowSet getUnitToConstraintSet(Object unit) {
		return unitToConstraintSet.get(unit);
	}
	
}
