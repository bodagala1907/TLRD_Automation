Feature: TLRD Omni Enterprise test feature

  @smoke @regression @project_omni_ropis
  Scenario Outline: Verify user is able to process the ROPIS order
    Given I visit the Omni Enterprise website as a registered user
    And I navigated to store home page by creating an order with below params:
      | order_type  | address_type | shipMethod_type  | line_type  | gift_message |
      | <orderType> | <addType>    | <shipMethodType> | <lineType> | <giftMsg>    |
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
      | orderType | addType               | shipMethodType | lineType | giftMsg |
#      | ROPIS     | forty_eight_addresses | Standard       | single   | false   |
      | ROPIS     | apo_addresses         | APO            | multiple | false   |
      | ROPIS     | forty_eight_addresses | Express        | single   | false   |
      | ROPIS     | us_territories        | us_territories | multiple | false   |