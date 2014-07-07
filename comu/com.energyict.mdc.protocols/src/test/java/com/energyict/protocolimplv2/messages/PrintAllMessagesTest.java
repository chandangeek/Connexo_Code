package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.DataVault;
import com.energyict.mdc.common.DataVaultProvider;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 9/26/13
 * Time: 10:26 AM
 */
@RunWith(MockitoJUnitRunner.class)
public class PrintAllMessagesTest {

    @Mock
    DataVaultProvider dataVaultProvider;
    @Mock
    DataVault dataVault;

    @Before
    public void setup(){
        when(dataVaultProvider.getKeyVault()).thenReturn(dataVault);
        DataVaultProvider.instance.set(dataVaultProvider);
    }

    @Test
    public void printAllMessagesTest() {
        for (DeviceMessageCategories deviceMessageCategories : DeviceMessageCategories.values()) {
            System.out.println("Category : " + deviceMessageCategories);
            for (DeviceMessageSpec deviceMessageSpec : deviceMessageCategories.getMessageSpecifications()) {
                System.out.println("\t - DeviceMessage: " + deviceMessageSpec.getName());
                for (PropertySpec propertySpec : deviceMessageSpec.getPropertySpecs()) {
                    System.out.println("\t\t - Attribute: " + propertySpec.getName());
                }
            }
        }
    }

}
