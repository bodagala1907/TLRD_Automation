Feature: TBI ActiveOmni Ship To Address test feature

  @smoke @regression @project_omni_sta
  Scenario Outline: Verify user is able to process the STA order
    Given I visit the Omni Enterprise website as a registered user
    And I navigated to store home page by creating an order with below params:
      | order_type  | address_type | shipMethod_type  | line_type  | gift_message |
      | <orderType> | <addType>    | <shipMethodType> | <lineType> | <giftMsg>    |
    Then I should be navigated to the store home page
    When I navigate to "Prepare Shipments" page
    And I search with the DO number
    When I complete the process of "STA" from pick list page
    Then I should be navigated to the store home page
    When I navigate to "Pack Shipments" page
    And I search with tracking number
    And I complete the order packing
    Examples:
      | orderType | addType               | shipMethodType | lineType | giftMsg |
      | STA       | forty_eight_addresses | Standard       | single   | true    |
      | STA       | forty_eight_addresses | Standard       | multiple | false   |
      | STA       | APO                   | APO            | multiple | false   |
      | STA       | us_territories        | us_territories | single   | false   |
      | STA       | forty_eight_addresses | Express        | single   | false   |
      | STA       | forty_eight_addresses | Rush           | multiple | false   |