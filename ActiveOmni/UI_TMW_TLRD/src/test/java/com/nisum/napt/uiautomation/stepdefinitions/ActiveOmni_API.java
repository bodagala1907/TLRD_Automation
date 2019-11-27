package com.nisum.napt.uiautomation.stepdefinitions;

import com.jayway.jsonpath.JsonPath;
import com.nisum.framework.runner.EnvVariables;
import com.nisum.framework.utils.Utils;
import com.nisum.napt.uiautomation.utils.Common;
import com.nisum.napt.uiautomation.utils.CustomerData;
import io.cucumber.datatable.DataTable;
import io.restassured.response.Response;
import org.ApiFramework.RequestObject;
import org.ApiFramework.RestCall;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActiveOmni_API {

    private static Logger log = Logger.getLogger(ActiveOmni_API.class);
    private static String webUrl = EnvVariables.getEnvVariables().get("WebURL");
    private static String authUrl = webUrl.replace("tssls4", "tssls4-auth");
    private static Response response;
    public static Map<String, String> generatedData = new HashMap<>();

    /**
     * Get the Authentication using auth api's.
     *
     * @param apiType  the apiEndPoint
     * @param authType the AuthenticationType
     * @throws Exception the throwable
     */
    public static void getAuthentication(String apiType, String authType) throws Exception {
        response = RestCall.invoke(authUrl, "", generateReqObj(apiType, authType));
        String resBody = response.getBody().asString();
        int statusCode = response.getStatusCode();
        Assert.assertEquals("Unable to do Auth Api call due to the status code: " + statusCode, 200, statusCode);
        generatedData.put("AccessToken", JsonPath.parse(resBody).read("access_token"));
        log.info("System Admin Authentication is completed successfully");
    }

    private static RequestObject generateReqObj(String apiType, String authType) throws Exception {
        RequestObject req = new RequestObject("tlrdAoApis.json", apiType);
        String user = authType.equals("system") ? "ome_username" : "childTMW_user";
        String pass = authType.equals("system") ? "ome_password" : "childTMW_pass";
        req.setQueryParam("username", EnvVariables.getEnvVariables().get(user));
        req.setQueryParam("password", EnvVariables.getEnvVariables().get(pass));
        req.setAuth("user", EnvVariables.getEnvVariables().get("auth_user"));
        req.setAuth("pass", EnvVariables.getEnvVariables().get("auth_pass"));
        log.info("Request params are passed successfully");
        return req;
    }

    public static void createOrderApi(String apiType, DataTable table) throws Exception {
        RequestObject req = new RequestObject("tlrdAoApis.json", apiType);
        req.setBody(updateCreateOrderJson(table));
        req.setHeader("Authorization", "Bearer " + generatedData.get("AccessToken"));
        req.setHeader("Content-Type", "application/json");
        response = RestCall.invoke(webUrl, "CreateOrder", req);
        String returnedFulfillStatus = verifyResponse(response, "1000.000");
        Assert.assertEquals("fulfillment Status Code is not matched with status id: " + returnedFulfillStatus, "OPEN", fulfillmentStatusCodes().get(returnedFulfillStatus));
        log.info("Created " + table.row(1).get(0) + "order with updated values using create API");

    }

    private static String updateCreateOrderJson(DataTable table) throws IOException {
        String orderType = table.row(1).get(0);
        String addType = table.row(1).get(1);
        String shipType = table.row(1).get(2);
        String lineType = table.row(1).get(3);
        String giftMsg = table.row(1).get(4);
        //Generating Random order, fulfillment, name and email
        generatedData.put("Email", CustomerData.getRandomEmail());
        generatedData.put("OrderId", String.valueOf(CustomerData.getRandomNumber(0, 1000000000)));
        generatedData.put("OrderLineId", String.valueOf(CustomerData.getRandomNumber(0, 1000000000)));
        generatedData.put("FulfillmentId", String.valueOf(CustomerData.getRandomNumber(0, 10000000)));
        generatedData.put("FirstName", CustomerData.getRandomFirstName());
        generatedData.put("LastName", CustomerData.getRandomLastName());
        log.info("Generated order, fulfillment, name and email for updating order json");

        String jsonType = orderType.equals("Selfie") ? "ShipToStoreCreateOrder.json" : (orderType.equals("ROPIS") ? "RopisCreateOrder.json" : "ShipToAddressCreateOrder.json");

        File filePath = Common.getResourceFile(jsonType);

        JSONObject createOrderBody = new JSONObject(Utils.readTextFile(filePath));

        String dueDate = orderType.equals("Selfie") ? getDate(3) : getDate(10);

        createOrderBody.put("FulfillmentId", generatedData.get("FulfillmentId"));


        if (createOrderBody.getJSONArray("FulfillmentAddress").length() == 1 || createOrderBody.getJSONArray("FulfillmentAddress").getJSONObject(0).getJSONObject("AddressTypeId").get("AddressTypeId").equals("Billing")) {
            createOrderBody.getJSONArray("FulfillmentAddress").getJSONObject(0).put("Address", addAddress(addType).put("Email", generatedData.get("Email")));
        }
        if (createOrderBody.getJSONArray("FulfillmentAddress").length() > 1 && createOrderBody.getJSONArray("FulfillmentAddress").getJSONObject(1).getJSONObject("AddressTypeId").get("AddressTypeId").equals("Shipping")) {
            createOrderBody.getJSONArray("FulfillmentAddress").getJSONObject(1).put("Address", addAddress(addType).put("Email", JSONObject.NULL));
        }

        if (createOrderBody.getJSONArray("FulfillmentLine").length() == 1) {
            createOrderBody.getJSONArray("FulfillmentLine").put(0, addLineItems("single"));

            generatedData.put("ItemId", createOrderBody.getJSONArray("FulfillmentLine").getJSONObject(0).get("ItemId").toString());
        }

        if (createOrderBody.getJSONArray("FulfillmentLine").length() != 2 && lineType.equals("multiple")) {
            createOrderBody.getJSONArray("FulfillmentLine").put(1, addLineItems(lineType));
        }

        createOrderBody.put("CustomerFirstName", generatedData.get("FirstName"));
        createOrderBody.put("CustomerLastName", generatedData.get("LastName"));
        String shipMethod = shipType.equals("Standard") ? "UGRD" : (shipType.equals("Rush") ? "UNDA" : (shipType.equals("Express") ? "U2DA" : "USP1"));

        if (!orderType.equals("ROPIS")) {
            createOrderBody.getJSONObject("Extended").put("GiftMessage", addGiftMsg(giftMsg));
            createOrderBody.put("CustomerEmail", generatedData.get("Email"));
            createOrderBody.put("OrderCaptureDate", getDate(0));
            createOrderBody.put("ShippingDueDate", dueDate);
            createOrderBody.put("DeliveryDueDate", dueDate);
            createOrderBody.getJSONArray("FulfillmentPackages").getJSONObject(0).put("ServiceLevelCode", shipMethod);
            createOrderBody.put("ShipViaId", shipMethod);
            createOrderBody.getJSONArray("FulfillmentPackages").getJSONObject(0).put("ShipToAddress", addAddress(addType).put("Email", JSONObject.NULL));
            createOrderBody.getJSONArray("FulfillmentPackages").getJSONObject(0).getJSONObject("PackageDetail").put("FulfillmentId", generatedData.get("FulfillmentId"));
        }
        generatedData.put("Store_Id", createOrderBody.get("ShipFromLocationId").toString());


        log.info("Create order json is updated successfully");
        return createOrderBody.toString();
    }

    private static JSONObject addAddress(String addType) {
        JSONObject add = Common.getRandomAddress(addType);
        JSONObject address = new JSONObject();
        address.put("FirstName", generatedData.get("FirstName"));
        address.put("State", add.get("address_state"));
        String phoneNumber = add.get("phone_area_code") + "-" + CustomerData.getRandomNumber(0, 1000) + "-" + CustomerData.getRandomNumber(0, 10000);
        address.put("Phone", phoneNumber);
        address.put("Address2", add.get("address_line_2"));
        address.put("Address3", JSONObject.NULL);
        address.put("PostalCode", add.get("address_zip_code"));
        address.put("Country", add.get("country_code"));
        address.put("LastName", generatedData.get("LastName"));
        address.put("Address1", add.get("address_line_1"));
        address.put("City", add.get("address_city"));
        address.put("County", JSONObject.NULL);
        return address;
    }

    private static JSONObject addLineItems(String lineType) {
        JSONObject lineItem = new JSONObject();
        lineItem.put("ItemUnitPrice", "699.99");
        lineItem.put("WeightUOM", "LB");
        lineItem.put("IsHazmat", "false");
        lineItem.put("Extended", addVAS());
        lineItem.put("OrderId", generatedData.get("OrderId"));
        lineItem.put("ItemId", "TMW304143210");
        lineItem.put("ItemDescription", "CK 2B SV FF TUX");
        lineItem.put("ItemUnitWeight", "1");
        lineItem.put("SupplyTypeId", "OnHand");
        lineItem.put("FulfillmentLineStatusId", "1000.000");
        lineItem.put("ItemColor", "10");
        lineItem.put("FulfillmentLineId", lineType.equals("single") ? "1" : "2");
        lineItem.put("OrderLineId", lineType.equals("single") ? generatedData.get("OrderLineId") : (Integer.parseInt(generatedData.get("OrderLineId")) + 1));
        lineItem.put("ItemStyle", "3041");
        lineItem.put("PipelineId", "FULFILLMENT_EXECUTION");
        lineItem.put("ItemSize", "432");
        lineItem.put("OrderedQty", "1");
        lineItem.put("AsnId", JSONObject.NULL);
        lineItem.put("QuantityUom", "U");
        return lineItem;
    }

    private static JSONObject addVAS() {
        JSONObject vas = new JSONObject();
        vas.put("VasInstruction1", JSONObject.NULL);
        vas.put("VasInstruction4", JSONObject.NULL);
        vas.put("VasInstruction5", JSONObject.NULL);
        vas.put("Comments", JSONObject.NULL);
        vas.put("VasInstruction2", JSONObject.NULL);
        vas.put("VasInstruction3", JSONObject.NULL);
        vas.put("VasInstruction10", JSONObject.NULL);
        vas.put("VasTypeId1", JSONObject.NULL);
        vas.put("VasTypeId2", JSONObject.NULL);
        vas.put("VasTypeId3", JSONObject.NULL);
        vas.put("VasTypeId4", JSONObject.NULL);
        vas.put("VasTypeId10", JSONObject.NULL);
        vas.put("VasTypeId5", JSONObject.NULL);
        vas.put("VasInstruction8", JSONObject.NULL);
        vas.put("VasTypeId6", JSONObject.NULL);
        vas.put("VasInstruction9", JSONObject.NULL);
        vas.put("VasInstruction6", JSONObject.NULL);
        vas.put("VasTypeId7", JSONObject.NULL);
        vas.put("VasInstruction7", JSONObject.NULL);
        vas.put("VasTypeId8", JSONObject.NULL);
        vas.put("VasTypeId9", JSONObject.NULL);
        return vas;
    }

    private static String addGiftMsg(String giftMsg) {
        String gift_msg;
        if (giftMsg.equals("false") || giftMsg.equals("")) {
            gift_msg = null;
        } else {
            gift_msg = "The gift message will be printed on the packing slip Hurray!!!! :)";
        }
        return gift_msg;
    }

    public static String getFulfillmentStatus(String apiType, String fulfillmentStatus) throws Exception {
        RequestObject req = new RequestObject("tlrdAoApis.json", apiType);
        req.setHeader("Authorization", "Bearer " + generatedData.get("AccessToken"));
        req.setHeader("Content-Type", "application/json");
        req.setPathParam("FullfilmentNo", generatedData.get("FulfillmentId"));
        req.setQueryParam("Organization", "TMW");
        response = RestCall.invoke(webUrl, "", req);
        log.info("Received the fulfillment status for fulfillment no: " + generatedData.get("FulfillmentId"));
        return verifyResponse(response, fulfillmentStatus);
    }

    public static Map<String, String> fulfillmentStatusCodes() {
        Map<String, String> statusCodes = new HashMap<>();
        //Units have been created and not yet attempted for allocation.
        statusCodes.put("1000.000", "OPEN");
        //Units has been attempted for allocation but failed to get allocation due to inventory unavailability.
        statusCodes.put("1500.000", "Back Ordered");
        //Units have been allocated and inventory has been reserved for it.
        statusCodes.put("2000.000", "ACCEPTED");
        //The units have been picked.
        statusCodes.put("3000.000", "PICKED");
        //The units have been sorted.
        statusCodes.put("3300.000", "SORTED");
        //The fulfillment system has indicated to OM that the units are in-process. Hard-allocated in WM, Accepted by
        // store fulfillment, etc.
        statusCodes.put("3500.000", "IN PACKING");
        //The units have been packed.
        statusCodes.put("4000.000", "PACKED");
        //The units have been partially shipped.
        statusCodes.put("4500.000", "PARTIALLY SHIPPED");
        //The units have been shipped.
        statusCodes.put("5000.000", "SHIPPED");
        //The units have been picked up.
        statusCodes.put("6000.000", "PICKED UP");
        //Units have been fulfilled; this state includes both Shipped and Picked Up units.
        statusCodes.put("7000.000", "Fulfilled");
        //Return The customer has initiated a return, but items have not been received by the retailer.
        statusCodes.put("8000.000", "PENDING");
        //Units have been returned to the retailer
        statusCodes.put("8500.000", "RETURNED");
        //Order line has been canceled
        statusCodes.put("9000.000", "CANCELLED");
        log.info("Returned the Status Name based on Status ID");
        return statusCodes;
    }

    private static String getDate(int days) {
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm");
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        cal.getTime();
        return dateFormat.format(cal.getTime()).replace(" ", "T");
    }

    private static String verifyResponse(Response response, String fulfillmentStatus) {
        String res = response.getBody().asString();
        int statusCode = response.getStatusCode();
        Assert.assertEquals("Unable to create order due to the status code: " + statusCode, 200, statusCode);
        JSONObject resBody = new JSONObject(res);
        Assert.assertEquals("success message is false", true, resBody.get("success"));
        Assert.assertEquals("statusCode is not OK", "OK", String.valueOf(resBody.get("statusCode")));
        Assert.assertEquals("Expected FulfillmentLineStatusId is not received", fulfillmentStatus, String.valueOf(resBody.getJSONObject("data").getJSONArray("FulfillmentLine").getJSONObject(0).get("FulfillmentLineStatusId")));
        log.info("Verified response successfully");
        return String.valueOf(resBody.getJSONObject("data").getJSONArray("FulfillmentLine").getJSONObject(0).get("FulfillmentLineStatusId"));
    }

}