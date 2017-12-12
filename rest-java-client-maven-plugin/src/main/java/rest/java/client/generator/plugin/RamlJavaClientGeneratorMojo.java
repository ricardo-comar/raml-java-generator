package rest.java.client.generator.plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.mule.client.codegen.RamlJavaClientGenerator;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "generate-client", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@Execute(goal = "generate-client")
public class RamlJavaClientGeneratorMojo extends AbstractMojo {


    @Parameter(defaultValue = "${project.build.resources[0].directory}/api.raml")
    private String ramlFile;

    @Parameter()
    private String ramlURL;

    @Parameter(required = true)
    private String basePackage;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources")
    private String outputDir;


    @Parameter(required = true, readonly = true, defaultValue = "${project}")
    private MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            final List<URL> ramlUrls = new ArrayList<>();
            if (this.ramlURL != null && !this.ramlURL.isEmpty()) {
                final String[] urls = this.ramlURL.split(",");
                for (String url : urls) {
                    ramlUrls.add(new URL(url));
                }
            } else {
                final String[] paths = this.ramlFile.split(",");
                for (String path : paths) {
                    final File ramlFile = new File(path);
                    if (!ramlFile.exists()) {
                        getLog().error("Raml file not found " + ramlFile);
                    }
                    ramlUrls.add(ramlFile.toURI().toURL());
                }
            }

            for (URL ramlUrl : ramlUrls) {
                final RamlJavaClientGenerator ramlJavaClientGenerator = new RamlJavaClientGenerator(basePackage, new File(outputDir));
                ramlJavaClientGenerator.generate(ramlUrl);
            }
            project.addCompileSourceRoot(outputDir);

        } catch (Exception e) {
            throw new MojoExecutionException("Exception while generating client.", e);
        }
    }
}
