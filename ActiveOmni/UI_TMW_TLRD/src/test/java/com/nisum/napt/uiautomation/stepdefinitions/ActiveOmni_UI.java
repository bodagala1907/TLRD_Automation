package com.nisum.napt.uiautomation.stepdefinitions;

import com.nisum.framework.interactions.Clicks;
import com.nisum.framework.interactions.Element;
import com.nisum.framework.interactions.Navigate;
import com.nisum.framework.interactions.Wait;
import com.nisum.framework.runner.EnvVariables;
import com.nisum.framework.runner.WebDriverManager;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.nisum.napt.uiautomation.stepdefinitions.ActiveOmni_API.*;


public class ActiveOmni_UI {

    private static Logger log = Logger.getLogger(ActiveOmni_UI.class);
    private WebDriver driver = WebDriverManager.getDriver();
    private Actions actions = new Actions(driver);
    private static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    private static Robot rb;
    private static Map<String, String> pdfText = new HashMap<>();
    private static String fulfillmentId;
    private WebDriverWait wait = new WebDriverWait(driver, 60);

    static {
        try {
            rb = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    @Given("I visit the Omni Enterprise website as a registered user")
    public void iVisitTheOmniEnterpriseWebsiteAsARegisteredUser() throws InterruptedException {
        Navigate.visit(EnvVariables.getEnvVariables().get("WebURL"));
        loginToActiveOmni();
        log.info("Navigated to the Omni Enterprise website");
        Wait.untilElementNotPresent(By.className("loading"));
        Wait.secondsUntilElementPresent("AO_home_page.omni_enterprise_title", 60);
        wait.until(ExpectedConditions.urlContains("/omnifacade/#/home"));
        String currentUrl = WebDriverManager.getCurrentUrl();
        Assert.assertTrue("User is on " + currentUrl, currentUrl.contains("/omnifacade/#/home"));
        log.info("Navigated to the Omni Enterprise home page");
    }

    private void loginToActiveOmni() {
        String userName = EnvVariables.getEnvVariables().get("ome_username");
        String password = EnvVariables.getEnvVariables().get("ome_password");
        if (WebDriverManager.getCurrentUrl().contains("org_login")) {
            log.info("Navigated to Omni Authentication Server page");
            Element.findElement("AO_login_page.login_username").sendKeys(userName);
            Element.findElement("AO_login_page.login_password").sendKeys(password);
            Clicks.javascriptClick("AO_login_page.login_button");
            log.info("Logged into the Omni Enterprise website");
        } else if (WebDriverManager.getCurrentUrl().contains("ssfedtst.tailoredbrands.com/login/forms/login.html?")) {
            Wait.untilElementPresent("AO_login_page.Please_Login_header");
            log.info("Navigated to Tailored Brands Login Page");
            Element.findElement("AO_login_page.LDAP_username").sendKeys(userName);
            Element.findElement("AO_login_page.LDAP_password").sendKeys(password);
            Clicks.javascriptClick("AO_login_page.LDAP_login_button");
            log.info("Logged into the Omni Enterprise website");
        }
    }

    @And("I navigated to store home page by creating an order with below params:")
    public void iNavigatedToStoreHomePageByCreatingAnOrderWithBelowParams(DataTable table) throws Exception {
        Wait.forPageReady();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[contains(text(),'OMNI ENTERPRISE')]")));
        Assert.assertTrue("OMNI ENTERPRISE text is not displayed on page", Element.elementPresent("AO_home_page.omni_enterprise_title"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='second-left " + "tile']//div[@class" + "='title'][contains(text(), " + "'Store')]")));
        Assert.assertTrue("Store tile is not displayed", Element.elementPresent("AO_home_page.store_tile"));
        Clicks.javascriptClick("AO_home_page.fullfillment_link");
        ArrayList<String> a = new ArrayList<>(driver.getWindowHandles());
        driver.close();
        driver.switchTo().window(a.get(1));
        createOrderUsingApi(table);
        Wait.untilElementPresent("select_store_page.search_store");
        Element.findElement("select_store_page.search_store").sendKeys(generatedData.get("Store_Id"));
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[@class='input-wrapper']//div//div")));
        Thread.sleep(5000);
        Element.findElement("select_store_page.select_store").click();
        log.info("Clicked on Fulfillment link under STORE tile and navigated to store page");
    }

    private void createOrderUsingApi(DataTable table) throws Exception {
        // Below line is commented due to the parent Authorization is not working with existing credentials
        //        ActiveOmni_API.getAuthentication("auth_api_endpoint", "system");
        ActiveOmni_API.getAuthentication("auth_api_endpoint", "childTWM");
        ActiveOmni_API.createOrderApi("create_order_api", table);
        String returnedFulfillStatus = getFulfillmentStatus("get_fulfillment_status", "1000.000");
        Assert.assertEquals("fulfillment Status Code is not matching with status id: " + returnedFulfillStatus, "OPEN", fulfillmentStatusCodes().get(returnedFulfillStatus));
        log.info(table.row(1).get(0) + " order is created successfully");
    }

    @Then("I should be navigated to the store home page")
    public void iShouldBeNavigatedToTheStoreHomePage() {
        Wait.forPageReady();
//        Wait.secondsUntilElementPresent(By.xpath("//span[@class='label navbar-title-store']"), 60);
        Wait.secondsUntilElementPresent("store_home_page.store_number", 60);
//        Wait.secondsUntilElementPresent(By.xpath("//*[@id='preparePickups-ui-fulfillment-home-l3']//ion-label"), 60);
        Wait.secondsUntilElementPresent("store_home_page.prepare_pickups_link", 60);
        Assert.assertTrue(driver.getCurrentUrl().contains("/storefacade/index.html"));
        log.info("Successfully navigated to Store Page");
        String actStore = Element.findElement("store_home_page.store_number").getText();
        Assert.assertEquals(generatedData.get("Store_Id"), actStore);
        log.info("Successfully verified the user is on store home page");
    }

    @And("I navigate to \"([^\"]*)\" page")
    public void iNavigateToPage(String pageType) {
        String eleText = pageType.toLowerCase().replace(" ", "_");
        WebElement navLink = Element.findElement("store_home_page." + eleText + "_link");
        Wait.untilElementPresent("store_home_page." + eleText + "_link");
        actions.click(navLink).perform();
        log.info("Clicked on " + pageType + " link");
        Wait.forPageReady();
        String expPageText = pageType.equals("Prepare Pickups") ? "PREPARE PICKUPS" : pageType.equals("Confirm Pickups") ? "CONFIRM PICKUP" : pageType.equals("Stage Pickups") ? "STAGE" : pageType.equals("Prepare Shipments") ? "PREPARE SHIPMENTS" : "PACK";
        String actPageText = Element.findElement("store_home_page.home_title").getText();
        Assert.assertEquals(expPageText, actPageText);
        log.info("Successfully navigated to: " + pageType);
    }

    @When("I search with the DO number")
    public void iSearchWithTheDONumber() throws Exception {
        fulfillmentId = ActiveOmni_API.generatedData.get("FulfillmentId");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='work-fulfillment-list-btn-filter']/span")));
        //        if (Element.elementPresent(By.xpath("//*[@id='work-resume-list-footer']/div/div/button/span/div"))) {
        //            Clicks.javascriptClick(By.xpath("//*[@id='work-resume-list-footer']/div/div/button/span/div"));
        //            log.info("Clicked the resume paused activity button");
        //        } else {
        try {
            Clicks.javascriptClick("pickups_shipments_page.filter_button");
            Wait.untilElementPresent("pickups_shipments_page.apply_button");
            WebElement clearButton = Element.findElement("pickups_shipments_page.clear_button");
            if (clearButton.isEnabled()) {
                clearButton.clear();
            }
            Element.findElement("pickups_shipments_page.search_field").sendKeys(fulfillmentId);
            Clicks.javascriptClick("pickups_shipments_page.apply_button");
            log.info("Searched with the DO number: " + fulfillmentId);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[contains(text(),'1 of max 10')]")));
            Assert.assertTrue(Element.elementPresent("pickups_shipments_page.sub_title_text"));
            Clicks.javascriptClick("pickups_shipments_page.start_picking_button");
            //            Thread.sleep(1000);
            Wait.forPageReady();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(),'PICK LIST')]")));
            String returnedFulfillStatus = ActiveOmni_API.getFulfillmentStatus("get_fulfillment_status", "2000.000");
            Assert.assertEquals("fulfillment Status Code is not matching with status id: " + returnedFulfillStatus, "ACCEPTED", ActiveOmni_API.fulfillmentStatusCodes().get(returnedFulfillStatus));
            log.info("Selected the DO to start picking");
        } catch (NoSuchFieldException e) {
            log.info("Unable to filter and select the order due to " + e.getMessage());
        }
        Assert.assertEquals("PICK LIST", Element.findElement("pick_list_page.pick_list_title").getText());
        Assert.assertTrue("Print Pick List button is not displayed", Element.elementPresent("pick_list_page.print_pick_list_button"));
        Assert.assertTrue("REJECT ITEM button is not displayed", Element.findElement("pick_list_page.reject_item_button").isEnabled());
        Assert.assertFalse("PICK NEXT button is Enabled", Element.findElement("pick_list_page.pick_next_button").isEnabled());
        log.info("Verified the store number on store home page");
    }

    @When("I complete the process of \"([^\"]*)\" from pick list page")
    public void iCompleteTheProcessOfFromPickListPage(String shipType) throws Exception {
        Clicks.javascriptClick("pick_list_page.print_pick_list_button");
        Thread.sleep(20000);
        try {
            ArrayList<String> a = new ArrayList<>(driver.getWindowHandles());
            driver.switchTo().window(a.get(1));
            escPrintPanel();
            driver.switchTo().window(a.get(2));
            driver.manage().window().maximize();
            actions.click().build().perform();
            String[] strAr = pdfReader();
            for (String st : strAr) {
                if (st.contains("Store") && st.contains("TKG")) {
                    pdfText.put(st.split(" ")[0], st.split(" ")[2]);
                    pdfText.put("Tracking", st.split(" ")[3]);
                }
                if (st.contains("TMW") && st.length() > 3) {
                    pdfText.put("SKU", st);
                    Assert.assertTrue("SKU number: " + pdfText.get("SKU") + " and ItemId: " + ActiveOmni_API.generatedData.get("ItemId") + " are not matching", pdfText.get("SKU").contains(ActiveOmni_API.generatedData.get("ItemId")));
                }
                if (st.contains("ITEMS")) {
                    pdfText.put("Items", st.split(" ")[0]);
                    pdfText.put("Unit", st.split(" ")[2]);
                }
            }
            log.info("Created MAP with required parameters: " + pdfText);
            driver.close();
            driver.switchTo().window(a.get(0));
            log.info("Collected all the required details from print pick list");
        } catch (Exception e) {
            log.info("" + e.getMessage());
        }

        String skuNum = ActiveOmni_API.generatedData.get("ItemId");
        String units = pdfText.get("Unit");

        String units_pkd = Element.findElement("pick_list_page.units_packed").getText();
        for (int i = 0; i < Integer.parseInt(units_pkd); i++) {
            Element.findElement("pick_list_page.barcode_text_field").sendKeys(skuNum);
            Clicks.javascriptClick("pick_list_page.go_button");
            log.info("Entered the SKU number: " + skuNum);
        }

        //        Thread.sleep(1000);
        Wait.secondsUntilElementNotPresent("pick_list_page.almost_done_text", 30);
        Assert.assertTrue("BACK TO LIST button is not displayed", Element.elementPresent("pick_list_page.back_to_list_button"));
        Assert.assertEquals("Units Picked is not equal to the order units", units + "/" + units, Element.findElement("pick_list_page.units_picked").getText());
        Wait.secondsUntilElementNotPresent("pick_list_page.submit_button", 2);
        Clicks.javascriptClick("pick_list_page.submit_button");
        log.info("Submitted the order for " + shipType + " successfully with all the items/units");
        Thread.sleep(10000);
        try {
            ArrayList<String> a = new ArrayList<>(driver.getWindowHandles());
            driver.switchTo().window(a.get(1));
            escPrintPanel();
            driver.switchTo().window(a.get(2));
            driver.manage().window().maximize();
            actions.click().build().perform();
            String[] strAr = pdfReader();
            if (shipType.equals("ROPIS")) {
                for (String st : strAr) {
                    if (st.contains("RESERVED ORDER FOR PICK UP")) {
                        Assert.assertEquals("RESERVED ORDER FOR PICK UP is not displayed in PDF", "RESERVED ORDER " + "FOR" + " PICK UP", st);
                    }
                    if (st.contains(ActiveOmni_API.generatedData.get("FirstName"))) {
                        Assert.assertEquals(ActiveOmni_API.generatedData.get("FirstName") + " is not displayed in PDF", ActiveOmni_API.generatedData.get("FirstName"), st);
                    }
                    if (st.contains(ActiveOmni_API.generatedData.get("FulfillmentId"))) {
                        Assert.assertEquals(ActiveOmni_API.generatedData.get("FulfillmentId") + " is not displayed in PDF", st.split(" ")[0], ActiveOmni_API.generatedData.get("FulfillmentId"));
                    }
                }
            }
            if (shipType.equals("Selfie")) {
                for (String st : strAr) {
                    if (st.contains(ActiveOmni_API.generatedData.get("FirstName"))) {
                        String exp = ActiveOmni_API.generatedData.get("FirstName") + " " + ActiveOmni_API.generatedData.get("LastName");
                        Assert.assertEquals(exp + " is not displayed in PDF", exp, st);
                    }
                    if (st.contains(ActiveOmni_API.generatedData.get("Email"))) {
                        Assert.assertEquals("Email is not displayed in PDF", ActiveOmni_API.generatedData.get("Email"), st.split(" ")[2].replace(":", ""));
                    }
                    if (st.contains(ActiveOmni_API.generatedData.get("FulfillmentId"))) {
                        Assert.assertEquals(ActiveOmni_API.generatedData.get("FulfillmentId") + " is not displayed in PDF", st.split(" ")[1], ActiveOmni_API.generatedData.get("FulfillmentId"));
                    }
                }
            }
            log.info("Created MAP with required parameters: " + pdfText);
            driver.close();
            driver.switchTo().window(a.get(0));
            log.info("Focused back on to the submitted successfully page");
        } catch (Exception e) {
            log.info("" + e.getMessage());
        }
        Wait.secondsUntilElementPresent("pick_list_page.success_message", 60);
        Assert.assertTrue("Prepare Pickups button is not visible", Element.elementPresent("pick_list_page.prepare_pickup_button"));
        Clicks.javascriptClick("pick_list_page.home_button");
        //        Thread.sleep(2000);
        String returnedFulfillStatus = ActiveOmni_API.getFulfillmentStatus("get_fulfillment_status", shipType.equals("Selfie") ? "6000.000" : "3000.000");
        Assert.assertEquals("fulfillment Status Code is not matching with status id: " + returnedFulfillStatus, shipType.equals("Selfie") ? "PICKED UP" : "PICKED", ActiveOmni_API.fulfillmentStatusCodes().get(returnedFulfillStatus));
        log.info("Processed the pick shipment and navigated back to home page");
    }

    private static String[] pdfReader() throws InterruptedException, IOException, UnsupportedFlavorException {
        Thread.sleep(2000);
        rb.keyPress(KeyEvent.VK_CONTROL);
        rb.keyPress(KeyEvent.VK_A);
        rb.keyRelease(KeyEvent.VK_A);
        rb.keyRelease(KeyEvent.VK_CONTROL);

        rb.keyPress(KeyEvent.VK_CONTROL);
        rb.keyPress(KeyEvent.VK_C);
        rb.keyRelease(KeyEvent.VK_C);
        rb.keyRelease(KeyEvent.VK_CONTROL);

        log.info("Copied the content from PDF");
        Thread.sleep(1000);
        String str = "";
        try {
            str = (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            log.error(e.getMessage());
        }
        log.info("Received the copied content from clipboard");
        return str.split("\n");
    }

    private static void escPrintPanel() {
        rb.keyPress(KeyEvent.VK_ESCAPE);
        rb.keyRelease(KeyEvent.VK_ESCAPE);
    }

    @And("I search with the fulfillment id to verify the status as \"([^\"]*)\"")
    public void iSearchWithTheFulfillmentIdToVerifyTheStatusAs(String statusType) throws InterruptedException {
        Wait.forPageReady();
        Element.findElement("confirm_pickups_page.barcode_text_field").sendKeys(fulfillmentId);
        Clicks.javascriptClick("confirm_pickups_page.search_button");
        Wait.untilElementNotPresent("confirm_pickups_page.status_type_text");
        wait.until(ExpectedConditions.numberOfElementsToBeLessThan(By.xpath("//ion-row[@class='work-pickup-item-container " + "row']"), 3));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='status-description sub-title-label']")));
        Assert.assertEquals(statusType, Element.findElement("confirm_pickups_page.status_type_text").getText());
        log.info("Verified the fulfillment id: " + fulfillmentId + " status as: " + statusType);
    }

    @And("I select the DO for completing the confirm pick up")
    public void iSelectTheDOForCompletingTheConfirmPickUp() throws Exception {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//ion-row[@class='work-pickup-item-container row']")));
        //                Thread.sleep(10000);
        Clicks.javascriptClick("prepare_pickups_page.do_element_text");
        Wait.untilElementPresent("prepare_pickups_page.verify_Button_Not_Verified");
        Clicks.javascriptClick("prepare_pickups_page.verify_customer_id_button");
        Clicks.javascriptClick("prepare_pickups_page.complete_order_button");
        Thread.sleep(1000);
        if (Element.elementPresent("prepare_pickups_page.pickup_customer_details")) {
            Clicks.javascriptClick("prepare_pickups_page.pickup_customer_details");
            Clicks.javascriptClick("prepare_pickups_page.complete_order_button");
        }
        //        Thread.sleep(1000);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".ma-popover-content .button-inner")));
        Element.findElement("prepare_pickups_page.reason_dropdown").click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(),'CUSTOMER NO SHOW')]")));
        Wait.untilElementPresent("prepare_pickups_page.customer_show_no_purchase_text");
        Clicks.javascriptClick("prepare_pickups_page.customer_show_purchased_text");
        Clicks.javascriptClick("prepare_pickups_page.reason_ok_button");
        Clicks.javascriptClick("prepare_pickups_page.complete_order_ok_button");
        Thread.sleep(2000);
        String returnedFulfillStatus = ActiveOmni_API.getFulfillmentStatus("get_fulfillment_status", "9000.000");
        Assert.assertEquals("fulfillment Status Code is not matching with status id: " + returnedFulfillStatus, "CANCELLED", ActiveOmni_API.fulfillmentStatusCodes().get(returnedFulfillStatus));
        log.info("Selected the DO and completed the confirm pick up");
    }

    @And("^I click on resume activity button$")
    public void iClickOnResumeActivityButton() throws Throwable {
        Thread.sleep(20000);
        if (Element.elementPresent("pickups_shipments_page.resume_paused_activity_button")) {
            Clicks.javascriptClick("pickups_shipments_page.resume_paused_activity_button");
            log.info("Clicked the resume paused activity button");
        }
    }

    @And("I search with tracking number")
    public void iSearchWithTrackingNumber() throws InterruptedException {
        String trackingNumber = pdfText.get("Tracking");
        String itemId = pdfText.get("SKU");
        Wait.forPageReady();
        Element.findElement("pack_shipments_page.packing_barcode_field").sendKeys(trackingNumber);
        Thread.sleep(2000);
        Clicks.javascriptClick("pack_shipments_page.go_button");
        log.info("Navigated to pack shipments page");
        Wait.secondsUntilElementPresent("pack_shipments_page.scan_barcode_field", 60);
        Element.findElement("pack_shipments_page.scan_barcode_field").sendKeys(itemId);
        Thread.sleep(2000);
        Clicks.javascriptClick("pack_shipments_page.scan_barcode_go_button");
        log.info("Searched with the tracking number to complete the pack shipments");
        Thread.sleep(5000);
        Wait.secondsUntilElementPresent("pack_shipments_page.delivery_method_text", 60);
        Assert.assertEquals("Delivery Method text is not matching in Pack Shipments page", "Delivery Method " + "ShipToAddress", Element.findElement("pack_shipments_page.delivery_method_text").getText());
        Assert.assertEquals("Fulfillment Id is not matching in Pack Shipments page", generatedData.get("FulfillmentId"), Element.findElement("pack_shipments_page.fullfillment_id").getText());
        Assert.assertEquals("Items and Packages are not matching", pdfText.get("Items"), Element.findElement("pack_shipments_page.packages_text").getText());
        Assert.assertEquals("Units and Packaed Units are not matching", pdfText.get("Unit"), Element.findElement("pack_shipments_page.packed_units_text").getText());
        log.info("Navigated to pack shipments page process page");
    }

    @When("I complete the order packing")
    public void iCompleteTheOrderPacking() throws Exception {
        String pkgID = Element.findElement("pack_shipments_page.package_number_text").getText().split("#")[1].replace(" ", "");
        Thread.sleep(5000);
        Clicks.javascriptClick("pack_shipments_page.submit_print_button");
        Wait.secondsUntilElementNotPresent("pack_shipments_page.submit_print_pkg_text", 60);

        if (Element.elementPresent("pack_shipments_page.retry_text")) {
            Wait.untilElementPresent("pack_shipments_page.internal_server_error_text");
            log.info("Label not generated ans dispalyed error message is : " + Element.findElement("pack_shipments_page.internal_server_error_text").getText());
        }
        Clicks.javascriptClick("pack_shipments_page.ok_button");
        Thread.sleep(10000);

        try {
            ArrayList<String> a = new ArrayList<>(driver.getWindowHandles());
            driver.switchTo().window(a.get(1));
            escPrintPanel();
            driver.switchTo().window(a.get(2));
            driver.manage().window().maximize();
            actions.click().build().perform();
            String[] strAr = pdfReader();
            for (String st : strAr) {
                if (st.contains("PKG")) {
                    pdfText.put("PackageNo", st);
                    Assert.assertEquals("", st, pkgID);
                }
                if (st.contains("TMW") && st.length() > 3) {
                    Assert.assertEquals("Sku number is not matching with PDF", pdfText.get("SKU"), st);
                }
                if (st.contains(ActiveOmni_API.generatedData.get("FulfillmentId"))) {
                    Assert.assertEquals(ActiveOmni_API.generatedData.get("FulfillmentId") + " is not displayed in PDF", st, ActiveOmni_API.generatedData.get("FulfillmentId"));
                }
                if (st.contains(ActiveOmni_API.generatedData.get("FirstName"))) {
                    String exp = ActiveOmni_API.generatedData.get("FirstName") + " " + ActiveOmni_API.generatedData.get("LastName");
                    Assert.assertEquals(exp + " is not displayed in PDF", exp, st);
                }
            }
            log.info("Created MAP with required parameters: " + pdfText);
            driver.close();
            driver.switchTo().window(a.get(0));
            log.info("Focused back on to the submitted successfully page");
        } catch (Exception e) {
            log.info("" + e.getMessage());
        }
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".positive-button")));
        Wait.secondsUntilElementPresent("pack_shipments_page.button1", 60);
        Clicks.javascriptClick("pack_shipments_page.button1");
        Wait.secondsUntilElementPresent("pack_shipments_page.taskgroup_button", 60);
        Clicks.javascriptClick("pack_shipments_page.taskgroup_button");
        String returnedFulfillStatus = ActiveOmni_API.getFulfillmentStatus("get_fulfillment_status", "4000.000");
        Assert.assertEquals("fulfillment Status Code is not matching with status id: " + returnedFulfillStatus, "PACKED", ActiveOmni_API.fulfillmentStatusCodes().get(returnedFulfillStatus));
    }

    @Given("I create an order with below params:")
    public void iCreateAnOrderWithBelowParams(DataTable table) throws Exception {
        createOrderUsingApi(table);
        log.info("Order Created  with given params");
    }
}