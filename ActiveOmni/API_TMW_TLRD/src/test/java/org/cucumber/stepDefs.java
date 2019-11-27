package org.cucumber;

import com.jayway.jsonpath.JsonPath;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import net.javacrumbs.jsonunit.core.Option;
import org.ApiFramework.FileUtils;
import org.ApiFramework.ReadProperties;
import org.ApiFramework.RequestObject;
import org.ApiFramework.RestCall;
import org.ApiFramework.validator.JsonValidator;
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONObject;
import org.testng.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

/**
 * The type Step defs.
 */
public class stepDefs {

    /**
     * The Data.
     */
    static Map<String, String> data = new HashMap<String, String>();
    /**
     * The Jv.
     */
    JsonValidator jv = new JsonValidator();
    String random1=null;
    Response response;
    /**
     * Hit the with the parameter and input from file.
     *
     * @param endPoint  the end point
     * @param parameter the parameter
     * @param request   the request
     * @throws Throwable the throwable
     */
    @When("I hit the \"([^\"]*)\" with the \"([^\"]*)\" parameter and input from \"([^\"]*)\" file")
    public void i_hit_the_with_the_parameter_and_input_from_file(String endPoint, String parameter, String request) throws Throwable {

        RequestObject req = new RequestObject(request, parameter);
        System.out.println("EndPoint :"+ReadProperties.getProp(endPoint));
        response=RestCall.invoke(ReadProperties.getProp(endPoint), "", req);
        String res = response.getBody().asString();
        System.out.println("*************" + res);
        data.put("res", res);
        data.put("AccessToken",JsonPath.parse(res).read("access_token"));
        System.out.println("Access Token : "+data.get("AccessToken"));
    }

    @When("I hit TMW Login API with the \"([^\"]*)\" with the \"([^\"]*)\" parameter and input from \"([^\"]*)\" file")
    public void i_hit_Parent_API_with_the_with_the_parameter_and_input_from_file(String endPoint, String parameter, String request)throws Throwable  {
        // Write code here that turns the phrase above into concrete actions
        RequestObject req = new RequestObject(request, parameter);

        response=RestCall.invoke(ReadProperties.getProp(endPoint), "", req);
        String res = response.getBody().asString();
        System.out.println("*************" + res);
        data.put("res", res);
        data.put("AccessToken",JsonPath.parse(res).read("access_token"));
        System.out.println("Parent Access Token : "+data.get("AccessToken"));
    }

    @When("Get the Order Detail for StoreNo \"([^\"]*)\" and Id \"([^\"]*)\"")
    public void get_the_Order_Detail_for_StoreNo_and_Id(String string, String string2) throws Exception {
        RequestObject req= new RequestObject("menswearhouseSKU.json","getallsku_scenario1");
        String key1="Bearer "+data.get("AccessToken");
        System.out.println("token : " +key1);
        req.setHeader("Authorization",key1);
        req.setHeader("Content-Type","application/json");
        req.setPathParam("FullfilmentNo",data.get("STSOrderNumber"));
        //req.setPathParam("Organization",string2);
        response=RestCall.invoke(ReadProperties.getProp("Endpoint"), "", req);
        System.out.println(response.getBody().asString());
    }

    @When("Get the Order Detail for StoreNo \"([^\"]*)\" and Id \"([^\"]*)\" for ShipToAddress")
    public void get_the_Order_Detail_for_StoreNo_and_Id_for_ShipToAddress(String string, String string2) throws Exception {
        // Write code here that turns the phrase above into concrete actions
        RequestObject req= new RequestObject("menswearhouseSKU.json","getallsku_scenario1");
        String key1="Bearer "+data.get("AccessToken");
        System.out.println("token : " +key1);
        req.setHeader("Authorization",key1);
        req.setHeader("Content-Type","application/json");
        req.setPathParam("FullfilmentNo",data.get("STAOrderNumber"));
        //req.setPathParam("Organization",string2);
        response=RestCall.invoke(ReadProperties.getProp("Endpoint"), "", req);
        System.out.println(response.getBody().asString());
    }

    @When("Get the Catalog Detail for StoreNo 5\"([^\"]*)\" and Id \"([^\"]*)\"")
    public void get_the_Catalog_Detail_for_StoreNo_and_Id(String storeNo, String id) throws Exception {
        RequestObject req = new RequestObject("menswearhouseSKU.json", "getallsku_scenario1");
        req.setPathParam("StoreId",storeNo);
        req.setPathParam("Product",id);
        response=RestCall.invoke(ReadProperties.getProp("menswearhouseEndPoint"), "", req);
        String res = response.getBody().asString();
        data.put("res", res);
    }

    @When("I hit the \"([^\"]*)\" with the \"([^\"]*)\" from \"([^\"]*)\" file to create shipto store order")
    public void i_hit_the_with_the_from_file_to_create_shipto_store_order(String endPoint, String request, String parameter) throws Exception {

        RequestObject req = new RequestObject(parameter, request);
        String body=FileUtils.readFile("/src/test/java/org/TestData/requestTemplate/OrderCreate.json");
        Random rand = new Random();

        //Generating Random number
        int random_number=rand.nextInt(999999999);
        int random_usernumber=rand.nextInt(9999);

        random1=String.valueOf(random_number);
        String random=String.valueOf(random_usernumber);
        //System.out.println("Random Number :"+random_number);

        // Generating Random String
        String generatedString = RandomStringUtils.randomAlphabetic(4);
        System.out.println("Random String : " +generatedString);

        //Concat int and string
        String Random_Value="USER".concat(random);
        System.out.println("Random Value : " +Random_Value);
        //storing values in to data table
        data.put("STSOrderNumber",random1);
        data.put("Random_Value",Random_Value);

        //Update the json body with random value
        body=JsonPath.parse(body).put("$","FulfillmentId",random1).jsonString();
        body=JsonPath.parse(body).put("$.FulfillmentPackages[0].PackageDetail","FulfillmentId",random1).jsonString();
        body=JsonPath.parse(body).put("$.FulfillmentPackages[0].ShipToAddress","FirstName",Random_Value).jsonString();
        body=JsonPath.parse(body).put("$","CustomerFirstName",Random_Value).jsonString();
        body=JsonPath.parse(body).put("$.FulfillmentAddress[0].Address","FirstName",Random_Value).jsonString();
        body=JsonPath.parse(body).put("$.FulfillmentLine[0]","OrderId",random1).jsonString();
        body=JsonPath.parse(body).put("$.FulfillmentLine[0]","OrderLineId",random1).jsonString();
        req.setBody(body);
        //System.out.println("Body :"+body);

        String key1="Bearer "+data.get("AccessToken");
        System.out.println("token : " +key1);
        req.setHeader("Authorization",key1);
        req.setHeader("Content-Type","application/json");
            response=RestCall.invoke(ReadProperties.getProp(endPoint), "CreateOrder", req);
            String res = response.getBody().asString();
            data.put("STSorderresp",res);
            data.put("openstatus","1000.000");
            data.put("CustomerFirstName",Random_Value);
            System.out.println("*************" + res);
   }

    @Then("Validate the fullfillment Details")
    public void validate_the_fullfillment_Details(DataTable dataTable) throws Exception{
       List<Map<String,String>> value=dataTable.asMaps();
       System.out.println(value);
       for (Map param: value)
       {
           Assert.assertEquals(JsonPath.parse(data.get("STSorderresp")).read(param.get("Path").toString()),data.get(param.get("Key")));
       }
    }
    @Then("Validate the fullfillment Details for ShitToAddress")
    public void validate_the_fullfillment_Details_for_ShitToAddress(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String,String>> value=dataTable.asMaps();
        System.out.println(value);
        for (Map param: value)
        {
            Assert.assertEquals(JsonPath.parse(data.get("STAorderresp")).read(param.get("Path").toString()),data.get(param.get("Key")));
        }
    }

    @When("I hit the \"([^\"]*)\" with the \"([^\"]*)\" from \"([^\"]*)\" file to create shipto address order")
    public void i_hit_the_with_the_from_file_to_create_shipto_address_order(String endPoint, String request, String parameter) throws Exception {
        // Write code here that turns the phrase above into concrete actions
        RequestObject req = new RequestObject(parameter, request);
        String body=FileUtils.readFile("/src/test/java/org/TestData/requestTemplate/ShiptoAddOrderCreate.json");

        Random rand = new Random();

        //Generating Random number
        int random_number=rand.nextInt(999999999);
        int random_usernumber=rand.nextInt(9999);

        random1=String.valueOf(random_number);
        String random=String.valueOf(random_usernumber);
        //System.out.println("Random Number :"+random_number);

       //Concat int and string
        String Random_Value="USER".concat(random);
        System.out.println("Random Value : " +Random_Value);
        //storing values in to data table
        data.put("STAOrderNumber",random1);
        data.put("Random_Value",Random_Value);

        //Update the json body with random value
        body=JsonPath.parse(body).put("$","FulfillmentId",random1).jsonString();
        body=JsonPath.parse(body).put("$.FulfillmentPackages[0].PackageDetail","FulfillmentId",random1).jsonString();
        body=JsonPath.parse(body).put("$.FulfillmentPackages[0].ShipToAddress","FirstName",Random_Value).jsonString();
        body=JsonPath.parse(body).put("$","CustomerFirstName",Random_Value).jsonString();
        body=JsonPath.parse(body).put("$.FulfillmentAddress[0].Address","FirstName",Random_Value).jsonString();
        body=JsonPath.parse(body).put("$.FulfillmentLine[0]","OrderId",random1).jsonString();
        body=JsonPath.parse(body).put("$.FulfillmentLine[0]","OrderLineId",random1).jsonString();
        req.setBody(body);
        //System.out.println("Body :"+body);

        String key1="Bearer "+data.get("AccessToken");
        System.out.println("token : " +key1);
        req.setHeader("Authorization",key1);
        req.setHeader("Content-Type","application/json");
        response=RestCall.invoke(ReadProperties.getProp(endPoint), "CreateOrder", req);
        String res = response.getBody().asString();
        data.put("STAorderresp",res);
        data.put("openstatus","1000.000");
        data.put("Acceptedstatus","2000.000");
        data.put("Packedstatus","3000.000");
        data.put("Shippedstatus","5000.000");
        data.put("PickedUpstatus","6000.000");
        data.put("STACustomerFirstName",Random_Value);
        System.out.println("*************" + res);
    }
    @Then("validate with the expected response in the file \"([^\"]*)\"")
    public void validate_with_the_expected_response_in_the_file(String responseFile) throws Throwable {
        jv.validateJson(data.get("res"), "", responseFile);
    }


    @Then("validate with the expected response in the \"([^\"]*)\" with path \"([^\"]*)\"")
    public void validate_with_the_expected_response_in_the_file(String responseString,String path) throws Throwable {
        jv.validateJsonField(data.get("res"), path, responseString);
    }

    @Then("print with the expected response with path \"([^\"]*)\"")
    public void print_with_the_expected_response_in_the_file(String path) throws Throwable {
        jv.printJsonField(data.get("res"), path);
    }

    @Then("validate with the expected response Schema with FilePath \"([^\"]*)\"")
    public void validate_with_the_expected_responseschema_in_the_file(String path) throws Exception {
        response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchema(FileUtils.readFile(ReadProperties.getProp("result_body_path")+"/"+path)));
    }

    @Then("verify with the expected response in count \"([^\"]*)\" with path \"([^\"]*)\"")
    public void verify_with_the_expected_response_in_count_with_path(String count, String path) throws Exception {
        // Write code here that turns the phrase above into concrete actions
        jv.validateJsonArrayCount(data.get("res"),path,count);
    }


    @Then("validate with the all catalog response Schema with FilePath \"([^\"]*)\"")
    public void validate_all_the_expected_responseschema_in_the_file(String path) throws Exception {
        List<Map> x=JsonPath.parse(response.getBody().asString()).read("catalogEntryView[*].sKUs[*]");
        for (Map Cata: x)
        {
            System.out.println("Verifying ID  :"+Cata.get("uniqueID").toString());
            JSONObject jb = new JSONObject(Cata);
            assertThatJson(jb.toString()).when(Option.IGNORING_VALUES).isEqualTo(FileUtils.readFile(ReadProperties.getProp("result_body_path")+"/"+path));
        }

    }

}