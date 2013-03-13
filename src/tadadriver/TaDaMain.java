package tadadriver;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JLookupSwitchStmt;
import soot.jimple.internal.JTableSwitchStmt;
import soot.jimple.toolkits.callgraph.CallGraphPack;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.Chain;
import tadagui.TaDaFinalView;
import utils.AppUtil;
import FlowAnalysis.Branch;
import FlowAnalysis.DataFlowAnalysis;
import FlowAnalysis.InterProceduralTracking;
import FlowSet.ConstraintFlowSet;
import FlowSet.ControlFlowSet;

public class TaDaMain implements Runnable{
public static String fileName = "/home/f85/dggraham/scratch-local/Research/DataPrivacyProject/Priest/workspace/branches.txt";
	


	public String[] mainArguments; 
	public static boolean isMethodInvocationmapBuilt = false;
	public static MethodInvocationMap methodInvocationMap;
	public static HashMap<Unit, Branch> branchMap = new HashMap<Unit, Branch>(1);
	public static CoverageDatabaseAttributeMapping coverageInfo = new CoverageDatabaseAttributeMapping();
	
	static boolean isCFTrackingOn = true; //get from args
	static boolean isInterprocedural = true; 
	
	
	public static void main(String[] args){
		
		
		PackManager.v().getPack("jtp").add(
				  
			  new Transform("jtp.myTransform", new BodyTransformer() {
				  
		      /*This method is invoked by soot for all methods in the main class provided as argument*/
				  HashSet<SootMethod> methodsToExplore;

		      protected void internalTransform(Body body, String phase, Map options) {
		    	  if(!isMethodInvocationmapBuilt){
		    		  if(TaDaFinalView.progressBar != null)
		    			  TaDaFinalView.progressBar.setString("Building Method Invocation Map");
			    	  Chain appClasses = Scene.v().getApplicationClasses();
			    	  methodInvocationMap = new MethodInvocationMap();
			    	  methodInvocationMap.buildMethodInvocationMap(appClasses);
			    	  methodsToExplore = methodInvocationMap.getMethodsToExplore(100);

			    	  isMethodInvocationmapBuilt = true;
		    	  }
		    	  
		    	  
		    	  for (SootMethod m : methodInvocationMap.methodsExecutingSQLQueries) {
					System.out.println(m.getSignature());
		    	  }
		    	  if(!methodsToExplore.contains(body.getMethod())) return;
		    	  DataFlowAnalysis dfAnalysis;
		          dfAnalysis = new DataFlowAnalysis(new ExceptionalUnitGraph(body), isCFTrackingOn, isInterprocedural);
		    	  
		          for (Unit u : dfAnalysis.getBranchMap().keySet()) {
		  			Branch b = dfAnalysis.getBranchMap().get(u);
		  			if(branchMap.get(u) == null)
		  				branchMap.put(u, b);
		  			else{
		  				branchMap.get(u).addControlFlowTaintVars(b.getControlFlowTaintVars());
		  				branchMap.get(u).addDataFlowTaintVars(b.getDataFlowTaintVars());
		  			}
		  		}
		          
		      }

			
		    }));
		Scene.v().addBasicClass("java.util.Iterator", SootClass.SIGNATURES);
		Scene.v().addBasicClass("@primitive.Unknown", SootClass.DANGLING);

		  soot.Main.main(args);
		  dumpBranches(branchMap);
		  dumpAttrCoverageInfo();
		  @SuppressWarnings("unused")
		  boolean done = true;
		  if(TaDaFinalView.progressBar != null)
			  TaDaFinalView.progressBar.setString("Finished Analysis!!!");
		  //soot.Main.main();
		}
	
	
	
	
	
	private static void dumpAttrCoverageInfo() {
		// TODO Auto-generated method stub
		java.io.File file = new java.io.File("/home/f85/dggraham/scratch-local/Research/DataPrivacyProject/Priest/workspace/coverage.txt");
		try {
			PrintStream stream = new PrintStream(file);
			for (String dBColumn : coverageInfo.getMap().keySet()) {
				stream.println(dBColumn + " -> " + coverageInfo.getMap().get(dBColumn));
			}
			stream.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}





	private static void dumpBranches(HashMap<Unit, Branch> branchMap) {
		java.io.File file = new java.io.File(fileName);
		
		
		try {
			PrintStream stream = new PrintStream(file);
			for (Unit stmt : branchMap.keySet()) {
				Branch b = branchMap.get(stmt);
				if(b.getDataFlowTaintVars().size() ==0 && b.getControlFlowTaintVars().size() ==0) continue;
				
				HashSet<String> attributes = b.getCFandDFDBTaintAttributes();
				int loc = b.getLineNumbersofDependentStmts().size();
				coverageInfo.addCombinedCoverage(attributes, loc);
				for (String dbColumn : attributes) {
					coverageInfo.addCoverageFor(dbColumn, loc);
				}
				
				
				int stmtLineNo = AppUtil.getStatementLineNumber(b.getStmt());
				if(stmtLineNo == 14){
					int i=0;
					for (SootMethod m : b.getContrlDependentStmts().keySet()) {
						for (Stmt s : b.getContrlDependentStmts().get(m)) {
							System.out.println(AppUtil.getStatementLineNumber(s) + " : " + s.toString() + " " + i++);
						}
					}
				}
				HashSet<String> lineNos = b.getLineNumbersofDependentStmts();
				
				if(stmt instanceof IfStmt)
					stream.println(b.getMethod().getSignature() + ((IfStmt)stmt).toString() + "\t" + b.getDataFlowTaintDBAttributes().toString() + "\t" + b.getCntrlFlowTaintDBAttributes() + "\t" + stmtLineNo + "->" +lineNos.size() );
				//if(b.getStmt().toString().contains("if(b.)))
				else if(stmt instanceof JTableSwitchStmt)
					stream.println(b.getMethod().getSignature() + ((JTableSwitchStmt)stmt).toString() + "\t" + b.getDataFlowTaintDBAttributes().toString()+ "\t" +b.getCntrlFlowTaintDBAttributes() + "\t" + stmtLineNo + "->" +lineNos.size() );
				else if(stmt instanceof JLookupSwitchStmt)
					stream.println(b.getMethod().getSignature() + ((JLookupSwitchStmt)stmt).toString() + "\t" + b.getDataFlowTaintDBAttributes().toString()+ "\t" +b.getCntrlFlowTaintDBAttributes() + "\t" + stmtLineNo + "->" +lineNos.size() );
				
			}
			HashMap<HashSet<String>, Integer> map = coverageInfo.getCombinedAttrCoverageMap();
			stream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}





	@Override
	public void run() {
		// TODO Auto-generated method stub
		main(mainArguments);
	}
	
	
}
