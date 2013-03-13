package FlowAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import soot.toolkits.scalar.Pair;
import sootchangedclasses.ForwardFlowAnalysis;
import utils.AppUtil;
import utils.HashMapUtils;

public class ControlFlowAnalysis extends ForwardFlowAnalysis{

	/**
	 * Inter Procedural
	 * Taint analysis based on control flow propagation
	 *	uses the results of taint analysis based on data flow propagation
	 *	Hence the analysis is Control Flow + Data Flow
	 *
	 *	May give some false results when a variable is tainted at a statement that is encountered
	 *	after an if/switch statement where the statement was used in the condition
	 */
	
	
	/*Flowset is a set of Values that are a set of variables that control the execution of the unit*/
	public HashMap<Unit, ControlFlowSet> controllingVarsAtUnit;
	/*Control flow taint map*/
	private HashMap<Value, HashSet<Value>> cfTaintMap = new HashMap<Value, HashSet<Value>>(1);
	/*Variables to track*/
	private HashSet<Value> cfVarsToTrack = new HashSet<Value>(1);
	/*Maps of branches and the control-flow variables tainting them*/
	private HashMap<Unit, Branch> branchMap = new HashMap<Unit, Branch>();
	/*Data Flow analysis for this method
	 * the DF taint information will be used in building this analysis*/
	private DataFlowAnalysis dfAnalysis;
	/*Flow at the entry vertex of the method*/
	private ControlFlowSet entryFlow;
	/*This method*/
	private SootMethod method;
	
	
	public ControlFlowAnalysis(UnitGraph graph, DataFlowAnalysis analysis, ControlFlowSet entry, HashSet<Value> cntrlVarsToTrack, HashMap<Value, HashSet<Value>> contrlTaintMap) {
		super(graph);
		this.method = graph.getBody().getMethod();
		this.dfAnalysis = analysis;
		this.entryFlow = entry;
		if(dfAnalysis != null)
			cfVarsToTrack.addAll(dfAnalysis.getVariablesToTrackForDF());
		cfVarsToTrack.addAll(cntrlVarsToTrack);
		
		//cfTaintMap = deepMapCopy(dfAnalysis.getDfTaintMap());
		if(dfAnalysis != null)
			cfTaintMap =  HashMapUtils.deepMapCopy(dfAnalysis.getDfTaintMap());
		for (Value value : contrlTaintMap.keySet()) {
			if(cfTaintMap.containsKey(value)){
				cfTaintMap.get(value).addAll(contrlTaintMap.get(value));
			}
			else
				cfTaintMap.put(value, new HashSet<Value>(contrlTaintMap.get(value)));
		}
		if(dfAnalysis != null)
			this.branchMap = dfAnalysis.getBranchMap();
		// TODO Auto-generated constructor stub
		doFlowAnalysis(graph);
		updateTaintMap();/*Finally Update the taint map*/
		updateStatementsControlledByBranch();
	}
	
	
	
	
	private void updateTaintMap() {
		// TODO Auto-generated method stub
		for (Object obj : this.unitToAfterFlow.keySet()) {
				Stmt s = (Stmt)obj;
				ControlFlowSet flow2 = (ControlFlowSet)this.unitToBeforeFlow.get(obj);
				ControlFlowSet flow = (ControlFlowSet)this.unitToAfterFlow.get(obj);
				if(flow.size() != flow2.size() || flow.getAllVars().size() != flow2.getAllVars().size()){
					int debug =1;
				}
				HashSet<Value> taintingVars = flow.getAllVars();
				for (Value taintedFrom : taintingVars) {
					for (ValueBox defBox : s.getDefBoxes()) {
						updateTaintMap(defBox.getValue(), taintedFrom);
					}
				}
				/**/
				if(taintingVars.size() == 0  && flow.size()!=0){
					Set<Stmt> condStmts =  flow.getCntrlTaintFlow().keySet();
					for (Stmt condStmt : condStmts) {
						for (ValueBox useBox : condStmt.getUseBoxes()) {
							if(cfVarsToTrack.contains(useBox.getValue())){
								for (ValueBox defBox : s.getDefBoxes()) {
									cfVarsToTrack.add(defBox.getValue());
									updateTaintMap(defBox.getValue(), useBox.getValue());
								}
							}
						}
					}
				}
				if(s instanceof IfStmt || s instanceof TableSwitchStmt){
					//update the branchmap
					updateBranchMap(s);
				}
		}
	}
	
	private void updateBranchMap(Stmt s) {
		Branch b = branchMap.get(s);
		if(b == null){
			b = new Branch(method, s);
			b.addControlFlowTaintVars(((ControlFlowSet)this.getFlowAfter(s)).getAllVars());
			branchMap.put(s, b);
		}
		else{
			b.addControlFlowTaintVars(((ControlFlowSet)this.getFlowAfter(s)).getAllVars());
		}
	}
	
	private void updateStatementsControlledByBranch(){
		for (Object obj : this.unitToAfterFlow.keySet()) {
			Stmt s = (Stmt)obj;
			int l = AppUtil.getStatementLineNumber(s);
			ControlFlowSet flow = (ControlFlowSet)this.unitToAfterFlow.get(s);
			ControlFlowSet flow2 = (ControlFlowSet)this.controllingVarsAtUnit.get(s);
			ControlFlowSet flow3 = (ControlFlowSet)this.unitToBeforeFlow.get(s);
			if(flow.getCntrlTaintFlow().size()>0 || flow2.getCntrlTaintFlow().size() >0){
				int deb =1;
			}
			Set<Stmt> cntrlStmts = flow.getCntrlTaintFlow().keySet();
			for (Stmt cntrlStmt : cntrlStmts) {
				
				if(cntrlStmt.equals(s))
					continue;
				Branch b = this.branchMap.get(cntrlStmt);
				if(b !=null){
					//b.getContrlDependentStmts().add(s);
					b.addCntrlDeptStmt(this.method, s);
					//b.getContrlDependentStmts().put(this.method, s);
				}
				else{
					b = new Branch(this.method, cntrlStmt);
					//b.getContrlDependentStmts().add(s);
					b.addCntrlDeptStmt(this.method, s);
					branchMap.put(cntrlStmt, b);
				}
			}
		}
	}




	/* Updates the taintMap 
	 * @parameter var is tanted from @parameter taintedFrom
	 * The method adds @var in the taintMap
	 * */
	private void updateTaintMap(Value var, Value taintedFrom) {
		// TODO Auto-generated method stub
		HashMap<Value, HashSet<Value>> copyOfTaintMap = new HashMap<Value, HashSet<Value>>();
		copyOfTaintMap = cfTaintMap;
		for (Value v : copyOfTaintMap.keySet()) {
			if(v.equals(taintedFrom)){
				cfTaintMap.get(v).add(var);
				continue;
			}
			
			HashSet<Value> vars = cfTaintMap.get(v);
			for (Value taintedFromV : vars) {
				if(taintedFromV.equals(taintedFrom)){
					cfTaintMap.get(v).add(var);
					break;
				}
			}
				
		}
	}




	private void doFlowAnalysis(UnitGraph graph) {
		controllingVarsAtUnit = new HashMap<Unit, ControlFlowSet>();
        {
        	boolean trackingOn = false;
            Iterator unitIt = graph.iterator();
            while(unitIt.hasNext()){
                Unit s = (Unit) unitIt.next();
                if(s.toString().contains("i > 5")){
    				int debug =1;
    				int a = debug;
    			}
                /*if(dfAnalysis.getStartAnalysisFrom() != null && trackingOn == false){
                	if(!dfAnalysis.getStartAnalysisFrom().equals(s)){
                		controllingVarsAtUnit.put(s, new ControlFlowSet());
                		continue;
                	}
                	else
                		trackingOn = true;
                }*/
            	ExtractControllingVariableSwitch controlSwitch = new ExtractControllingVariableSwitch(s);
            	s.apply(controlSwitch);
            	ControlFlowSet flow = controlSwitch.getControllingVars();
            	controllingVarsAtUnit.put(s, flow);
            }
        }
		doAnalysis();
	}
	

	
	@Override
	protected void flowThrough(Object in, Object d, Object out) {
		// TODO Auto-generated method stub
		int oldVarsToTrack;
		Stmt s = (Stmt)d;
		do{
			ControlFlowSet
	        inFlow = (ControlFlowSet) in,
	    	outFlow = (ControlFlowSet) out;
			ControlFlowSet flowAtUnit = (ControlFlowSet) controllingVarsAtUnit.get(d);
	    	outFlow.clear();
	    	if(flowAtUnit.getCntrlTaintFlow().size()>0 && inFlow.getCntrlTaintFlow().size()>0){
	    		int deb = 1;
	    		int a = deb;
	    	}
	    	inFlow.addFlow(flowAtUnit, outFlow);
	    	oldVarsToTrack = cfVarsToTrack.size(); 
	    	updateVariablesToTrackForCF((Unit)d, (ControlFlowSet) outFlow);
	    	
		}while(oldVarsToTrack != cfVarsToTrack.size());
	}

	
	/*Updating flow at branches which helps in propagation control flow tainting*/
	private void updateFlowAtUnits(){
		for (Unit unit : controllingVarsAtUnit.keySet()) {
			ExtractControllingVariableSwitch visitor = new ExtractControllingVariableSwitch(unit);
			unit.apply(visitor);
			for (Object pair : visitor.getControllingVars().toList()) {
				controllingVarsAtUnit.get(unit).add(pair);
			}
		}
	}
	
	private void updateVariablesToTrackForCF(Unit d, ControlFlowSet outFlow) {
		HashSet<Value> taintedFrom = outFlow.getAllVars();
		if(taintedFrom.size() == 0) return;
		List<ValueBox> defs = d.getDefBoxes();
		for (ValueBox valueBox : defs) {
			cfVarsToTrack.add(valueBox.getValue());
			for (Value taintedFromVal : taintedFrom) {
				updateCFTaintMap(valueBox.getValue(), taintedFromVal);
			}
		}	
		updateFlowAtUnits();
	}
	
	private void updateCFTaintMap(Value var, Value taintedFrom) {
		// TODO Auto-generated method stub
		HashMap<Value, HashSet<Value>> copyOfTaintMap = new HashMap<Value, HashSet<Value>>();
		copyOfTaintMap = cfTaintMap;
		for (Value v : copyOfTaintMap.keySet()) {
			if(v.equals(taintedFrom)){
				cfTaintMap.get(v).add(var);
				continue;
			}
			
			HashSet<Value> vars = cfTaintMap.get(v);
			for (Value taintedFromV : vars) {
				if(taintedFromV.equals(taintedFrom)){
					cfTaintMap.get(v).add(var);
					break;
				}
			}
				
		}
	}



	@Override
	protected void copy(Object source, Object dest) {
		// TODO Auto-generated method stub
		ControlFlowSet sourceFlow = (ControlFlowSet)source;
		ControlFlowSet destinationFlow = (ControlFlowSet)dest;
		destinationFlow.clear();
		
		ControlFlowSet clonedSet = (ControlFlowSet) sourceFlow.clone();
		for (Stmt s : clonedSet.getStmtStack()) {
			destinationFlow.add(new Pair<Stmt, HashSet<Value>>(s, clonedSet.getCntrlTaintFlow().get(s)));
		}
	}

	@Override
	protected Object entryInitialFlow() {
		/*need to figure out how this method works*/
		return entryFlow;
	}

	@Override
	protected void merge(Object in1, Object in2, Object out) {
		// TODO Auto-generated method stub
		ControlFlowSet
        inFlow1 = (ControlFlowSet) in1,
    	inFlow2 = (ControlFlowSet) in2;
		ControlFlowSet outFlow = (ControlFlowSet) out;
    	outFlow.clear();
    	inFlow1.intersection(inFlow2, outFlow);
	}

	@Override
	protected Object newInitialFlow() {
		// TODO Auto-generated method stub
		return new ControlFlowSet();
	}
	
	
	
	/*
	 * Returns a variable @var whose tracking is on 
	 * and the variable @def is tainted by @var
	 */
	public HashSet<Value> cfTaintedFrom(Value def) {
		// TODO Auto-generated method stub
		HashSet<Value> values = new HashSet<Value>();
		if(cfTaintMap.keySet().contains(def)){
			values.add(def);
			return values;
		}
		
		for (Value var : cfTaintMap.keySet()) {
			HashSet<Value> uses = cfTaintMap.get(var);
			if(uses.contains(def)){
				values.add(var);
			}
		}
		return values;
	}
	
	
	
	
	
	
	public void setCfVarsToTrack(HashSet<Value> cfVarsToTrack) {
		this.cfVarsToTrack = cfVarsToTrack;
	}




	public HashSet<Value> getCfVarsToTrack() {
		return cfVarsToTrack;
	}






	public void setBranchMap(HashMap<Unit, Branch> branchMap) {
		this.branchMap = branchMap;
	}




	public HashMap<Unit, Branch> getBranchMap() {
		return branchMap;
	}






	class ExtractControllingVariableSwitch implements StmtSwitch{

		private ControlFlowSet controllingVars = new ControlFlowSet();
	
		
		public ExtractControllingVariableSwitch(Unit s)
		{
			
		}
		
		public void caseAssignStmt(AssignStmt stmt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void caseBreakpointStmt(BreakpointStmt stmt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
			// TODO Auto-generated method stub
			int enter = 1;
		}

		@Override
		public void caseGotoStmt(GotoStmt stmt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void caseIdentityStmt(IdentityStmt stmt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void caseIfStmt(IfStmt stmt) {
			// TODO Auto-generated method stub
			List<ValueBox> useBoxes = stmt.getUseBoxes();
			for( ValueBox use : useBoxes){
				/*Set of variables that taint condition in this if statement*/
				HashSet<Value> vars = new HashSet<Value>();
				if(getCfVarsToTrack().contains(use.getValue())){
					HashSet<Value> var = cfTaintedFrom(use.getValue());
					vars.addAll(var);
				}
				getControllingVars().add(new Pair<Stmt, HashSet<Value>>(stmt, vars));
								
			}
		}

		@Override
		public void caseInvokeStmt(InvokeStmt stmt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
			// TODO Auto-generated method stub
			List<ValueBox> useBoxes = stmt.getUseBoxes();
			for( ValueBox use : useBoxes){
				/*Set of variables that taint condition in this if statement*/
				HashSet<Value> vars = new HashSet<Value>();
				if(getCfVarsToTrack().contains(use.getValue())){
					HashSet<Value> var = cfTaintedFrom(use.getValue());
					vars.addAll(var);
				}				
				getControllingVars().add(new Pair<Stmt, HashSet<Value>>(stmt, vars));
			}
		}

		@Override
		public void caseNopStmt(NopStmt stmt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void caseRetStmt(RetStmt stmt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void caseReturnStmt(ReturnStmt stmt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void caseTableSwitchStmt(TableSwitchStmt stmt) {
			
			List<ValueBox> useBoxes = stmt.getUseBoxes();
			for( ValueBox use : useBoxes){
				/*Set of variables that taint condition in this if statement*/
				HashSet<Value> vars = new HashSet<Value>();
				if(getCfVarsToTrack().contains(use.getValue())){
					HashSet<Value> var = cfTaintedFrom(use.getValue());
					vars.addAll(var);
				}				
				getControllingVars().add(new Pair<Stmt, HashSet<Value>>(stmt, vars));
			}
			
		}

		@Override
		public void caseThrowStmt(ThrowStmt stmt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void defaultCase(Object obj) {
			// TODO Auto-generated method stub
			
		}

		public ControlFlowSet getControllingVars() {
			return controllingVars;
		}
		
	}

}
