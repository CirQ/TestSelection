import org.apache.maven.plugin.surefire.SurefirePlugin;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import utils.LoggingHandler;

@Mojo(name="fake", defaultPhase=LifecyclePhase.TEST, threadSafe=true, requiresDependencyResolution=ResolutionScope.TEST)
public class RegressionSurefireFakeMojo extends SurefirePlugin {

    @Parameter
    private String rootPath;
    @Parameter
    private String classPackageName;
    @Parameter
    private String testPackageName;

    @Override
    public void execute() {
        LoggingHandler.initializeLogger(this.getLog());

        LoggingHandler.info("");
        LoggingHandler.info("----------------");
        LoggingHandler.info("  Starting RTS");
        LoggingHandler.info("----------------");
        LoggingHandler.info("");

        LoggingHandler.info("but do nothing........");
    }
}
