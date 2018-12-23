package utils;

import java.io.File;

public class PackageHandler {
    private String classPath;    // Path to source .class files
    private String testPath;     // Path to test .class files
    private String classPackageName;  // Name of class package, period delimited
    private String testPackageName;   // Name of test package, period delimited

    private static PackageHandler singleton = null;

    private PackageHandler(){}

    public static void initialize(String rootPath, String classPackageName, String testPackageName) {
        if(singleton == null)
            singleton = new PackageHandler();
        File path = new File("myChecksum");
        if(!path.isDirectory())
            path.mkdirs();
        singleton.classPath = rootPath + "/target/classes/" + classPackageName.replace(".", "/");
        singleton.testPath = rootPath + "/target/test-classes/" + testPackageName.replace(".", "/");
        singleton.classPackageName = classPackageName.trim();
        singleton.testPackageName = testPackageName.trim();
    }

    public static String getClassPath() {
        return singleton.classPath;
    }
    public static String getTestPath() {
        return singleton.testPath;
    }
    public static String getClassPackageName() {
        return singleton.classPackageName;
    }
    public static String getTestPackageName() {
        return singleton.testPackageName;
    }
}
