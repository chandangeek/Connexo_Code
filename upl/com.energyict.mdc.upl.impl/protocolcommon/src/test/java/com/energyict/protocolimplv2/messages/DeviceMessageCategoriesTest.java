package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageCategoriesTest extends TestCase {

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Converter converter;

    @Test
    public void testMessageUniqueIds() {
        List<Long> messageIds;
        for (DeviceMessageCategories categories : DeviceMessageCategories.values()) {
            messageIds = new ArrayList<>();
            for (DeviceMessageSpec deviceMessageSpec : categories.get(this.propertySpecService, this.nlsService, this.converter).getMessageSpecifications()) {
                boolean condition = messageIds.contains(deviceMessageSpec.getId());
                if (condition) {
                    Logger logger = Logger.getLogger(DeviceMessageCategoriesTest.class.getSimpleName());
                    logger.severe("Unique message ID violation: " + deviceMessageSpec.getName());
                }
                assertFalse(condition);
                messageIds.add(deviceMessageSpec.getId());
            }
        }
    }

}