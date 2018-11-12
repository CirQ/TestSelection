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

public class TestNode {
    public static Map<String, TestNode> instances = new HashMap<>();

    private Set<TestNode> parents;
    private Set<ClassNode> dependencies;
    private String className;	// TODO: can modify to testName?
    private boolean needToRetest;

    private TestNode(String className) {
        this.parents = new HashSet<>();
        this.dependencies = new HashSet<>();
        this.className = className;
        this.needToRetest = false;
    }

    public static void InitTestTree() throws IOException {
        InitTestTreeNodes(PackageHandler.getTestPath(), PackageHandler.getTestPackageName());

        Process pr = Runtime.getRuntime().exec("jdeps -J-Duser.language=en -verbose:class -filter:none " + PackageHandler.getTestPath());
        BufferedReader jDepsReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String jDepsLine = null;
            TestNode testNode = null;
            while ((jDepsLine = jDepsReader.readLine()) != null) {
                jDepsLine = jDepsLine.trim();

                // Determine if a new testNode is being referenced
            if (jDepsLine.startsWith(PackageHandler.getTestPackageName())) {
                if (!jDepsLine.contains("$")) {
                    String className = jDepsLine.split("\\s+")[0];
                    testNode = TestNode.instances.get(className);
                }
                else {
                    testNode = null;
                }
            }
            else if (testNode != null) {
                String dependencyName = jDepsLine.split("\\s+")[1];
                if (dependencyName.startsWith(PackageHandler.getTestPackageName()) && !dependencyName.contains("$")) {
                    if (jDepsLine.endsWith("not found")) {
                        // Determine if this is a class dependency or a test dependency
                        // Note: only class dependencies end with "not found"
                        testNode.dependencies.add(ClassNode.instances.get(dependencyName));
                    }
                    else {
                        // When a 'parent' class depends on a 'child' class
                        // we add the 'parent' to the child's list of 'parents'
                        TestNode.instances.get(dependencyName).addParent(testNode);
                    }
                }
            }
        }
    }

    public static void InitTestTreeNodes(String directoryName, String packageName) {
         File directory = new File(directoryName);

        // get all the files from a directory
        for (File file: directory.listFiles()) {
            if (file.isFile()) {
                String fileName = file.getName();
                if (fileName.endsWith(".class") && !fileName.contains("$")) {
                    String className = packageName + "." + fileName.split("\\.")[0];
                    addTestNode(className);
                }
            } 
            else if (file.isDirectory()) {
                String newPackageName = packageName + "." + file.getName();
                InitTestTreeNodes(file.getAbsolutePath(), newPackageName);
            }
        }
    }

    public static void addTestNode(String className) {
        TestNode node = new TestNode(className);
        TestNode.instances.put(className, node);
    }

    public void addParent(TestNode parent) {
        this.parents.add(parent);
    }

    public void checkIfNeedRetest(){
        for(ClassNode dependency : dependencies){
            if(dependency.isNeedToRetest()){
                this.needToRetest = true;

                // Use below to percolate class dependency
                // changes up to parent test classes

                //this.setNeedToRetest(true);
            }
        }
    }

    @Override
    public String toString() {
        String desc = className + "\n: parents - ";

        for (TestNode parent : parents) {
            desc += parent.getClassName() + ", ";
        }

        desc += "\n: dependencies - ";
        for (ClassNode dependency : dependencies) {
            desc += dependency.getClassName() + ", ";
        }
        System.out.println("");

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
            for (TestNode parent : parents) {
                parent.setNeedToRetest(true);
            }
        }
    }
}
