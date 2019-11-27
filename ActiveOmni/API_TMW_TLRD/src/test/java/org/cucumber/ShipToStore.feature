Feature: Create TMW ship to store order and verify details.


  @sanity @regression
  Scenario: Verify the TMW ship to store order and verify details with fullfillmentID.

    When I hit the "AuthUrl" with the "getallsku_scenario2" parameter and input from "menswearhouseSKU.json" file
    When I hit TMW Login API with the "AuthUrl" with the "getallsku_scenario3" parameter and input from "menswearhouseSKU.json" file
    When I hit the "Endpoint" with the "getallsku_scenario4" from "menswearhouseSKU.json" file to create shipto store order
    Then Validate the fullfillment Details
      | Path     | Key  |
      | data.FulfillmentId    | STSOrderNumber  |
      | data.MinStatusId   | openstatus  |
      | data.CustomerFirstName   | CustomerFirstName  |
    When Get the Order Detail for StoreNo "095206247" and Id "TMW"
    Then Validate the fullfillment Details
      | Path     | Key  |
      | data.FulfillmentId    | STSOrderNumber  |
      | data.MinStatusId   | openstatus  |
      | data.CustomerFirstName   | CustomerFirstName  |
