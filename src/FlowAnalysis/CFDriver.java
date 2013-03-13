package FlowAnalysis;

import java.util.HashMap;
import java.util.HashSet;

import FlowSet.ControlFlowSet;
import soot.G;
import soot.Value;
import soot.options.Options;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;

public class CFDriver {
	
	public CFDriver(UnitGraph graph)
    {
    	System.out.println("method................." + graph.getBody().getMethod().getName() + "::::" + graph.getBody().getMethod().getDeclaringClass().getName());
        if(Options.v().verbose())
            G.v().out.println("[" + graph.getBody().getMethod().getName() +
                               "]     Constructing GuaranteedDefs...");
        
        DataFlowAnalysis dfAnalysis = new DataFlowAnalysis(graph, true, false);
        ControlFlowAnalysis cfAnalysis = new ControlFlowAnalysis(graph, dfAnalysis, new ControlFlowSet(), new HashSet<Value>(), new HashMap<Value, HashSet<Value>>());
        int i =0;
    }
}
