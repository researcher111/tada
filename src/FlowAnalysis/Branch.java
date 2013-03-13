package FlowAnalysis;

import java.util.HashMap;
import java.util.HashSet;

import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JNopStmt;
import soot.tagkit.SourceLnNamePosTag;
import tadadriver.MethodInvocationMap;
import utils.AppUtil;

/* A data structure to store the information about a particular branch that is dependent
 * on (tainted by) a specific variable specified by dummyMethod
 * 
 * */
public class Branch {
	/*Method containing this branch*/
	private SootMethod method;
	/*Class containg this branch*/
	private SootClass clazz;
	/*If Statement representing the branch*/
	private Stmt stmt;
	/*Variables this branch is tainted by using data flow propagation*/
	private HashSet<Value> dataFlowTaintVars;
	/*Variables this branch is tainted by using control flow propagation*/
	private HashSet<Value> controlFlowTaintVars;
	/*The number of statements that are control dependent on this branch*/
	private HashMap<SootMethod, HashSet<Stmt>> contrlDependentStmts;
	
	
	
	
	public Branch(SootMethod containingMethod, Stmt ifStmt){
		dataFlowTaintVars = new HashSet<Value>(1);
		this.method = containingMethod;
		this.stmt = ifStmt;
		this.clazz = containingMethod.getDeclaringClass();
		controlFlowTaintVars = new HashSet<Value>(1);
		dataFlowTaintVars = new HashSet<Value>(1);
		contrlDependentStmts = new HashMap<SootMethod, HashSet<Stmt>>();
	}
	
	
	public void addCntrlDeptStmt(SootMethod method, Stmt s){
		if(contrlDependentStmts.containsKey(method)){
			contrlDependentStmts.get(method).add(s);
		}
		else{
			HashSet<Stmt> set = new HashSet<Stmt>();
			set.add(s);
			contrlDependentStmts.put(method, set);
		}
	}
	
	public HashSet<Value> getDataFlowTaintVars(){
		return dataFlowTaintVars;
	}
	
	public void addDataFlowTaintVar(Value val){
		dataFlowTaintVars.add(val);
	}
	
	public void addDataFlowTaintVars(HashSet<Value> vals){
		dataFlowTaintVars.addAll(vals);
	}
	
	
	public HashSet<Value> getControlFlowTaintVars(){
		return controlFlowTaintVars;
	}
	
	public void addControlFlowTaintVar(Value val){
		controlFlowTaintVars.add(val);
	}
	
	public void addControlFlowTaintVars(HashSet<Value> vals){
		controlFlowTaintVars.addAll(vals);
	}
	
	
	public void setMethod(SootMethod method) {
		this.method = method;
	}
	public SootMethod getMethod() {
		return method;
	}
	public void setClazz(SootClass clazz) {
		this.clazz = clazz;
	}
	public SootClass getClazz() {
		return clazz;
	}
	public void setStmt(soot.jimple.IfStmt stmt) {
		this.stmt = stmt;
	}
	public soot.jimple.Stmt getStmt() {
		return stmt;
	}



	



	public HashMap<SootMethod, HashSet<Stmt>> getContrlDependentStmts() {
		return contrlDependentStmts;
	}
	
	public HashSet<String> getLineNumbersofDependentStmts(){
		
		HashSet<String> lineNumbers = new HashSet<String>();
		int code = stmt.hashCode();
		
		for (SootMethod m : contrlDependentStmts.keySet()) {
			for (Stmt stmt : contrlDependentStmts.get(m)) {
				int line = AppUtil.getStatementLineNumber(stmt);
				if (line ==0){
					int debug =1;
					int a = debug;
				}
				//Not adding noop statements they are making a mess.
				if(stmt instanceof JNopStmt)
					continue;
				if(AppUtil.isDifferentStartEndPos(stmt)){
					if(stmt instanceof JGotoStmt || stmt instanceof JNopStmt){
						int debug =1;
						int a = debug;
					}
					else{
						lineNumbers.add(line + "(" + m.getDeclaringClass().getName() + ")");
					}
				}
				else
					lineNumbers.add(line + "(" + m.getDeclaringClass().getName() + ")");
			}
		}
		return lineNumbers;
	}
	
	public HashSet<String> getDataFlowTaintDBAttributes(){
		HashSet<String> dbVars = new HashSet<String>();
		for (Value var : dataFlowTaintVars) {
			dbVars.add(MethodInvocationMap.allVarAttributeMap.get(var));
		}
		return dbVars;
	}
	
	public HashSet<String> getCntrlFlowTaintDBAttributes(){
		HashSet<String> dbVars = new HashSet<String>();
		for (Value var : controlFlowTaintVars) {
			dbVars.add(MethodInvocationMap.allVarAttributeMap.get(var));
		}
		return dbVars;
	}
	
	public HashSet<String> getCFandDFDBTaintAttributes(){
		HashSet<String> dbVars = new HashSet<String>();
		for (Value var : dataFlowTaintVars) {
			dbVars.add(MethodInvocationMap.allVarAttributeMap.get(var));
		}
		for (Value var : controlFlowTaintVars) {
			dbVars.add(MethodInvocationMap.allVarAttributeMap.get(var));
		}
		return dbVars;
	}


	public void addCntrlDeptStmts(
			HashMap<SootMethod, HashSet<Stmt>> statementsToAddMap) {
		// TODO Auto-generated method stub
		for (SootMethod method : statementsToAddMap.keySet()) {
			for (Stmt s: statementsToAddMap.get(method)) {
				this.addCntrlDeptStmt(method, s);
			}
		}
	}
	
}
