
import java.io.*;

import utils.TestMediator;


/**
 * Created by HL on 4/11/17.
 * 
 * Desc: 
 * This class is included for manual execution of the project. It is not used when the project is ran
 * as a surefire plugin.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        // Start Execution Timer
        long start = System.nanoTime();
        
        // Init Dependency Trees
        TestMediator.buildDependencyTrees();
        
        // Construct Regression Test String
        StringBuilder builder = new StringBuilder();
        builder.append("mvn test -DfailIfNoTests=false -Dtest=");
        for(String selectedTest: TestMediator.getSelectedTests()){
            builder.append(selectedTest);
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        
        // Print Regression Test String
        System.out.println(builder.toString());

        // Print Exection Time
        long duration = System.nanoTime() - start;
        double seconds = (double)duration / 1000000000.0;
        System.out.println("\nRegression test selection time: " + seconds + "seconds.");
    }
}

