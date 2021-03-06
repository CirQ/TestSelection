package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dependencyTrees.ClassNode;
import dependencyTrees.TestNode;

public class TestMediator {

    public static final String TEST_CHECKSUM_FILE = "testChecksum.txt";
    public static final String CLASS_CHECKSUM_FILE = "classChecksum.txt";

    public static List<String> getSelectedTests() {
        List<String> selectedTests = new ArrayList<>();
        for(TestNode instance: TestNode.instances.values()){
            if(instance.isNeedToRetest()){
                selectedTests.add(instance.getClassName());
            }
        }
        return selectedTests;
    }

    public static List<String> getExcludedTests() {
        List<String> excludedTests = new ArrayList<>();
        for(TestNode instance: TestNode.instances.values()){
            if(!instance.isNeedToRetest() || instance.getClassName().contains("$")){
                excludedTests.add(instance.getClassName());
            }
        }
        return excludedTests;
    }

    public static void buildDependencyTrees(String rootPath, String classPackageName, String testPackageName) throws IOException {
        // Compute Class and Test Dependency Trees
        PackageHandler.initialize(rootPath, classPackageName, testPackageName);
        ClassNode.InitClassTree();
        TestNode.InitTestTree();

        // Compute Test checksums and mark associated nodes
        CheckSumHandler testCheckSumHandler = new CheckSumHandler(TEST_CHECKSUM_FILE, true);
        testCheckSumHandler.doChecksum(PackageHandler.getTestPath());
        for (String dangerousTest: testCheckSumHandler.getDangerousClasses()) {
            TestNode.instances.get(dangerousTest).setNeedToRetest(true);
        }

        // Compute Class checksums and mark associated nodes
        CheckSumHandler classCheckSumHandler = new CheckSumHandler(CLASS_CHECKSUM_FILE, false);
        classCheckSumHandler.doChecksum(PackageHandler.getClassPath());
        for (String dangerousClass: classCheckSumHandler.getDangerousClasses()) {
            ClassNode.instances.get(dangerousClass).setNeedToRetest(true);
        }

        // Mark tests which depend on a dangerous class
        for (TestNode instance: TestNode.instances.values()) {
            instance.checkIfNeedRetest();
        }
    }

    public static void printDependencyTrees() {
        System.out.println("Class Tree: ");
        for (ClassNode node: ClassNode.instances.values()) {
            System.out.println(node);
        }

        System.out.println("Test Tree: ");
        for (TestNode node: TestNode.instances.values()) {
            System.out.println(node);
        }
    }

}
