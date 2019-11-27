package com.nisum.napt.uiautomation.stepdefinitions;

import com.nisum.framework.runner.WebDriverManager;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;


public class Hooks {

    private static org.apache.log4j.Logger log = Logger.getLogger(Hooks.class);
    private static Scenario sem;

    @Before
    public void beforeScenario(Scenario sc) throws Exception {
        log.info("Scenario: " + sc.getName().toString());
        WebDriverManager.setDriver();
        sem = sc;
    }

    @After
    public void afterScenario(Scenario sc) {
        if (sc.isFailed()) {
            byte[] src = ((TakesScreenshot) WebDriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            sc.embed(src, "image/png");
        }
        WebDriverManager.destroyDriver();
    }


}
