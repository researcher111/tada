package Visitors;

import java.util.HashSet;
import java.util.List;

import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StmtSwitch;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;


public class ExtractMethodInvocationStmtSwitch implements StmtSwitch{

	private HashSet<InvokeExpr> methodInvocations;
	
	public ExtractMethodInvocationStmtSwitch(){
		methodInvocations = new HashSet<InvokeExpr>();
	}
	
	@Override
	public void caseAssignStmt(AssignStmt stmt) {
		// TODO Auto-generated method stub
		List<ValueBox> useBoxes =  stmt.getUseBoxes();
		ExtractMethodInvocationExprSwitch exprSwitch = new ExtractMethodInvocationExprSwitch();
		for (ValueBox useBox : useBoxes) {
			Value v  = useBox.getValue();
			v.apply(exprSwitch);
		}
		getMethodInvocations().addAll(exprSwitch.getMethodInvocations());
	}

	@Override
	public void caseBreakpointStmt(BreakpointStmt stmt) {
		// TODO Auto-generated method stub
		try {
			throw new Exception("Need to Figure out how to implement for BreakpointStmt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
		// TODO Auto-generated method stub
		ExtractMethodInvocationExprSwitch exprSwitch = new ExtractMethodInvocationExprSwitch();
		Value v  = stmt.getOp();
		v.apply(exprSwitch);
		getMethodInvocations().addAll(exprSwitch.getMethodInvocations());
	}

	@Override
	public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
		// TODO Auto-generated method stub
		ExtractMethodInvocationExprSwitch exprSwitch = new ExtractMethodInvocationExprSwitch();
		Value v  = stmt.getOp();
		v.apply(exprSwitch);
		getMethodInvocations().addAll(exprSwitch.getMethodInvocations());
	}

	@Override
	public void caseGotoStmt(GotoStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseIdentityStmt(IdentityStmt stmt) {
		// TODO Auto-generated method stub
		ExtractMethodInvocationExprSwitch exprSwitch = new ExtractMethodInvocationExprSwitch();
		Value val = stmt.getRightOp();
		val.apply(exprSwitch);
		getMethodInvocations().addAll(exprSwitch.getMethodInvocations());
	}

	@Override
	public void caseIfStmt(IfStmt stmt) {
		// TODO Auto-generated method stub
		ExtractMethodInvocationExprSwitch exprSwitch = new ExtractMethodInvocationExprSwitch();
		Value val = stmt.getCondition();
		val.apply(exprSwitch);
		getMethodInvocations().addAll(exprSwitch.getMethodInvocations());
	}

	@Override
	public void caseInvokeStmt(InvokeStmt stmt) {
		// TODO Auto-generated method stub
		ExtractMethodInvocationExprSwitch exprSwitch = new ExtractMethodInvocationExprSwitch();
		Value val = stmt.getInvokeExpr();
		val.apply(exprSwitch);
		getMethodInvocations().addAll(exprSwitch.getMethodInvocations());
	}

	@Override
	public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
		// TODO Auto-generated method stub
		ExtractMethodInvocationExprSwitch exprSwitch = new ExtractMethodInvocationExprSwitch();
		Value val = stmt.getKey();
		val.apply(exprSwitch);
		getMethodInvocations().addAll(exprSwitch.getMethodInvocations());	
	}

	@Override
	public void caseNopStmt(NopStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseRetStmt(RetStmt stmt) {
		// TODO Auto-generated method stub
		ExtractMethodInvocationExprSwitch exprSwitch = new ExtractMethodInvocationExprSwitch();
		Value val = stmt.getStmtAddress();
		val.apply(exprSwitch);
		getMethodInvocations().addAll(exprSwitch.getMethodInvocations());
	}

	@Override
	public void caseReturnStmt(ReturnStmt stmt) {
		// TODO Auto-generated method stub
		ExtractMethodInvocationExprSwitch exprSwitch = new ExtractMethodInvocationExprSwitch();
		Value val = stmt.getOp();
		val.apply(exprSwitch);
		getMethodInvocations().addAll(exprSwitch.getMethodInvocations());
	}

	@Override
	public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseTableSwitchStmt(TableSwitchStmt stmt) {
		// TODO Auto-generated method stub
		ExtractMethodInvocationExprSwitch exprSwitch = new ExtractMethodInvocationExprSwitch();
		Value val = stmt.getKey();
		val.apply(exprSwitch);
		getMethodInvocations().addAll(exprSwitch.getMethodInvocations());
	}

	@Override
	public void caseThrowStmt(ThrowStmt stmt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void defaultCase(Object obj) {
		// TODO Auto-generated method stub
		try {
			throw new Exception("Not Implemented for " + obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
	}

	public void setMethodInvocations(HashSet<InvokeExpr> methodInvocations) {
		this.methodInvocations = methodInvocations;
	}

	public HashSet<InvokeExpr> getMethodInvocations() {
		return methodInvocations;
	}
	

}
