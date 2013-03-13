package Visitors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.DivExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.EqExpr;
import soot.jimple.Expr;
import soot.jimple.ExprSwitch;
import soot.jimple.FloatConstant;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.JimpleValueSwitch;
import soot.jimple.LeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.LongConstant;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.OrExpr;
import soot.jimple.ParameterRef;
import soot.jimple.RemExpr;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.SubExpr;
import soot.jimple.ThisRef;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;
import soot.jimple.internal.JimpleLocal;
import soot.util.Chain;

public class ExtractMethodInvocationExprSwitch implements JimpleValueSwitch{

	private HashSet<InvokeExpr> methodInvocations;
	
	
	public ExtractMethodInvocationExprSwitch(){
		setMethodInvocations(new HashSet<InvokeExpr>());
	}
	
	@Override
	public void caseAddExpr(AddExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseAndExpr(AndExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseCastExpr(CastExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp();
		v1.apply(this);
	}

	@Override
	public void caseCmpExpr(CmpExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseCmpgExpr(CmpgExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseCmplExpr(CmplExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseDivExpr(DivExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseEqExpr(EqExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseGeExpr(GeExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseGtExpr(GtExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseInstanceOfExpr(InstanceOfExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp();
		v1.apply(this);
	}

	@Override
	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
		// TODO Auto-generated method stub
		List<Value> args =  v.getArgs();
		for (Value value : args) {
			value.apply(this);
		}
		methodInvocations.add(v);
	}

	@Override
	public void caseLeExpr(LeExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseLengthExpr(LengthExpr v) {
		// TODO Auto-generated method stub
		Value val = v.getOp();
		val.apply(this);
	}

	@Override
	public void caseLtExpr(LtExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseMulExpr(MulExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseNeExpr(NeExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseNegExpr(NegExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp();
		v1.apply(this);
	}

	@Override
	public void caseNewArrayExpr(NewArrayExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getSize();
		v1.apply(this);
	}

	@Override
	public void caseNewExpr(NewExpr v) {
	}

	@Override
	public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
		// TODO Auto-generated method stub
		List<Value> sizeExprs =  v.getSizes();
		for (Value value : sizeExprs) {
			value.apply(this);
		}
	}

	@Override
	public void caseOrExpr(OrExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseRemExpr(RemExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseShlExpr(ShlExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseShrExpr(ShrExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
		// TODO Auto-generated method stub
		
		List<Value> args =  v.getArgs();
		for (Value value : args) 
			value.apply(this);
		methodInvocations.add(v);
	}

	@Override
	public void caseStaticInvokeExpr(StaticInvokeExpr v) {
		// TODO Auto-generated method stub
		List<Value> args =  v.getArgs();
		for (Value value : args) {
			value.apply(this);
		}
		methodInvocations.add(v);
	}

	@Override
	public void caseSubExpr(SubExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseUshrExpr(UshrExpr v) {
		// TODO Auto-generated method stub
		Value  v1 = v.getOp1();
		Value  v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
		// TODO Auto-generated method stub
		List<Value> args =  v.getArgs();
		for (Value value : args) {
			value.apply(this);
		}
		methodInvocations.add(v);
	}

	@Override
	public void caseXorExpr(XorExpr v) {
		// TODO Auto-generated method stub
		Value v1 = v.getOp1();
		Value v2 = v.getOp2();
		v1.apply(this);
		v2.apply(this);
	}

	@Override
	public void defaultCase(Object obj) {
		// TODO Auto-generated method stub
		try {
			throw new Exception("Not implemented for " + obj.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setMethodInvocations(HashSet<InvokeExpr> methodInvocations) {
		this.methodInvocations = methodInvocations;
	}

	public HashSet<InvokeExpr> getMethodInvocations() {
		return methodInvocations;
	}

	
	
	
	
	
	
	
	/*Methods for non-expression values : We dont need to do anything 
	 * with them*/
	
	@Override
	public void caseLocal(Local l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseClassConstant(ClassConstant v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseDoubleConstant(DoubleConstant v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseFloatConstant(FloatConstant v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseIntConstant(IntConstant v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseLongConstant(LongConstant v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseNullConstant(NullConstant v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseStringConstant(StringConstant v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseArrayRef(ArrayRef v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseCaughtExceptionRef(CaughtExceptionRef v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseInstanceFieldRef(InstanceFieldRef v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseParameterRef(ParameterRef v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseStaticFieldRef(StaticFieldRef v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseThisRef(ThisRef v) {
		// TODO Auto-generated method stub
		
	}

}
