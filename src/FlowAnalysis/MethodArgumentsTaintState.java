package FlowAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import FlowSet.ControlFlowSet;

import soot.SootMethod;
import soot.Value;

/***
 * The state of arguments tainting when a method is explored
 * Keeping track of state helps in pruning out unnecessary duplicate explorations.
 * The state is governed by the taint map at the time of invocation and the control flow set
 * at the invocation statement. 
 * */
public class MethodArgumentsTaintState {

	HashMap<Value, HashSet<Value>> map;
	ControlFlowSet entryFlowSet;
	
	private DataFlowAnalysis analysisForThisState;
	public MethodArgumentsTaintState(HashMap<Value, HashSet<Value>> cfMap){
		map = new HashMap<Value, HashSet<Value>>();
		for (Value v : cfMap.keySet()) {
			HashSet<Value> valueSet = new HashSet<Value>();
			valueSet.addAll(cfMap.get(v));
			map.put(v, valueSet);
		}
	}
	
	public MethodArgumentsTaintState(HashMap<Value, HashSet<Value>> cfMap, ControlFlowSet flowSet){
		map = new HashMap<Value, HashSet<Value>>();
		for (Value v : cfMap.keySet()) {
			HashSet<Value> valueSet = new HashSet<Value>();
			valueSet.addAll(cfMap.get(v));
			map.put(v, valueSet);
		}
		entryFlowSet = (ControlFlowSet) flowSet.clone();
	}
	
	/**
	 * Checks whether a state is same as this state
	 * 
	 * */
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof MethodArgumentsTaintState))
			return false;
		MethodArgumentsTaintState that = (MethodArgumentsTaintState)obj;
		if(that.map.size() != this.map.size())
			return false;
		for (Value v : that.map.keySet()) {
			if(!this.map.containsKey(v))
				return false;
			if(!that.map.get(v).equals(this.map.get(v)))
				return false;
		}
		
		if(!that.entryFlowSet.getAllVars().equals(this.entryFlowSet.getAllVars()))
			return false;
		
		if(map.size() >0){
			int d =1;
			int e = d;
		}
		return true;
	}

	public void setAnalysisForThisState(DataFlowAnalysis analysisForThisState) {
		this.analysisForThisState = analysisForThisState;
	}

	public DataFlowAnalysis getAnalysisForThisState() {
		return analysisForThisState;
	}
	
	
}
