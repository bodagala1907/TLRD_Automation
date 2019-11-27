Feature: Create TMW ship to store order and verify details.


  @sanity @regression
  Scenario: Verify the TMW ship to store order and verify details with fullfillmentID.

  Scenario: Verify the TMW ship to Address order and verify details with FullfillmentID
    When I hit the "AuthUrl" with the "getallsku_scenario2" parameter and input from "menswearhouseSKU.json" file
    When I hit TMW Login API with the "AuthUrl" with the "getallsku_scenario3" parameter and input from "menswearhouseSKU.json" file
    When I hit the "Endpoint" with the "getallsku_scenario4" from "menswearhouseSKU.json" file to create shipto address order
    Then Validate the fullfillment Details for ShitToAddress
      | Path     | Key  |
      | data.FulfillmentId    | STAOrderNumber  |
      | data.FulfillmentLine[0].FulfillmentLineStatusId   | openstatus  |
      | data.CustomerFirstName   | STACustomerFirstName  |
    When Get the Order Detail for StoreNo "095206247" and Id "TMW" for ShipToAddress
    Then Validate the fullfillment Details for ShitToAddress
      | Path     | Key  |
      | data.FulfillmentId    | STAOrderNumber  |
      | data.FulfillmentLine[0].FulfillmentLineStatusId  | openstatus  |
      | data.CustomerFirstName   | STACustomerFirstName  |

