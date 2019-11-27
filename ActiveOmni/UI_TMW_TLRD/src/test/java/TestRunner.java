import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import cucumber.api.testng.CucumberFeatureWrapper;
import cucumber.api.testng.TestNGCucumberRunner;
import org.ApiFramework.Report.ReportGenerator;
import org.junit.runner.RunWith;
import org.testng.annotations.*;

//@RunWith(Cucumber.class)
@CucumberOptions(
        features = "features",
        glue = {"com.nisum.napt.uiautomation.stepdefinitions"},
        tags = {"@project_omni_ropis"},
        plugin = {"pretty",
                "html:target/cucumber_target.html", "json:target/cucumber.json"})
public class TestRunner extends AbstractTestNGCucumberTests {

    @AfterSuite
    public static void capture() {
        ReportGenerator.ReportGenerator();
    }

}

