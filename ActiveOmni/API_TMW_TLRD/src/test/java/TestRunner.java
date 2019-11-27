import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import org.junit.runner.RunWith;


/**
 * The type Test runner.
 */
//@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"src/test/java/org/cucumber/ShipToStore.feature"},
        glue = {"src/test/java/org/cucumber/stepDefs"},
        tags = {"@regression"},
        plugin = {"pretty",
        "html:target/cucumber_target.html", "json:target/cucumber.json"}
        )

public class TestRunner extends AbstractTestNGCucumberTests {
}

