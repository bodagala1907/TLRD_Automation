Feature: TLRD Omni Enterprise test feature

  @smoke @regression @project_omni_selfie
  Scenario Outline: Verify user is able to process the Selfie order
    Given I visit the Omni Enterprise website as a registered user
    And I navigated to store home page by creating an order with below params:
      | order_type  | address_type | shipMethod_type  | line_type  |
      | <orderType> | <addType>    | <shipMethodType> | <lineType> |
    Then I should be navigated to the store home page
    When I navigate to "Prepare Pickups" page
    And I search with the DO number
    When I complete the process of "Selfie" from pick list page
    Then I should be navigated to the store home page
    When I navigate to "Confirm Pickups" page
    Then I search with the fulfillment id to verify the status as "Picked Up"
    Examples:
      | orderType | addType               | shipMethodType | lineType |
      | Selfie    | forty_eight_addresses | Standard       | single   |
      | Selfie    | alaska_addresses      | Standard       | multiple |
      | Selfie    | puerto_addresses      | Standard       | single   |
      | Selfie    | us_territories        | Standard       | multiple |
