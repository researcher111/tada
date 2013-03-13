package SootDriver;


import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.IfStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.internal.JTableSwitchStmt;
import soot.jimple.toolkits.pointer.LocalMustNotAliasAnalysis;
import soot.toolkits.graph.DominatorAnalysis;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.Pair;
import utils.AppUtil;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import FlowAnalysis.Branch;
import FlowAnalysis.CFDriver;
import FlowAnalysis.ControlFlowAnalysis;
import FlowAnalysis.InterProceduralTracking;
import FlowSet.ConstraintFlowSet;
import FlowSet.ControlFlowSet;

public class DominatorSootDriver {

	public static String fileName = "c:/branches.txt";
	public static String driverMethod = "entryMeth";
	
	public static void main(String[] args) {
		  PackManager.v().getPack("jtp").add(
		    new Transform("jtp.myTransform", new BodyTransformer() {
		      protected void internalTransform(Body body, String phase, Map options) {
		    	  String methodName = body.getMethod().getName();
		    	  if(DominatorAnalysisWraper.alreadyExploredMethods.contains(body.getMethod())) return;
		    	  DominatorAnalysisWraper da = new DominatorAnalysisWraper(new ExceptionalUnitGraph(body));
		    	  //dumpBranches(da.branchMap, da.pairGraphMapper);
		      }
		    }));
		  soot.Main.main(args);
		  Scene.v().addBasicClass("Test1K", SootClass.HIERARCHY);
		  //soot.Main.main();
		}
	
	
	private static void dumpBranches(HashMap<Pair<Unit, Unit>, Integer> branchMap, HashMap<Pair<Unit, Unit>, UnitGraph> pairGraphMapper) {
		java.io.File file = new java.io.File(fileName);
		try {
			PrintStream stream = new PrintStream(file);
			
			for (Pair<Unit, Unit> branch : branchMap.keySet()) {
				UnitGraph graph = pairGraphMapper.get(branch);
				Unit from = branch.getO1();
				Unit to = branch.getO2();
				int line = AppUtil.getStatementLineNumber((Stmt) from);
				
				Unit succ = null;
				if(graph.getSuccsOf(to).size() ==1)
					succ = graph.getSuccsOf(to).get(0);
				else if(graph.getSuccsOf(to).size() == 0){
					@SuppressWarnings("unused")
					int debug =1;
				}
				else{
					@SuppressWarnings("unused")
					int debug =1;
				}
				if(from instanceof IfStmt && branch.toString().contains("goto")){
					if(line == 164){
						@SuppressWarnings("unused")
						int debug =1;
					}
					//Unit nop = graph.getSuccsOf(succ).get(0);
					Unit nop = succ;
					while(nop.toString().contains("nop") && !nop.toString().contains("if") && !nop.toString().contains("switch"))
						nop = graph.getSuccsOf(nop).get(0);
					to = nop;
				}
			
				/*if(to.toString().equals("nop")){
					if(from instanceof IfStmt)
						stream.println(line + ": branch: true -> " + branchMap.get(branch));
					if(from instanceof TableSwitchStmt || from instanceof LookupSwitchStmt)
						stream.println(line + ": branch: " + AppUtil.getStatementLineNumber((Stmt) to) + " -> " + branchMap.get(branch));
				}
				else{
					if(from instanceof IfStmt)
						stream.println(line + ": branch: false -> " + branchMap.get(branch));
					if(from instanceof TableSwitchStmt || from instanceof LookupSwitchStmt)
						stream.println(line + ": branch: " + AppUtil.getStatementLineNumber((Stmt) to) + " -> " + branchMap.get(branch));
				}*/
				stream.println(line + ": branch: " + AppUtil.getStatementLineNumber((Stmt) to) + " -> " + branchMap.get(branch));
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
