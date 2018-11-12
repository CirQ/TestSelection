import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.surefire.SurefirePlugin;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.ResolutionScope;

import utils.TestMediator;

@Mojo(name="TestSelection", defaultPhase=LifecyclePhase.TEST, threadSafe=true, requiresDependencyResolution=ResolutionScope.TEST)
public class RegressionSurefireMojo extends SurefirePlugin {

    @Parameter(property="args")
    private String[] args = null;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
//            Runtime.getRuntime().exec("mvn test-compile");

            System.out.println("\n\nStarting RTS");
            TestMediator.buildDependencyTrees(args[0], args[1], args[2]);
            List<String> excludedTests = TestMediator.getExcludedTests();
            if (getExcludes() != null) {
                excludedTests.addAll(getExcludes());
            }
            setExcludes(excludedTests);

            super.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
