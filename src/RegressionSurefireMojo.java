import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.surefire.SurefirePlugin;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.ResolutionScope;

import utils.LoggingHandler;
import utils.TestMediator;

@Mojo(name="main", defaultPhase=LifecyclePhase.TEST, threadSafe=true, requiresDependencyResolution=ResolutionScope.TEST)
public class RegressionSurefireMojo extends SurefirePlugin {

    @Parameter
    private String rootPath;
    @Parameter
    private String classPackageName;
    @Parameter
    private String testPackageName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            LoggingHandler.initializeLogger(this.getLog());

            LoggingHandler.info("");
            LoggingHandler.info("----------------");
            LoggingHandler.info("  Starting RTS");
            LoggingHandler.info("----------------");
            LoggingHandler.info("");

            TestMediator.buildDependencyTrees(rootPath, classPackageName, testPackageName);
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
