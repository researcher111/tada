package SootDriver;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import Visitors.ExtractMethodInvocationStmtSwitch;

import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.toolkits.graph.DominatorAnalysis;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.Pair;
import utils.AppUtil;

public class DominatorAnalysisWraper {

	UnitGraph graph;
	DominatorAnalysis domAnalysis;
	static HashMap<SootMethod, Integer> potentialLOCMap = new HashMap<SootMethod, Integer>();
	//static HashMap<SootMethod, DominatorAnalysisWraper> alreadyExploredMethods = new HashMap<SootMethod, DominatorAnalysisWraper>();
	static HashSet<SootMethod> alreadyExploredMethods = new HashSet<SootMethod>(1); 
	public static HashMap<Pair<Unit, Unit>, Integer> branchMap = new HashMap<Pair<Unit,Unit>, Integer>();
	public static HashMap<Pair<Unit, Unit>, UnitGraph> pairGraphMapper = new HashMap<Pair<Unit,Unit>, UnitGraph>();
	static ArrayList<SootMethod> invocationStack = new ArrayList<SootMethod>();
	static ArrayList<SootMethod> LOCStack = new ArrayList<SootMethod>();
	public static String filePath = "C:\\static-app2k.txt";
	
	
	public static BufferedWriter fileWriter; 
	
	public static BufferedWriter openFile() {
		FileWriter fw;
		BufferedWriter writer= null;
		try {
			fw = new FileWriter(filePath, true);
			writer = new BufferedWriter(fw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writer;
	}
	
	public static void writeBranchToFile(String branch) throws IOException{
		 fileWriter = openFile();
		fileWriter.write(branch + "\n");
		fileWriter.close();
	}
	
	/*public static void main(String[] args) {
		for(int i=0; i< 1000; i++){
			try {
				writeBranchToFile("dsjfjfjf");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}*/
	
	public void dumpBranch(UnitGraph graph, Pair<Unit, Unit> branch, int n){
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
		try {
			String s = line + ": branch: " + AppUtil.getStatementLineNumber((Stmt) to) + " -> " + n;
			writeBranchToFile(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public DominatorAnalysisWraper(UnitGraph graph) {
		// TODO Auto-generated constructor stub
		
		System.out.println("Adding " + graph.getBody().getMethod()  + " " + invocationStack.size());
		invocationStack.add(graph.getBody().getMethod());
		alreadyExploredMethods.add(graph.getBody().getMethod());
		
		this.graph = graph;
		this.domAnalysis = new DominatorAnalysis(graph);
		for (Unit unit : graph) {

			HashSet<InvokeExpr> invocationsAtUnit = new HashSet<InvokeExpr>();
			
			if(unit instanceof IfStmt || unit instanceof TableSwitchStmt || unit instanceof LookupSwitchStmt){
				List<Unit> branches = graph.getSuccsOf(unit);
				invocationsAtUnit = extractMethodInvocations(unit);
				for (Unit branch : branches) {
					if(unit instanceof TableSwitchStmt || unit instanceof LookupSwitchStmt){
						@SuppressWarnings("unused")
						List<Unit> succ = graph.getSuccsOf(branch);
						branch = succ.get(0);
						
						int unitLine = AppUtil.getStatementLineNumber((Stmt) unit);
						int line = AppUtil.getStatementLineNumber((Stmt) succ.get(0));
						@SuppressWarnings("unused")
						int d =1;
					}
					/*else{
						List<Unit> succ = graph.getSuccsOf(branch);
						if(branch.toString().contains("goto")){
							Unit nop = graph.getSuccsOf(succ.get(0)).get(0);
							while(nop.toString().contains("nop") && !nop.toString().contains("if"))
								nop = graph.getSuccsOf(nop).get(0);
							branch = nop;
						}
						else{
							Unit nop = graph.getSuccsOf(succ.get(0)).get(0);
							while(nop.toString().contains("nop") && !nop.toString().contains("if"))
								nop = graph.getSuccsOf(nop).get(0);
							branch = nop;
						}
					}*/
					int n = getNumberofStatementsDominatedBy(branch, unit);
					Pair<Unit, Unit> p = new Pair<Unit, Unit>(unit, branch);
					//branchMap.put(p, n);
					//pairGraphMapper.put(p, graph);
					dumpBranch(graph, p, n);
					System.out.println("Size:" + branchMap.size() + " "+ p + "-> " + n);
				}
			}
			/*
			invocationsAtUnit = extractMethodInvocations(unit);
			
			for (InvokeExpr invokeExpr : invocationsAtUnit) {
				SootMethod method = invokeExpr.getMethod();
				if(!alreadyExploredMethods.contains(method)){
					if(invocationStack.contains(method))
						continue;
					try{
						Body body = method.getActiveBody();
						alreadyExploredMethods.add(method);
						//@SuppressWarnings("unused")
						//DominatorAnalysisWraper invokedMethodAnalysis = new DominatorAnalysisWraper(new ExceptionalUnitGraph(body));
					}
					catch (Exception e) {
						// TODO: handle exception
						System.out.println("No active body for " + method);
					}
				}
				else{
					@SuppressWarnings("unused")
					int debug =1;
				}
			}
			*/
		}
		
		System.out.println("Removing " + graph.getBody().getMethod()  + " " + invocationStack.size());
		invocationStack.remove(graph.getBody().getMethod());
		
	}

	private HashSet<InvokeExpr> extractMethodInvocations(Unit unit) {
		ExtractMethodInvocationStmtSwitch methodInvocationSwitch = new ExtractMethodInvocationStmtSwitch();
		unit.apply(methodInvocationSwitch);
		HashSet<InvokeExpr> invocations = methodInvocationSwitch.getMethodInvocations();
		return invocations;
	}

	private int getNumberofStatementsDominatedBy(Unit branch, Unit ifStmt) {
		// TODO Auto-generated method stub
		HashSet<Integer> dominatedByLines = new HashSet<Integer>();
		int invocationLines = 0;
		for (Unit unit : graph) {
			int lineNumber = AppUtil.getStatementLineNumber((Stmt) ifStmt);
			if(domAnalysis.dominates((Stmt)branch, (Stmt)unit)){
				dominatedByLines.add(AppUtil.getStatementLineNumber((Stmt) unit));
				
				HashSet<InvokeExpr> invocations  = extractMethodInvocations(unit);
				for (InvokeExpr invocation : invocations) {
					SootMethod invokedMethod = invocation.getMethod();
					if(potentialLOCMap.keySet().contains(invokedMethod)){
						invocationLines  = invocationLines + potentialLOCMap.get(invokedMethod);
					}
					else{
						int n = getPotentialLOC(invocation);
						invocationLines  = invocationLines + n;
						potentialLOCMap.put(invokedMethod, n);
					}
				}
			}
		}
		return dominatedByLines.size() + invocationLines;
	}

	private int getPotentialLOC(InvokeExpr invocation) {
		
		
		SootMethod method = invocation.getMethod();
		System.out.println("getPotentialLOC " + method);
		HashSet<Integer> linesSet = new HashSet<Integer>();
		int locFromInvocations = 0;
		
		if(method.isConcrete()){
			if(!method.hasActiveBody()){
				System.out.println("no active body for : " + method.getSignature());
				return 0 ;
			}
		}
		else if(method.isPhantom()){
			return 0;
		}
		else if(method.isAbstract()){
			//Need to get all the methods that can be invoked and traverse all those for a conservative approach
			//For less conservative, we can pick a single method using some heuristics
			
		}
		else{
			if(!method.hasActiveBody()){
				return 0;
			}
			
			try {
				throw new Exception("Need to implement for non-concrete methods");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		LOCStack.add(invocation.getMethod());
		
		PatchingChain<Unit> unitsInMethod = method.getActiveBody().getUnits();
		for (Unit u : unitsInMethod) {
			int lineNo = AppUtil.getStatementLineNumber((Stmt) u);
			linesSet.add(lineNo);
			HashSet<InvokeExpr> invokedMethods;
			invokedMethods = extractMethodInvocations(u);
			
			
			for (InvokeExpr nestedInvocation : invokedMethods) {
				if(!potentialLOCMap.containsKey(nestedInvocation.getMethod())){
					if(!LOCStack.contains(nestedInvocation.getMethod())){
						int invocationLines = getPotentialLOC(nestedInvocation);
						potentialLOCMap.put(nestedInvocation.getMethod(), invocationLines);
						locFromInvocations = locFromInvocations + invocationLines;
					}	
				}
				else
					locFromInvocations = locFromInvocations + potentialLOCMap.get(nestedInvocation.getMethod());
			}
		}

		LOCStack.remove(invocation.getMethod());
		return linesSet.size() + locFromInvocations;
	}
	
}
