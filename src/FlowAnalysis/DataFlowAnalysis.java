package FlowAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import FlowSet.ConstraintFlowSet;
import FlowSet.ControlFlowSet;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StmtSwitch;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.Pair;
import sootchangedclasses.ForwardFlowAnalysis;
import utils.AppUtil;
import utils.HashMapUtils;

/**
 * Flow analysis starting from a given program point at which dummyMethod is invoked for
 * variables passed as arguments to the method.
 * 
 * The analysis is inter-procedural. Whenever a method is invoked at a statement an analysis foe 
 * that method is invoked interprocedurally
 * 
 * Limitations: Overrding methods (implementing methods are handled)  
 **/

public class DataFlowAnalysis extends ForwardFlowAnalysis{

	FlowSet emptySet = new ConstraintFlowSet();
    private HashMap<Unit, FlowSet> unitToConstraintSet;
    
    /*Indicates whether tracking is on yet*/
    private boolean isTrackinOn = false;
    /*is set after a dummy method is invoked and the next def is added to @variablesToTrack*/
    private boolean addNextDefToTrackList = false;
    /* Variables to track for constraints using data flow*/
    private HashSet<Value> variablesToTrackForDF = new HashSet<Value>();
    /*the name of variable to track received as an argument of dummy method*/
    private String variableToTrack;
    /* a map of variables tainted using df propagation by variables in @variablesToTrack */
    private HashMap<Value, HashSet<Value>> dfTaintMap = new HashMap<Value, HashSet<Value>>();
    /*a map of variables tainted using cf propagation by variables in @variablesToTrack */
    /*The set of the statements invoking methods */
    private HashMap<Unit, HashSet<InvokeExpr>> methodInvocationsMap = new HashMap<Unit, HashSet<InvokeExpr>>(1);
    //List of branches and their the variable they are tainted with
	private HashMap<Unit, Branch> branchMap = new HashMap<Unit, Branch>(1); 
	/*Return statement map : return statement -> variables tainting the statement */
	private HashMap<Unit, HashSet<Value>> retStmtmap = new HashMap<Unit, HashSet<Value>>(1);
	/*Method of this analysis*/
	private SootMethod method;
	/*Call Stack*/
	ArrayList<ConstraintFlowSet> constraintStack;
	/*intraprocedural DF analysis for this method*/
	private DataFlowAnalysis intraProcDFAnalysis;
	/*intraprocedural CF analysis for this method*/
	private ControlFlowAnalysis intraProcCFAnalysis;
	/*Method Call Stack*/
	private static Stack<String> methodStack = new Stack<String>();
	private static HashMap<String, HashSet<MethodArgumentsTaintState>> exploredMethods = new HashMap<String, HashSet<MethodArgumentsTaintState>>();
	/*bool value indication whether control-flow tracking is on or not*/
	private boolean isCFTrackingOn;
	/*bool value to indicate whether this analysis is inter-procedural*/
	private boolean isInterProcedural = false;
    /*bool value indicationg whether to explore already explored methods*/
	private boolean skipAlreadyExploredMethods = false;
	static int numExplored =0;
	static int maxStackDepth =0;
	/**
	 *
	 */
	
	public DataFlowAnalysis(UnitGraph grsootaph, HashSet<Value> varsToTrack, HashMap<Value, HashSet<Value>> map, ArrayList<ConstraintFlowSet> stack, boolean isCFTrackingOn, boolean isInterProcedural, ControlFlowSet entryControlFlowSet) {
		super(graph);
		MethodArgumentsTaintState invocationState = new MethodArgumentsTaintState(map, entryControlFlowSet);
		//If the method is calling itself do not analyze..may produce some imprecise results though
		if(isInterProcedural && !checkMethodStack(graph))
			return;
		if(isCFTrackingOn && isInterProcedural){
			/*If CFTracking is on we do intraprocedural analysis of this method
			 * The results of the analysis are used for the control-flow analysis of the method.
			 * the entrySet of the controlflow analysis is passed as argument from the calling context of this analysis
			*/
			intraProcDFAnalysis = new DataFlowAnalysis(graph, 
					varsToTrack, map, stack, false, false, new ControlFlowSet());
			intraProcCFAnalysis = new ControlFlowAnalysis(graph, intraProcDFAnalysis, 
					entryControlFlowSet, varsToTrack, map);
			
			/*Copy the branchmap to the branchMap of this analysis*/
			HashMap<Unit, Branch> branchMap = intraProcCFAnalysis.getBranchMap();
			updateBranchMapFromThis(branchMap);
			
		}
		System.out.println(++numExplored +  " Stack Depth: " + methodStack.size() + "/" + maxStackDepth + " Exploring: ..." + graph.getBody().getMethod().getSignature());
		if(TaDaFinalView.progressBar != null)
			  TaDaFinalView.progressBar.setString("Inv Stack Depth: " + methodStack.size() + " Exploring: " + graph.getBody().getMethod().getName());
		this.constraintStack = stack;
		this.method = graph.getBody().getMethod();
		this.variablesToTrackForDF = varsToTrack;
    	this.dfTaintMap = map;
    	/*is true if invoked from ...InterProc*/
    	isTrackinOn = false;
		/*If the method is annotated as a TaDa method*/
    	updateTaintMapIfTaDaMethod();
		
		this.isCFTrackingOn = isCFTrackingOn;
		if(methodStack.size() > maxStackDepth)
			maxStackDepth = methodStack.size();
		
    	
    	this.isInterProcedural = isInterProcedural;
		doFlowAnalysis(graph);
		if(isInterProcedural){
			methodStack.remove(graph.getBody().getMethod().getSignature());
			invocationState.setAnalysisForThisState(this);
			/*Chache the analysis so that we can use it for interprocedural analysis
			 * when the method is invoked with the same taint state*/
			addMethodToExploredSet(graph.getBody().getMethod().getSignature(), invocationState);
		}
	}

	private void updateTaintMapIfTaDaMethod() {
		if(MethodInvocationMap.getTadaMethods().contains(method)){
			isTrackinOn = true;
			Set<Value> annotatedVars = MethodInvocationMap.getVarDatabaseAttributeMap(method).keySet();
			this.variablesToTrackForDF.addAll(annotatedVars);
			for (Value value : annotatedVars) {
				if(!this.dfTaintMap.containsKey(value)){
					this.dfTaintMap.put(value, new HashSet<Value>());
				} else
					try {
						//throw new Exception("TaintMap already contains var " + value);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			
		}
	}

	private void updateBranchMapFromThis(HashMap<Unit, Branch> branchMap) {
		for (Unit u : branchMap.keySet()) {
			Branch b = branchMap.get(u);
			if(this.branchMap.get(u) == null)
				this.branchMap.put(u, b);
			else{
				this.branchMap.get(u).addControlFlowTaintVars(b.getControlFlowTaintVars());
				this.branchMap.get(u).addDataFlowTaintVars(b.getDataFlowTaintVars());
				this.branchMap.get(u).addCntrlDeptStmts(b.getContrlDependentStmts());
			}
		}
	}
	
	/*Checks if the call stack has the same method on 
	 * top called again to prevent from infinite loop*/
	private boolean checkMethodStack(UnitGraph graph) {
		// TODO Auto-generated method stub
		String signature = graph.getBody().getMethod().getSignature();
    	if(!methodStack.isEmpty() && methodStack.peek().equals(signature)) 
    		return false;
    	methodStack.push(graph.getBody().getMethod().getSignature());
		return true;
	}

	public DataFlowAnalysis(UnitGraph graph, boolean isCFTrackingOn, boolean isInterProc) {
		super(graph);
		if(isInterProc && !checkMethodStack(graph))
			return;
		if(isCFTrackingOn){
			intraProcDFAnalysis = new DataFlowAnalysis(graph, 
					false, false);
			intraProcCFAnalysis = new ControlFlowAnalysis(graph, intraProcDFAnalysis, 
					new ControlFlowSet(), new HashSet<Value>(), new HashMap<Value, HashSet<Value>>());
			/*Copy the branchmap to the branchMap of this analysis*/
			HashMap<Unit, Branch> branchMap = intraProcCFAnalysis.getBranchMap();
			updateBranchMapFromThis(branchMap);
		}
		isInterProcedural = isInterProc;
		this.isCFTrackingOn = isCFTrackingOn;
		this.method = graph.getBody().getMethod();
		updateTaintMapIfTaDaMethod();
		constraintStack = new ArrayList<ConstraintFlowSet>();
		doFlowAnalysis(graph);
		if(isInterProcedural)
			methodStack.remove(graph.getBody().getMethod().getSignature());
	}

	
	
	private void doFlowAnalysis(UnitGraph graph) {
		unitToConstraintSet = new HashMap<Unit, FlowSet>();
        {
            Iterator unitIt = graph.iterator();
            while(unitIt.hasNext()){
                Unit s = (Unit) unitIt.next();
                if(isInterProcedural){
	                boolean isMethodInvoked =  updateMethodInvocationMap(s);               
	                /*Do flow analysis of the method invoked here*/
	                if(isMethodInvoked){
	                	for (InvokeExpr invocation: methodInvocationsMap.get(s)) {
	                		doAnalysisFor(invocation, s);
						}
	                }
                }
            	ExtractConstraintSwitch constraintSwitch = new ExtractConstraintSwitch(s);
            	s.apply(constraintSwitch);
            	HashSet<Pair<Value, Constraints>> constraintValuePairs = constraintSwitch.getConstraintValuePair();
            	FlowSet genSet = constraintSwitch.getGenSet();
            	if(constraintValuePairs != null && constraintValuePairs.size() > 0){
            		for (Pair<Value, Constraints> pair : constraintValuePairs) 
            			genSet.add(pair, genSet);
            	}
            	unitToConstraintSet.put(s, genSet);
            }
        }
		doAnalysis();
	}
	
	private void doAnalysisFor(InvokeExpr invocation, Unit s) {
		// TODO Auto-generated method stub
		Body methodBody = null;
		if(invocation.toString().contains("@primitive.Unknown")){
			System.out.println("no active body for : " + method.getSignature());
			return;
		}
		SootMethod method = invocation.getMethod();
		if(method.isConcrete()){
			if(!method.hasActiveBody()){
				System.out.println("no active body for : " + method.getSignature());
				return;
			}
			else {
				methodBody = method.getActiveBody();
			}
			trackMethod(invocation, methodBody, s);
		}
		else if(method.isPhantom()){
			try {
				throw new Exception("Need to implement for phantom methods");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if(method.isAbstract()){
			//Need to get all the methods that can be invoked and traverse all those for a conservative approach
			//For less conservative, we can pick a single method using some heuristics
			HashSet<SootMethod> implementations =  getAllImplementations(method);
			for (SootMethod sootMethod : implementations) {
				if(sootMethod.getActiveBody() == null){
					int debug = 1;
				}
				else
					trackMethod(invocation, sootMethod.getActiveBody(), s);
			}
			
		}
		else{
			if(!method.hasActiveBody()){
				System.out.println("no active body for : " + method.getSignature());
				return;
			}
			
			try {
				throw new Exception("Need to implement for non-concrete methods");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//Find the arguments that need to be tracked
	}

	/*Gets all the methods implementing an abstract method*/
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
						if(clazz.isInterface()){
							//get methods implementing this interface
							try {
								SootMethod m = clazz.getMethod(method.getSubSignature());
								overridingMethods.addAll(getAllImplementations(m));
								//throw new Exception("Not Implemented Yet");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								//Chain<SootClass> intInterfaces =  clazz.getInterfaces();
								e.printStackTrace();
								continue;
							}
						}
						implementingClasses.add(clazz);
						try{
							SootMethod m2 =  clazz.getMethod(method.getName(), 
									method.getParameterTypes(), method.getReturnType());
							overridingMethods.add(m2);
						}
						catch(Exception e){
							System.out.println("No Method " + method.getName() + " found in " + clazz + " implementing " + t);
						}
					}
				}
			}
		}
		return overridingMethods;
	}
	
	private void trackMethod(InvokeExpr invocation, Body methodBody, Unit s) {
		// TODO Auto-generated method stub
		//invocation.getArgs()
		//Find the arguments that need to be tracked
		int n = invocation.getArgCount();
		HashMap<Value, HashSet<Value>> dfTaintMap = new HashMap<Value, HashSet<Value>>(1);
		HashSet<Value> dfVariablesToTrack = new HashSet<Value>(1);
		
		HashMap<Value, HashSet<Value>> cfTaintMap = new HashMap<Value, HashSet<Value>>(1);
		HashSet<Value> cfVariablesToTrack = new HashSet<Value>(1);
		
		for (int i=0; i<n; i++) {
			Value arg = invocation.getArg(i);
			if(arg instanceof soot.jimple.Constant) continue;
			if(!(arg instanceof JimpleLocal)){
				try {
					throw new Exception("JimpleLocal expected as an argument for a method invocation");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			/*Track the parameters that are tainted by a variable that is being tracked cor data flow analysis*/
			if(this.getVariablesToTrackForDF().contains(arg)){
					//need to get the value object for tainted arguments
					Local parameter = methodBody.getParameterLocal(i);
					HashSet<Value> paramToTrack = new HashSet<Value>();
					paramToTrack.add(parameter);
					HashSet<Value> vars = this.dfTaintedFrom(arg);
					for (Value value : vars) {
						if(dfTaintMap.get(value) == null)
							dfTaintMap.put(value, paramToTrack);
						else{
							dfTaintMap.get(value).addAll(paramToTrack);
						}
						dfVariablesToTrack.add(value);
						dfVariablesToTrack.add(parameter);
					}
					//add the corresponding method parameters to track
			}
			if(intraProcCFAnalysis != null && intraProcCFAnalysis.getCfVarsToTrack().contains(arg)){
				//need to get the value object for tainted arguments
				Local parameter = methodBody.getParameterLocal(i);
				HashSet<Value> paramToTrack = new HashSet<Value>();
				paramToTrack.add(parameter);
				HashSet<Value> vars = intraProcCFAnalysis.cfTaintedFrom(arg);
				for (Value value : vars) {
					if(cfTaintMap.get(value) == null)
						cfTaintMap.put(value, paramToTrack);
					else{
						cfTaintMap.get(value).addAll(paramToTrack);
					}
					cfVariablesToTrack.add(value);
					cfVariablesToTrack.add(parameter);
				}
				//add the corresponding method parameters to track
			}
		}
		/*If we want to skip methods already explored with the same taint state*/
		if(skipAlreadyExploredMethods){
			if(isCFTrackingOn){
				DataFlowAnalysis analysis = IsMethodExplored(invocation.getMethod().getSignature(), cfTaintMap, (ControlFlowSet) intraProcCFAnalysis.getFlowAfter(s));
				if(analysis != null){//if the method is explored already
					//Do not explore this method if the method has been explored with current taint state
					updateCurrentAnalysis(s, analysis);
					ControlFlowSet cntrlFlowSet = (ControlFlowSet) intraProcCFAnalysis.getFlowAfter(s);
					/*results of exploration are retrieved from cache*/
					updateBranchMapForCurrentControlFlowSet(invocation.getMethod(), cntrlFlowSet);
					return;
				}
			}
			if(!isCFTrackingOn){
				DataFlowAnalysis analysis = IsMethodExplored(invocation.getMethod().getSignature(), dfTaintMap, new ControlFlowSet());
				if(analysis != null){
					//exploredMethods.put(invocation.getMethod().getSignature(), new MethodArgumentsTaintState(dfTaintMap));
					//Do not explore this method if the method has been explored with current taint state
					updateCurrentAnalysis(s, analysis);
					return;
				}
			}
		}
		
		/*TODO: need to figure out the stack*/
		ArrayList<ConstraintFlowSet> stack = new ArrayList<ConstraintFlowSet>(this.constraintStack);
		/*get the results from a light weight intra-procedural analysis
		 * the results are used by control-flow analysis*/
		
		DataFlowAnalysis methodAnalysis;
		
		if(isCFTrackingOn){
			ControlFlowSet cntrlFlowSet = (ControlFlowSet) intraProcCFAnalysis.getFlowAfter(s);
			methodAnalysis = new DataFlowAnalysis(new ExceptionalUnitGraph(methodBody), 
					cfVariablesToTrack, cfTaintMap, stack, true, true, cntrlFlowSet);
			
		}
		else{
			methodAnalysis = new DataFlowAnalysis(new ExceptionalUnitGraph(methodBody), 
					dfVariablesToTrack, dfTaintMap, stack, false, true, new ControlFlowSet());
		}
		/*get the vars that taint return values of the invoked method*/
		updateCurrentAnalysis(s, methodAnalysis);
	}

	private void updateBranchMapForCurrentControlFlowSet(
			SootMethod method, ControlFlowSet cntrlFlowSet) {
		if(!method.hasActiveBody()) return;
		for(soot.jimple.Stmt cntrlStmt : cntrlFlowSet.getCntrlTaintFlow().keySet()){
			if(branchMap.get(cntrlStmt) != null){
				for(Unit stmtInMethod : method.getActiveBody().getUnits()){
					//branchMap.get(cntrlStmt).getContrlDependentStmts().add((soot.jimple.Stmt) stmtInMethod);
					branchMap.get(cntrlStmt).addCntrlDeptStmt(method, (soot.jimple.Stmt) stmtInMethod);
					if(branchMap.containsKey(stmtInMethod)){
						branchMap.get(stmtInMethod).getControlFlowTaintVars().addAll(branchMap.get(cntrlStmt).getControlFlowTaintVars());
					}
				}
			}
			else{
				int debug =1;
				int a= debug;
			}
		}
		if(MethodInvocationMap.methodsInvokedByThisMethod.get(method) == null)
			return;
		for(SootMethod invokedMethod  : MethodInvocationMap.methodsInvokedByThisMethod.get(method)){
			if(!invokedMethod.equals(method))
				updateBranchMapForCurrentControlFlowSet(invokedMethod, cntrlFlowSet);
		}
	}

	/***
	 * If method is explored with the same taint state ...returns the analysis results from before
	 * @param signature
	 * @param map
	 * @return
	 */
	private DataFlowAnalysis IsMethodExplored(String signature,
			HashMap<Value, HashSet<Value>> map, ControlFlowSet flowSet) {
		// TODO Auto-generated method stub
		HashSet<MethodArgumentsTaintState> exploredStates = exploredMethods.get(signature);
		MethodArgumentsTaintState thisState = new MethodArgumentsTaintState(map, flowSet);
		if(exploredStates == null){
			return null;
		}
		else{
			for (MethodArgumentsTaintState methodArgumentsTaintState : exploredStates) {
				if(thisState.equals(methodArgumentsTaintState))
					return methodArgumentsTaintState.getAnalysisForThisState();
			}
			return null;
		}
	}
	
	private boolean addMethodToExploredSet(String signature,
			MethodArgumentsTaintState state) {
		// TODO Auto-generated method stub
		HashSet<MethodArgumentsTaintState> exploredStates = exploredMethods.get(signature);
		if(exploredStates == null){
			exploredStates = new HashSet<MethodArgumentsTaintState>();
			exploredStates.add(state);
			exploredMethods.put(signature, exploredStates);
			return false;
		}
		else{
			for (MethodArgumentsTaintState methodArgumentsTaintState : exploredStates) {
				if(state.equals(methodArgumentsTaintState))
					return true;
			}
			exploredStates.add(state);
			return false;
		}
	}
	

	
	private void updateCurrentAnalysis(Unit s,
			DataFlowAnalysis methodAnalysis) {
		HashSet<Value> vars = methodAnalysis.getReturnTaintVars();
		//this.branchMap.putAll(methodAnalysis.branchMap);
		updateBranchMapFromThis(methodAnalysis.branchMap);
		
		if(vars.size()>0){
			// Taint all the defs in the statement with the set returned and add them to vars to track
			// make isTrackingOn true if the current statement has non zero definitions
			if(s.getDefBoxes().size()>0){
				isTrackinOn = true; 
				for (Value use : vars) {
					if(!variablesToTrackForDF.contains(use)){
						variablesToTrackForDF.add(use);
						dfTaintMap.put(use, new HashSet<Value>());						
					}
					for (ValueBox defBox : s.getDefBoxes()) {
						updateVariablesToTrackForDF(use, defBox.getValue());
					}
				}
			}
		}
	}

	private HashSet<Value> getReturnTaintVars() {
		// TODO Auto-generated method stub
		HashSet<Value> allTaintingVarsReturned = new HashSet<Value>();
		for (Unit u : retStmtmap.keySet()) {
			allTaintingVarsReturned.addAll(retStmtmap.get(u));
		}
		return allTaintingVarsReturned;
	}

	/*From a statement extract all the 
	 * method invocation statements and put in @methodInvocationsMap
	 * return true if there is at least one method invoke at the statement s
	 * */
	private boolean updateMethodInvocationMap(Unit s) {
		//if(isCFTrackingOn() || processInvocation(s)){		
		//keep a track of invocations for building inter-procedural graph
		ExtractMethodInvocationStmtSwitch stmtSwitch = new ExtractMethodInvocationStmtSwitch();
		s.apply(stmtSwitch);
		HashSet<InvokeExpr>  invocations = stmtSwitch.getMethodInvocations();
		if(invocations.size() > 0){
			methodInvocationsMap.put(s, invocations);
			return true;
		}
		//}
		return false;
	}
	
	/*Required for interprocedural analysis...don't go into method invocations that
	 * do not pass as an argument any variable that needs to be tracked.
	 * */
	private boolean processInvocation(Unit s)
	{
		
		List<ValueBox> uses = s.getUseBoxes();
		for (ValueBox val : uses) {
			if(variablesToTrackForDF.contains(val.getValue()))
				return true;
		}
		return false;
	}

	private void updateVariablesToTrackForDF(Value use, Value def) {
		
		if(variablesToTrackForDF.contains(use)){
			variablesToTrackForDF.add(def);
			updateDFTaintMap(def, use);
		}
		
	}
	
	
	private void updateVariablesToTrackForDF(List<ValueBox> uses, List<ValueBox> defs) {
		for (ValueBox defBox : defs) {
			Value def = defBox.getValue();
			//if(variablesToTrack.contains(def)) continue;
			
			for (ValueBox useBox : uses) {
				if(variablesToTrackForDF.contains(useBox.getValue())){
					variablesToTrackForDF.add(def);
					updateDFTaintMap(def, useBox.getValue());
				}
					
			}
		}
	}
	
	/* Updates the taintMap 
	 * @parameter var is tanted from @parameter taintedFrom
	 * The method adds @var in the taintMap
	 * */
	private void updateDFTaintMap(Value var, Value taintedFrom) {
		// TODO Auto-generated method stub
		HashMap<Value, HashSet<Value>> copyOfTaintMap = new HashMap<Value, HashSet<Value>>();
		copyOfTaintMap = dfTaintMap;
		for (Value v : copyOfTaintMap.keySet()) {
			if(v.equals(taintedFrom)){
				dfTaintMap.get(v).add(var);
				continue;
			}
			
			HashSet<Value> vars = dfTaintMap.get(v);
			for (Value taintedFromV : vars) {
				if(taintedFromV.equals(taintedFrom)){
					dfTaintMap.get(v).add(var);
					break;
				}
			}
				
		}
	}

	
	/*
	 * Returns a variable @var whose tracking is on 
	 * and the variable @def is tainted by @var
	 */
	public HashSet<Value> dfTaintedFrom(Value def) {
		// TODO Auto-generated method stub
		HashSet<Value> values = new HashSet<Value>();
		if(dfTaintMap.keySet().contains(def)){
			values.add(def);
			return values;
		}
		
		for (Value var : dfTaintMap.keySet()) {
			HashSet<Value> uses = dfTaintMap.get(var);
			if(uses.contains(def)){
				values.add(var);
			}
		}
		return values;
	}

	
	/*For debugging purposes -> to check whether a flow has duplicates*/
	private boolean testFlow(ConstraintFlowSet flow)
	{
		ArrayList<String> vars = new ArrayList<String>();
		for (Object object : flow.toList()) {
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
    	ConstraintFlowSet
        inFlow = (ConstraintFlowSet) in,
    	outFlow = (ConstraintFlowSet) out;
    	ConstraintFlowSet ConstraintsAtUnit = (ConstraintFlowSet) getUnitToConstraintSet(unit);
    	//ArraySparseSet merged = mergeConstraints(inFlow, ConstraintsAtUnit);
    	
    	ConstraintFlowSet concatenated = inFlow.concatConstraints(ConstraintsAtUnit);
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
		ConstraintFlowSet 
        sourceSet = (ConstraintFlowSet) source,
        destSet = (ConstraintFlowSet) dest;
		destSet.clear();

		ConstraintFlowSet clonedSet = (ConstraintFlowSet) sourceSet.clone();
		for (Object object : clonedSet.toList()) {
			destSet.add(object);
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
		
		ConstraintFlowSet flow = (ConstraintFlowSet)in1;
		ConstraintFlowSet outFlow = (ConstraintFlowSet)out;
		if(!testFlow(flow))
    	{
    		int a=0;
    	}
		
		ConstraintFlowSet flow1 = (ConstraintFlowSet)in1;
		ConstraintFlowSet flow2 = (ConstraintFlowSet)in2;
		ConstraintFlowSet merged = flow1.mergeConstraints(flow2);
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
	
	
	public void setMethodInvocationsMap(HashMap<Unit, HashSet<InvokeExpr>> methodInvocationsMap) {
		this.methodInvocationsMap = methodInvocationsMap;
	}

	public HashMap<Unit, HashSet<InvokeExpr>> getMethodInvocationsMap() {
		return methodInvocationsMap;
	}



	public HashSet<Value> getVariablesToTrackForDF() {
		return variablesToTrackForDF;
	}


	public HashMap<Unit, Branch> getBranchMap() {
		return branchMap;
	}
	
	/*gets the set of variables tainting any of the uses at a given statement
	 * The method tracks all the interprocedural paths up to this statement using the constraintSet.*/
	/*
	public HashSet<Value> getCoCompositionalConstraints(Stmt stmt)
	{
		
	}*/
	

	

	public HashMap<Value, HashSet<Value>> getDfTaintMap() {
		return dfTaintMap;
	}
	
	/*Just for debugging purposes*/
	private int CountBranchesWithTainting(HashMap<Unit, Branch> branchMap) {
	
		int count =0;
		for (Unit stmt : branchMap.keySet()) {
			Branch b = branchMap.get(stmt);
			if(b.getDataFlowTaintVars().size() ==0 && b.getControlFlowTaintVars().size() ==0) continue;
			else count++;
		}
		return count;
		
	}


	

	public void setIntraProcCFAnalysis(ControlFlowAnalysis intraProcCFAnalysis) {
		this.intraProcCFAnalysis = intraProcCFAnalysis;
	}

	public ControlFlowAnalysis getIntraProcCFAnalysis() {
		return intraProcCFAnalysis;
	}




	/*Class implementing visitor pattern for getting 
	 * constraints given a statement*/
	class ExtractConstraintSwitch implements StmtSwitch{

		private FlowSet genSet;
		private HashSet<Pair<Value, Constraints>> constraintValuePairs;
		boolean processConstraints = false;
		
		public ExtractConstraintSwitch(Unit s){
            setGenSet(emptySet.clone());
            List<ValueBox> uses = s.getUseBoxes();
            List<ValueBox> defs = s.getDefBoxes();
            Pair<Value, Constraints> constraintValuePair = null;
            
    		if(addNextDefToTrackList){
    			for (Object obj : s.getUseAndDefBoxes()) {
    				ValueBox defUse = (ValueBox) obj;
    				boolean isAdded = addToVariablesToTrack(defUse.getValue());
    				if(isAdded)
    					addNextDefToTrackList = false;
    			}
				
    		}
    		defs = s.getDefBoxes();
    		updateVariablesToTrackForDF(uses, defs);
    		processConstraints = false;
            for (ValueBox defBox : defs) {
				if(getVariablesToTrackForDF().contains(defBox.getValue()))
					processConstraints = true;
			}
			
		}
		
		
		@Override
		public void caseAssignStmt(AssignStmt stmt) {
			// TODO Auto-generated method stub
			if (!isTrackinOn || !processConstraints){
            	unitToConstraintSet.put(stmt, getGenSet());
				return;
            }
			setConstraintValuePairs(getConstraintsForAssignment(stmt));
		}

		@Override
		public void caseBreakpointStmt(BreakpointStmt stmt) {
			// TODO Auto-generated method stub
			try {
				throw new Exception("Not Implemented for statement : " + stmt.getClass().toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
			// TODO Auto-generated method stub
			try {
				throw new Exception("Not Implemented for statement : " + stmt.getClass().toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
			// TODO Auto-generated method stub
			try {
				throw new Exception("Not Implemented for statement : " + stmt.getClass().toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void caseGotoStmt(GotoStmt stmt) {
			// TODO Auto-generated method stub
			if (!isTrackinOn || !processConstraints){
            	unitToConstraintSet.put(stmt, getGenSet());
				return;
            }
			unitToConstraintSet.put(stmt, getGenSet());
		}

		@Override
		public void caseIdentityStmt(IdentityStmt stmt) {
			// TODO Auto-generated method stub
			if (!isTrackinOn || !processConstraints){
            	unitToConstraintSet.put(stmt, getGenSet());
				return;
            }
			setConstraintValuePairs(getConstraintForIdentity(stmt));
		}

		@Override
		public void caseIfStmt(IfStmt stmt) {
			/*System.out.println("istrackingon : " + isTrackinOn + " processConstraints: " + processConstraints);
			if (!isTrackinOn || !processConstraints){
            	unitToConstraintSet.put(stmt, getGenSet());
				return;
            }
			unitToConstraintSet.put(stmt, getGenSet());
			*/
			List<ValueBox> useBoxes = stmt.getUseBoxes();
			for( ValueBox use : useBoxes){
				/*Set of variables that taint condition in this if statement*/
				HashSet<Value> vars = new HashSet<Value>();
				if(variablesToTrackForDF.contains(use.getValue())){
					HashSet<Value> var = dfTaintedFrom(use.getValue());
					vars.addAll(var);
				}
				if(vars.size()>0){
					if(branchMap.get(stmt) == null){
						Branch b = new Branch(method, stmt);
						b.addDataFlowTaintVars(vars);
						branchMap.put(stmt, b);
					}
					else{
						branchMap.get(stmt).getDataFlowTaintVars().addAll(vars);
					}
				}
				
			}
		}

		@Override
		public void caseInvokeStmt(InvokeStmt stmt) {
			// TODO Auto-generated method stub
			/*still to write code for static invocations and invocations without a receiver object*/
			InvokeExprBox box = (InvokeExprBox) ((InvokeStmt)stmt).getInvokeExprBox();
        	if(box.getValue() instanceof JVirtualInvokeExpr)
    		{
    			JVirtualInvokeExpr invokeExpr = (JVirtualInvokeExpr) box.getValue();
    			if(invokeExpr.getMethodRef().name().equals("dummyMethod")){
    				isTrackinOn = true;
    				addNextDefToTrackList = true;
    				variableToTrack = invokeExpr.getArg(0).toString();
    				if(variableToTrack.indexOf('"') != -1)
    					variableToTrack = variableToTrack.substring(1, variableToTrack.length()-1);
    				unitToConstraintSet.put(stmt, getGenSet());
    				return;
    			}
    			else if(!processConstraints){ 
    				unitToConstraintSet.put(stmt, getGenSet());
    				return;
    			}
    		}
        	if (!isTrackinOn || !processConstraints){
            	unitToConstraintSet.put(stmt, getGenSet());
				return;
            }
        	setConstraintValuePairs(getConstraintForInvocation(stmt));
		}

		@Override
		public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
			// TODO Auto-generated method stub
			List<ValueBox> useBoxes = stmt.getUseBoxes();
			for( ValueBox use : useBoxes){
				/*Set of variables that taint condition in this if statement*/
				HashSet<Value> vars = new HashSet<Value>();
				if(variablesToTrackForDF.contains(use.getValue())){
					HashSet<Value> var = dfTaintedFrom(use.getValue());
					vars.addAll(var);
				}
				if(vars.size()>0){
					if(branchMap.get(stmt) == null){
						Branch b = new Branch(method, stmt);
						b.addDataFlowTaintVars(vars);
						branchMap.put(stmt, b);
					}
					else{
						branchMap.get(stmt).getDataFlowTaintVars().addAll(vars);
					}
				}
				
			}
		}

		@Override
		public void caseNopStmt(NopStmt stmt) {
			// TODO Auto-generated method stub
			if (!isTrackinOn || !processConstraints){
            	unitToConstraintSet.put(stmt, getGenSet());
				return;
            }
			unitToConstraintSet.put(stmt, getGenSet());
		}

		@Override
		public void caseRetStmt(RetStmt stmt) {
			// TODO Auto-generated method stub
			if(isTrackinOn){
				for (Object obj : stmt.getUseAndDefBoxes()) {
					ValueBox v = (ValueBox)obj;
					retStmtmap.put(stmt, dfTaintedFrom(v.getValue()));
				}
			}
			if (!isTrackinOn || !processConstraints){
            	unitToConstraintSet.put(stmt, getGenSet());
				return;
            }
			unitToConstraintSet.put(stmt, getGenSet());
		}

		@Override
		public void caseReturnStmt(ReturnStmt stmt) {
			// TODO Auto-generated method stub
			if(isTrackinOn){
				for (Object obj : stmt.getUseAndDefBoxes()) {
					ValueBox v = (ValueBox)obj;
					retStmtmap.put(stmt, dfTaintedFrom(v.getValue()));
				}
			}
			if (!isTrackinOn || !processConstraints){
            	unitToConstraintSet.put(stmt, getGenSet());
				return;
            }
			unitToConstraintSet.put(stmt, getGenSet());
		}

		@Override
		public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
			// TODO Auto-generated method stub
			if (!isTrackinOn || !processConstraints){
            	unitToConstraintSet.put(stmt, getGenSet());
				return;
            }
			unitToConstraintSet.put(stmt, getGenSet());
		}

		@Override
		public void caseTableSwitchStmt(TableSwitchStmt stmt) {
			// TODO Auto-generated method stub
			List<ValueBox> useBoxes = stmt.getUseBoxes();
			for( ValueBox use : useBoxes){
				/*Set of variables that taint condition in this if statement*/
				HashSet<Value> vars = new HashSet<Value>();
				if(variablesToTrackForDF.contains(use.getValue())){
					HashSet<Value> var = dfTaintedFrom(use.getValue());
					vars.addAll(var);
				}
				if(vars.size()>0){
					if(branchMap.get(stmt) == null){
						Branch b = new Branch(method, stmt);
						b.addDataFlowTaintVars(vars);
						branchMap.put(stmt, b);
					}
					else{
						branchMap.get(stmt).getDataFlowTaintVars().addAll(vars);
					}
				}
				
			}
		}

		@Override
		public void caseThrowStmt(ThrowStmt stmt) {
			
		}

		@Override
		public void defaultCase(Object obj) {
			// TODO Auto-generated method stub
			try {
				throw new Exception("Not Implemented for statement : " + obj.getClass().toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		
		
		private boolean addToVariablesToTrack(Value val) {
			String varName = val.toString();
			
			if(val.toString().equals(variableToTrack)){
				getVariablesToTrackForDF().add(val);
				getDfTaintMap().put(val, new HashSet<Value>());
				return true;
			}
			return false;
		}
		
		private HashSet<Pair<Value, Constraints>> getConstraintForInvocation(Unit s) {
			InvokeStmt invocation = (InvokeStmt) s;
			InvokeExpr expr = invocation.getInvokeExpr();
			InvokeExprBox box = (InvokeExprBox) invocation.getInvokeExprBox();
			HashSet<Pair<Value, Constraints>> valueConstraintPairs = new HashSet<Pair<Value,Constraints>>(1);
			
			if (box.getValue() instanceof JSpecialInvokeExpr) {
				JSpecialInvokeExpr invokeExpr = (JSpecialInvokeExpr) box.getValue();
				Value receiverObject = invokeExpr.getBase();
				HashSet<Value> vals = dfTaintedFrom(receiverObject);

				for (Object obj : invocation.getUseAndDefBoxes()) {
					ValueBox defUse = (ValueBox) obj;
					String so = defUse.getValue().toString();
				}
				
				for (Value value : vals) {
					Constraints consForReceiverObject = new Constraints(value);
					consForReceiverObject.addNewContstraint(invocation);
					Pair<Value, Constraints> valueConstraintPair = new Pair<Value, Constraints>(value, consForReceiverObject);
					valueConstraintPairs.add(valueConstraintPair);
				}
				
				
				
				return valueConstraintPairs;
				//constraintsAtUnit.put(s, map);
			} 
			else if(box.getValue() instanceof JVirtualInvokeExpr)
			{
				JVirtualInvokeExpr invokeExpr = (JVirtualInvokeExpr) box.getValue();
				Value receiverObject = invokeExpr.getBase();
				HashSet<Value> vals = dfTaintedFrom(receiverObject);
				
				for (Value value : vals) {
					Constraints consForReceiverObject = new Constraints(value);
					consForReceiverObject.addNewContstraint(invocation);
					Pair<Value, Constraints> valueConstraintPair = new Pair<Value, Constraints>(value, consForReceiverObject);
					valueConstraintPairs.add(valueConstraintPair);
				}
				return valueConstraintPairs;
				
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
	
		private HashSet<Pair<Value, Constraints>> getConstraintsForAssignment(Unit s) {
			
			List<ValueBox> uses = s.getUseBoxes();
			List<ValueBox> defs = s.getDefBoxes();
			HashSet<Pair<Value, Constraints>> valueConstraintPairs = new HashSet<Pair<Value,Constraints>>();
			
			AssignStmt assignment = (AssignStmt) s;
			
			if(defs.size() != 1){
				try {
					throw new Exception("More than 1 definitions!!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Value def =  defs.get(0).getValue();
			HashSet<Value> vals = dfTaintedFrom(def);
			for (Value value : vals) {
				Constraints consForDef = new Constraints(value);
				consForDef.addNewContstraint(assignment);
				Pair<Value, Constraints> valueConstraintPair = new Pair<Value, Constraints>(value, consForDef);
				valueConstraintPairs.add(valueConstraintPair);
			}
			return valueConstraintPairs;
		}
		
		private HashSet<Pair<Value, Constraints>> getConstraintForIdentity(Unit s) {
			JIdentityStmt stmt = (JIdentityStmt)s;
			List<ValueBox> defs = stmt.getDefBoxes();
			Pair<Value, Constraints> valueConstraintPair = null;
			HashSet<Pair<Value, Constraints>> valueConstraintPairs = new HashSet<Pair<Value,Constraints>>();
			
			for (ValueBox valueBox : defs) {
				Value def = valueBox.getValue();
				HashSet<Value> vals = dfTaintedFrom(def);
				for (Value value : vals) {
					Constraints consForDef = new Constraints(value);
					consForDef.addNewContstraint(stmt);
					valueConstraintPair = new Pair<Value, Constraints>(value, consForDef);
					valueConstraintPairs.add(valueConstraintPair);
				}
			}
			return valueConstraintPairs;
		}

		public void setConstraintValuePairs(HashSet<Pair<Value, Constraints>> constraintValuePair) {
			this.constraintValuePairs = constraintValuePair;
		}

		public HashSet<Pair<Value, Constraints>> getConstraintValuePair() {
			return constraintValuePairs;
		}

		public void setGenSet(FlowSet genSet) {
			this.genSet = genSet;
		}

		public FlowSet getGenSet() {
			return genSet;
		}

	}
}
