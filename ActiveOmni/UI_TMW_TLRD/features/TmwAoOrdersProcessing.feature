Feature: TLRD Omni Enterprise test feature

  @smoke @regression @project_omni_ropis
  Scenario Outline: Verify user is able to process the ROPIS order
    Given I visit the Omni Enterprise website as a registered user
    And I navigated to store home page by creating an order with below params:
      | order_type  | address_type | shipMethod_type  | line_type  |
      | <orderType> | <addType>    | <shipMethodType> | <lineType> |
    Then I should be navigated to the store home page
    When I navigate to "Prepare Pickups" page
    And I search with the DO number
    And I complete the process of "ROPIS" from pick list page
    Then I should be navigated to the store home page
    When I navigate to "Confirm Pickups" page
    Then I search with the fulfillment id to verify the status as "Picked"
    And I select the DO for completing the confirm pick up
    Then I search with the fulfillment id to verify the status as "Cancelled"
    Examples:
      | orderType | addType               | shipMethodType | lineType |
      | ROPIS     | forty_eight_addresses | Standard       | single   |
      | ROPIS     | alaska_addresses      | Standard       | multiple |
      | ROPIS     | puerto_addresses      | Standard       | single   |
      | ROPIS     | us_territories        | Standard       | multiple |

  @smoke @regression @project_omni_selfie
  Scenario Outline: Verify user is able to process the Selfie order
    Given I visit the Omni Enterprise website as a registered user
    And I navigated to store home page by creating an order with below params:
      | order_type  | address_type | shipMethod_type  | line_type  | gift_message |
      | <orderType> | <addType>    | <shipMethodType> | <lineType> | <giftMsg>    |
    Then I should be navigated to the store home page
    When I navigate to "Prepare Pickups" page
    And I search with the DO number
    When I complete the process of "Selfie" from pick list page
    Then I should be navigated to the store home page
    When I navigate to "Confirm Pickups" page
    Then I search with the fulfillment id to verify the status as "Picked Up"
    Examples:
      | orderType | addType               | shipMethodType | lineType | giftMsg |
      | Selfie    | forty_eight_addresses | Standard       | single   | true    |
      | Selfie    | alaska_addresses      | Standard       | multiple | false   |
      | Selfie    | puerto_addresses      | Standard       | single   | true    |
      | Selfie    | us_territories        | Standard       | multiple | false   |

  @smoke @regression @project_omni_sta
  Scenario Outline: Verify user is able to process the STA order
    Given I visit the Omni Enterprise website as a registered user
    And I navigated to store home page by creating an order with below params:
      | order_type  | address_type | shipMethod_type  | line_type  |
      | <orderType> | <addType>    | <shipMethodType> | <lineType> |
    Then I should be navigated to the store home page
    When I navigate to "Prepare Shipments" page
    And I search with the DO number
    When I complete the process of "STA" from pick list page
    Then I should be navigated to the store home page
    When I navigate to "Pack Shipments" page
    And I search with tracking number
    And I complete the order packing
    Examples:
      | orderType | addType               | shipMethodType | lineType |
      | STA       | forty_eight_addresses | Standard       | single   |
      | STA       | alaska_addresses      | Standard       | multiple |
      | STA       | puerto_addresses      | Standard       | single   |
      | STA       | us_territories        | Standard       | multiple |

  @smoke @regression @create_order_usingAPI
  Scenario: Verify user is able to create order using API
    Given I create an order with below params:
      | orderType | addType               | shipMethodType | lineType | giftMsg |
      | Selfie    | forty_eight_addresses | Standard       | single   | true    |
