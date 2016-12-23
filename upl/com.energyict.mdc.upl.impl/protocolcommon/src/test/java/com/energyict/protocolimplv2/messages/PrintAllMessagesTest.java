package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Copyrights EnergyICT
 * Date: 9/26/13
 * Time: 10:26 AM
 */
@RunWith(MockitoJUnitRunner.class)
public class PrintAllMessagesTest {

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Converter converter;

    @Test
    public void printAllMessagesTest() {
        for (DeviceMessageCategories deviceMessageCategory : DeviceMessageCategories.values()) {
            System.out.println("Category : " + deviceMessageCategory);
            for (DeviceMessageSpec deviceMessageSpec : deviceMessageCategory.get(this.propertySpecService, this.nlsService, this.converter).getMessageSpecifications()) {
                System.out.println("\t - DeviceMessage: " + deviceMessageSpec.getName());
                for (PropertySpec propertySpec : deviceMessageSpec.getPropertySpecs()) {
                    System.out.println("\t\t - Attribute: " + propertySpec.getName());
                }
            }
        }
    }

}
