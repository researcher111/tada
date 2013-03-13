package utils;

import java.util.HashSet;
import java.util.List;

import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnNamePosTag;
import soot.tagkit.Tag;
import soot.util.Chain;

public class AppUtil {

	public static HashSet<SootMethod> getAllImplementations(SootMethod method) {
		Chain appClasses = Scene.v().getApplicationClasses();
		HashSet<SootClass> implementingClasses = new HashSet<SootClass>(1);
		HashSet<SootMethod> overridingMethods = new HashSet<SootMethod>(1);
		
		
		SootClass t = method.getDeclaringClass();
		if(/*t.isAbstract() || */t.isPhantom() || t.isPhantomClass()){
			boolean b1 = t.isAbstract();
			boolean be = t.isPhantom();
			boolean b3 = t.isPhantomClass();
			
			try {
				throw new Exception("Need to implement for Plantom Classes");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(t.isAbstract()){
			for (Object object : appClasses) {
				SootClass clazz = (SootClass)object;
				SootClass superClass =  clazz.getSuperclass();
				{
					if(superClass.getName().equals(t.toString()))
					{
						implementingClasses.add(clazz);
						SootMethod m2 =  clazz.getMethod(method.getName(), 
								method.getParameterTypes(), method.getReturnType());
						overridingMethods.add(m2);
					}
				}
			}
		}
		if(t.isInterface())
		{
			for (Object object : appClasses) {
				SootClass clazz = (SootClass)object;
				Chain<SootClass> interfaces =  clazz.getInterfaces();
				for (SootClass sootClass : interfaces) {
					if(sootClass.getName().equals(t.toString()))
					{
						implementingClasses.add(clazz);
						SootMethod m2 =  clazz.getMethod(method.getName(), 
								method.getParameterTypes(), method.getReturnType());
						overridingMethods.add(m2);
					}
				}
			}
		}
		return overridingMethods;
	}
	
	public static int getStatementLineNumber(Stmt s){
		List<Tag> tags = s.getTags();
		int line = -1;
		for (Tag tag : tags) {
			if(tag instanceof SourceLnNamePosTag){
				SourceLnNamePosTag lineTag = (SourceLnNamePosTag)tag;
				line = lineTag.startLn();
			}
			else if(tag instanceof LineNumberTag){
				LineNumberTag lineTag = (LineNumberTag)tag;
				line = lineTag.getLineNumber();
			}
		}
		return line;
	}
	
	public static boolean isDifferentStartEndPos(Stmt s){
		List<Tag> tags = s.getTags();
		for (Tag tag : tags) {
			if(tag instanceof SourceLnNamePosTag){
				SourceLnNamePosTag lineTag = (SourceLnNamePosTag)tag;
				if(lineTag.startLn() != lineTag.endLn())
					return true;
				else
					return false;
			}
		}
		return false;
	}



	public static Value getVaribleInMethod(SootMethod method, String name) {
		// TODO Auto-generated method stub
		Chain<Local> localVars = method.getActiveBody().getLocals();
		for (Local local : localVars) {
			if(local.getName().equals(name))
				return local;
		}
		try {
			throw new Exception("No variable with name " + name + " in method " + method);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	} 
}
