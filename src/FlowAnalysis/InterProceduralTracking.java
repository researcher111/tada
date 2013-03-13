package FlowAnalysis;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;



import FlowSet.ConstraintFlowSet;
import FlowSet.ControlFlowSet;


import soot.Body;
import soot.G;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.InvokeExprBox;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.util.Chain;
import utils.AppUtil;


public class InterProceduralTracking {
	/*Not used anymore*/
	/*
	 * The Class constructs an conducts interprocedural flow 
	 * analysis recursively in a depth first fashion.
	 * todo: optimization: do not invoke a method twice 
	 */
	    //set of constraints for a variable at a statement;
		private HashMap<Unit, FlowSet> constraintsAtUnit;
		//set of parameters to track if this Unit was invoked from some some other unit.
		private HashSet<Value> varsToTrack;
		//the method for which analysis is conducted
		private Body method;
		//Methods invoked from the current method
		private HashMap<Unit, HashSet<InvokeExpr>> methodInvocationsMap;
		//Map of a method invocation in methodInvocationsMap -> constraint flow for the method
		private HashMap<InvokeExpr, HashMap<Unit, FlowSet>> invocationToFlow = new HashMap<InvokeExpr, HashMap<Unit, FlowSet>>(1);
		//current interprocedural depth
		private static int methodCallDepth =0;
		//Max Call depth until which analysis will be done
		private final int MAXCALLDEPTH = 1000;
		private static int maxCallDepth =0;
		//List of branches and their the variable they are tainted with
		private HashMap<Unit, Branch> branchMap = new HashMap<Unit, Branch>(1); 
		/*Stack of constraints at the units in the methods in the call stack 
		 * leading to the current being analyzed. These are combined when needed to get inter-procedural paths*/
		private ArrayList<ConstraintFlowSet> constraintStack;
		private static Stack<String> methodStack = new Stack<String>();
		
		private boolean isControlFlowTrackingOn = false;
		
		/*Gets the constraints by interprocedurally traversing the graphs 
		 * in a depth first fashion
		 * @param graph CFG of the method to analyze
		 * @param varsToTrack initial set of variables to track for taint analysis
		 * @param taintMap initial taintMap at the starting of exploration
		 * @stack stack of method calls that lead to this method
		 * */
	    public InterProceduralTracking(UnitGraph graph, HashSet<Value> varsToTrack, HashMap<Value, HashSet<Value>> taintMap, ArrayList<ConstraintFlowSet> stack, ControlFlowSet cntrlFlowSet, HashSet<Value> cfVarsToTrack, HashMap<Value, HashSet<Value>> cfTaintMap)
	    {
	    	 
	    	
	        if(Options.v().verbose())
	            G.v().out.println("[" + graph.getBody().getMethod().getName() +
	                               "]     Constructing Constraints...");
	        
	        //MyDataFlowAnalysis analysis = new MyDataFlowAnalysis(graph);
	        DataFlowAnalysis dfAnalysis;
        	dfAnalysis = new DataFlowAnalysis(graph, isControlFlowTrackingOn, true);
        	this.branchMap = dfAnalysis.getBranchMap();
	    
	    }
		
		public HashSet<SootMethod> getAllImplementations(SootMethod method) {
			Chain appClasses = Scene.v().getApplicationClasses();
			HashSet<SootClass> implementingClasses = new HashSet<SootClass>(1);
			HashSet<SootMethod> overridingMethods = new HashSet<SootMethod>(1);
			
			
			SootClass t = method.getDeclaringClass();
			if(/*t.isAbstract() || */t.isPhantom() || t.isPhantomClass()){
				try {
					throw new Exception("Need to implement for Plantom Classes");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(t.isAbstract()){
				for (Object object : appClasses) {
					SootClass clazz = (SootClass)object;
					SootClass superClass =  clazz.getSuperclass();
					{
						if(superClass.getName().equals(t.toString()))
						{
							implementingClasses.add(clazz);
							SootMethod m2 =  clazz.getMethod(method.getName(), 
									method.getParameterTypes(), method.getReturnType());
							overridingMethods.add(m2);
						}
					}
				}
			}
			if(t.isInterface())
			{
				for (Object object : appClasses) {
					SootClass clazz = (SootClass)object;
					Chain<SootClass> interfaces =  clazz.getInterfaces();
					for (SootClass sootClass : interfaces) {
						if(sootClass.getName().equals(t.toString()))
						{
							implementingClasses.add(clazz);
							SootMethod m2 =  clazz.getMethod(method.getName(), 
									method.getParameterTypes(), method.getReturnType());
							overridingMethods.add(m2);
						}
					}
				}
			}
			return overridingMethods;
		}

		
		public void setConstraintsAtUnit(HashMap<Unit, FlowSet> constraintsAtUnit) {
			this.constraintsAtUnit = constraintsAtUnit;
		}

		public HashMap<Unit, FlowSet> getConstraintsAtUnit() {
			return constraintsAtUnit;
		}
		
		public FlowSet getConstraintsAtUnit(Unit s){
			return constraintsAtUnit.get(s);
		}

		
		public void setMethod(Body method) {
			this.method = method;
		}

		public Body getMethod() {
			return method;
		}
	

		public HashSet<InvokeExpr> getMethodInvocationsAtUnit(Unit s) {
			return methodInvocationsMap.get(s);
		}


		public void setBranchMap(HashMap<Unit, Branch> branchMap) {
			this.branchMap = branchMap;
		}


		public HashMap<Unit, Branch> getBranchMap() {
			return branchMap;
		}
	
}
