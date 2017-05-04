package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.propertyspec.MockPropertySpecService;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/04/2017 - 13:20
 */
public class TestAllDeviceMessageIds {

    /**
     * Verify that every {@link DeviceMessageSpec} (defined in protocols 9.1) also has a matching entry in the {@link DeviceMessageId} enumeration
     */
    @Test
    public void testAllDeviceMessageIds() {
        for (DeviceMessageCategorySupplier deviceMessageCategorySupplier : DeviceMessageCategories.values()) {
            DeviceMessageCategory deviceMessageCategory = deviceMessageCategorySupplier.get(new MockPropertySpecService(), mock(NlsService.class), mock(Converter.class));

            for (DeviceMessageSpec deviceMessageSpec : deviceMessageCategory.getMessageSpecifications()) {
                assertTrue("ID '" + deviceMessageSpec.getId() + "' for device message spec '" + deviceMessageSpec.getNameTranslationKey().getKey() + "' is not defined in com.energyict.mdc.protocol.api.messaging.DeviceMessageId", Stream.of(DeviceMessageId.values()).filter(id -> id.dbValue() == deviceMessageSpec.getId()).findAny().isPresent());
            }
        }
    }
}