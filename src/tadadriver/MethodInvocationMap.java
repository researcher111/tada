package tadadriver;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import Visitors.ExtractMethodInvocationExprSwitch;
import Visitors.ExtractMethodInvocationStmtSwitch;


import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.dava.toolkits.base.AST.traversals.AllVariableUses;
import soot.jimple.InvokeExpr;
import soot.tagkit.AnnotationArrayElem;
import soot.tagkit.AnnotationElem;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import utils.AppUtil;

/**
 * Contains the map of method invocations in the input application
 * */
public class MethodInvocationMap {

	public static  HashMap<SootMethod, HashSet<SootMethod>> methodsInvokedByThisMethod;
	public static HashMap<SootMethod, HashSet<SootMethod>> methodsInvokingThisMethod;
	private static HashSet<SootMethod> tadaMethods;
	public HashMap<SootMethod, HashSet<SootMethod>> implementorsMap;
	
	private static HashMap<SootMethod, HashMap<Value, String>> variableDataBaseArttrMap = new HashMap<SootMethod, HashMap<Value,String>>(1);
	public static HashMap<Value, String> allVarAttributeMap = new HashMap<Value, String>(1);
	
	
	public HashSet<SootMethod> methodsExecutingSQLQueries;
	
	/*Map of all the methods that transitively invoke a TaDa method*/
	public HashMap<SootMethod, HashSet<SootMethod>> tadaMethodInvokedByMap;
	
	public MethodInvocationMap(){
		methodsInvokedByThisMethod = new HashMap<SootMethod, HashSet<SootMethod>>(1);
		methodsInvokingThisMethod = new HashMap<SootMethod, HashSet<SootMethod>>(1);
		tadaMethods = new HashSet<SootMethod>(1);
		tadaMethodInvokedByMap = new HashMap<SootMethod, HashSet<SootMethod>>(1);
		implementorsMap = new HashMap<SootMethod, HashSet<SootMethod>>();
		methodsExecutingSQLQueries = new HashSet<SootMethod>(1);
	}
	
	
	public void addToMethodsInvokedBy(SootMethod m, SootMethod methodInvokedByM){
		if(methodsInvokedByThisMethod.get(m) == null){
			HashSet<SootMethod> methodSet =  new HashSet<SootMethod>(1);
			methodSet.add(methodInvokedByM);
			methodsInvokedByThisMethod.put(m, methodSet);
		}
		else
			methodsInvokedByThisMethod.get(m).add(methodInvokedByM);
	}
	
	
	public void addToMethodsInvokedBy(SootMethod m, HashSet<SootMethod> methodsInvokedByM){
		if(methodsInvokedByThisMethod.get(m) == null)
			methodsInvokedByThisMethod.put(m, methodsInvokedByM);
		else
			methodsInvokedByThisMethod.get(m).addAll(methodsInvokedByM);
	}
	
	
	
	public void addToMethodsInvoking(SootMethod m, SootMethod methodInvokingM){
		if(methodsInvokingThisMethod.get(m) == null){
			HashSet<SootMethod> methodSet =  new HashSet<SootMethod>(1);
			methodSet.add(methodInvokingM);
			methodsInvokingThisMethod.put(m, methodSet);
		}
		else{
			methodsInvokingThisMethod.get(m).add(methodInvokingM);
		}
	}
	
	public void addToMethodsInvoking(SootMethod m, HashSet<SootMethod> methodsInvokingM){
		if(methodsInvokingThisMethod.get(m) == null)
			methodsInvokingThisMethod.put(m, methodsInvokingM);
		else
			methodsInvokingThisMethod.get(m).addAll(methodsInvokingM);
	}
	
	public HashSet<SootMethod> getMethodsInvoking(SootMethod method){
		return methodsInvokingThisMethod.get(method);
	}
	
	public HashSet<SootMethod> getMethodsInvokedBy(SootMethod method){
		return methodsInvokedByThisMethod.get(method);
	}
	
	public void buildMethodInvocationMap(soot.util.Chain<SootClass> classes){
		int count =0;
		for (SootClass sootClass : classes) {
			for (SootMethod method : sootClass.getMethods()) {
				if(! method.hasActiveBody()){
					continue;
				}
				addIfTaDaMethod(method);
				for (Unit unit : method.getActiveBody().getUnits()) {
					ExtractMethodInvocationStmtSwitch visitor = new ExtractMethodInvocationStmtSwitch();
					unit.apply(visitor);
					HashSet<InvokeExpr> invocations = visitor.getMethodInvocations();
					for (InvokeExpr invokeExpr : invocations) {
						if(invokeExpr.toString().contains("@primitive.Unknown")){
							continue;
						}
						SootMethod invokedMethod = invokeExpr.getMethod();
						if(invokedMethod.getName().contains("execute") && 
								invokedMethod.getDeclaringClass().getName().contains("Statement")){
							methodsExecutingSQLQueries.add(method);
						}
						addToMethodsInvokedBy(method, invokeExpr.getMethod());
						addToMethodsInvoking(invokeExpr.getMethod(), method);
						count++;
					}
				}
			}
			
		}
		
	}
	
	public HashSet<SootMethod> getMethodsToExplore(int invocationLevel){
		//Get Top Level Methods Invoking tadaMethods
		HashSet<SootMethod> methodsToExplore = new HashSet<SootMethod>();
		
		for (SootMethod method : tadaMethods) {
			HashSet<SootMethod> invokingMethods = getMethodsInvokingAtLevel(method, invocationLevel);
			methodsToExplore.addAll(invokingMethods);
		}
		return methodsToExplore;
	}


	private HashSet<SootMethod> getMethodsInvokingAtLevel(SootMethod method,
			int level) {
		
		//Todo: include all the indirectly implemented interfaces and methods overridden in other classes
		HashSet<SootMethod> workSet = new HashSet<SootMethod>();
		workSet.add(method);
		
		HashSet<SootMethod> newWorkset = new HashSet<SootMethod>(1);
		HashSet<SootMethod> topLevel = new HashSet<SootMethod>();
		HashSet<SootMethod> processed = new HashSet<SootMethod>(1);
		for(int i=0; i< level; i++){
			if(i !=0){
				for (SootMethod sootMethod : newWorkset) {
					if(!processed.contains(sootMethod))
						workSet = new HashSet<SootMethod>(newWorkset);
				}
				newWorkset = new HashSet<SootMethod>(1);
			}
			if(workSet.isEmpty())
				break;
			
			HashSet<SootMethod> implementedMethods = new HashSet<SootMethod>();
			for (SootMethod m : workSet) {
				for (SootClass interfaze : m.getDeclaringClass().getInterfaces()) {
					try{
						SootMethod m2 =  interfaze.getMethod(m.getName(), 
								m.getParameterTypes(), m.getReturnType());
						if(!processed.contains(m2))
							implementedMethods.add(m2);
					}catch (RuntimeException e) {
						//e.printStackTrace();
					}
				}
			}
			workSet.addAll(implementedMethods);
			Iterator<SootMethod> iter = workSet.iterator();
			
			while(iter.hasNext()){
				SootMethod m = iter.next();
				if(getMethodsInvoking(m) != null){
					newWorkset.addAll(getMethodsInvoking(m));
				}
				else
					topLevel.add(m);
			}
			processed.addAll(workSet);
		}
		processed.remove(method);
		tadaMethodInvokedByMap.put(method, processed);
		topLevel.addAll(newWorkset);
		return topLevel;
	}


	private void addIfTaDaMethod(SootMethod method) {
		List<Tag> tags = method.getTags();
		for (Tag tag : tags) {
			if(tag instanceof VisibilityAnnotationTag){
				VisibilityAnnotationTag annot = (VisibilityAnnotationTag) tag;
				ArrayList<AnnotationTag> tgs = annot.getAnnotations();
				for (AnnotationTag annotationTag : tgs) {
					String t = annotationTag.getType();
					if(t.contains("TaDaMethod")){
						tadaMethods.add(method);
						if(!(annotationTag.getElemAt(0) instanceof AnnotationArrayElem)){
							try {
								throw new Exception();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							continue;
						}
						HashMap<Value, String> varDataMap = new HashMap<Value, String>(1);
							AnnotationArrayElem elem1 = (AnnotationArrayElem) annotationTag.getElemAt(0);
							AnnotationArrayElem elem2 = (AnnotationArrayElem) annotationTag.getElemAt(1);
							
							ArrayList<AnnotationElem> values1 = elem1.getValues();
							ArrayList<AnnotationElem> values2 = elem2.getValues();
							
							if(values1.size()== values2.size() && elem1.getName().equals("variablesToTrack") && elem2.getName().equals("correspondingDatabaseAttribute")){
								for (int i=0; i< values1.size(); i++) {
									String value1 = ((AnnotationStringElem)values1.get(i)).getValue();
									String value2 = ((AnnotationStringElem)values2.get(i)).getValue();
									Value var = AppUtil.getVaribleInMethod(method, value1);
									varDataMap.put(var, value2.toLowerCase());
								}
							} else
								try {
									if(values1.size()!= values2.size())
										throw new Exception("Size of the two attribues expected as same");
									throw new Exception("Illegal Attribute Value  " + elem1.getName() + " and/or " + elem2.getName());
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}		
						this.variableDataBaseArttrMap.put(method, varDataMap);
						allVarAttributeMap.putAll(varDataMap);
						return;
					}
				}
			}
		}
	}


	public void setTadaMethods(HashSet<SootMethod> tadaMethods) {
		this.tadaMethods = tadaMethods;
	}


	public static HashSet<SootMethod> getTadaMethods() {
		return tadaMethods;
	}
	
	public static HashMap<Value, String> getVarDatabaseAttributeMap(SootMethod method){
		return variableDataBaseArttrMap.get(method);
	}
}
