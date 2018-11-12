package dependencyTrees;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import utils.PackageHandler;

public class ClassNode {
	public static Map<String, ClassNode> instances = new HashMap<>();
	
	private Set<ClassNode> parents;
	private String className;
	private boolean needToRetest;
	
	private ClassNode(String className) {
		this.parents = new HashSet<>();
		this.className = className;
		this.needToRetest = false;
	}
	
	public static void InitClassTree() throws IOException {
		InitClassTreeNodes(PackageHandler.getClassPath(), PackageHandler.getClassPackageName());
		
        Process pr = Runtime.getRuntime().exec("jdeps -J-Duser.language=en -verbose:class -filter:none " + PackageHandler.getClassPath());
        BufferedReader jDepsReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        
        String jDepsLine = null;
        ClassNode classNode = null;
        while ((jDepsLine = jDepsReader.readLine()) != null) {
        	jDepsLine = jDepsLine.trim();
        	
        	// Determine if a new classNode is being referenced
        	if (jDepsLine.startsWith(PackageHandler.getClassPackageName())) {
        		if (!jDepsLine.contains("$")) {
            		String className = jDepsLine.split("\\s+")[0];
            		classNode = ClassNode.instances.get(className);
        		}
        		else {
        			classNode = null;
        		}
        	}
        	else if (classNode != null) {
        		String dependencyName = jDepsLine.split("\\s+")[1];
        		if (dependencyName.startsWith(PackageHandler.getClassPackageName()) && !dependencyName.contains("$")) {
        			// When a 'parent' class depends on a 'child' class
            		// we add the 'parent' to the child's list of 'parents'
            		ClassNode.instances.get(dependencyName).addParent(classNode);
        		}
        	}
        }
	}
	
	public static void InitClassTreeNodes(String directoryName, String packageName) {
		File directory = new File(directoryName);
		 
		// get all the files from a directory
        for (File file: directory.listFiles()) {
            if (file.isFile()) {
                String fileName = file.getName();
                if (fileName.endsWith(".class") && !fileName.contains("$")) {
					String className = packageName + "." + fileName.split("\\.")[0];
                	addClassNode(className);
                }
            } 
            else if (file.isDirectory()) {
            	String newPackageName = packageName + "." + file.getName();
            	InitClassTreeNodes(file.getAbsolutePath(), newPackageName);
            }
        }
	}

	public static void addClassNode(String className) {
    	ClassNode node = new ClassNode(className);
		ClassNode.instances.put(className, node);
	}
	
	public void addParent(ClassNode parent) {
		this.parents.add(parent);
	}
	
	@Override
	public String toString() {
		String desc = className + ": ";
		for (ClassNode parent: parents) {
			desc += parent.getClassName() + ", ";
		}
		return desc;
	}
	
	public String getClassName() {
		return className;
	}

	public boolean isNeedToRetest() {
		return needToRetest;
	}

	public void setNeedToRetest(boolean needToRetest) {
		// Only update if not already true and attempting to set true
		if (!this.needToRetest && needToRetest) {
			this.needToRetest = true;
			for (ClassNode parent: parents) {
				parent.setNeedToRetest(true);
			}
		}
	}
}
