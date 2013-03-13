package SootDriver;


import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.IfStmt;
import soot.jimple.internal.JTableSwitchStmt;
import soot.jimple.toolkits.pointer.LocalMustNotAliasAnalysis;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import FlowAnalysis.Branch;
import FlowAnalysis.CFDriver;
import FlowAnalysis.ControlFlowAnalysis;
import FlowAnalysis.InterProceduralTracking;
import FlowSet.ConstraintFlowSet;
import FlowSet.ControlFlowSet;

public class MyMain {

	public static String fileName = "c:/branches.txt";
	
	public static void main(String[] args) {
		
		  PackManager.v().getPack("jtp").add(
		    new Transform("jtp.myTransform", new BodyTransformer() {
		    	/*This method is invoked by soot for all methods in the main class provided as argument*/
		      protected void internalTransform(Body body, String phase, Map options) {
		          //new LocalMustNotAliasAnalysis(new ExceptionalUnitGraph(body));
		    	  //new GuaranteedDefs(new ExceptionalUnitGraph(body));
		    	  String methodName = body.getMethod().getName();
		    	  //if(!methodName.equals("main")) return;
		    	  if(!methodName.equals("testFieldVar2")) return;
		    	  //InterProceduralTracking entryMethodTrack = new InterProceduralTracking(new ExceptionalUnitGraph(body), null, null, new ArrayList<ConstraintFlowSet>(), new ControlFlowSet(), new HashSet<Value>(), new HashMap<Value, HashSet<Value>>());
		    	  //dumpBranches(entryMethodTrack.getBranchMap());
		    	  CFDriver cfd = new CFDriver(new ExceptionalUnitGraph(body));
		    	  ControlFlowAnalysis analysis =  new ControlFlowAnalysis(new ExceptionalUnitGraph(body), null, 
							new ControlFlowSet(), new HashSet<Value>(), new HashMap<Value, HashSet<Value>>());
		      }
		    }));
		  //Scene.v().addBasicClass("java.util.Iterator", soot.SootClass.SIGNATURES);
		  soot.Main.main(args);
		  //soot.Main.main();
		}
	
	
	private static void dumpBranches(HashMap<Unit, Branch> branchMap) {
		java.io.File file = new java.io.File(fileName);
		try {
			PrintStream stream = new PrintStream(file);
			for (Unit stmt : branchMap.keySet()) {
				Branch b = branchMap.get(stmt);
				if(b.getDataFlowTaintVars().size() ==0 && b.getControlFlowTaintVars().size() ==0) continue;
				if(stmt instanceof IfStmt)
					stream.println(b.getMethod().getSignature() + ((IfStmt)stmt).toString() + "\t" + b.getDataFlowTaintVars().toString() + "\t" + b.getControlFlowTaintVars());
				else if(stmt instanceof JTableSwitchStmt)
					stream.println(b.getMethod().getSignature() + ((JTableSwitchStmt)stmt).toString() + "\t" + b.getDataFlowTaintVars().toString()+ "\t" +b.getControlFlowTaintVars());
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
