package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.mdw.core.DataVaultProvider;
import com.energyict.mdw.core.RandomProvider;
import com.energyict.mdw.crypto.KeyStoreDataVaultProvider;
import com.energyict.mdw.crypto.SecureRandomProvider;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageCategoriesTest extends TestCase {

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private NlsService nlsService;

    @Test
    public void testMessageUniqueIds() {
        DataVaultProvider.instance.set(new KeyStoreDataVaultProvider());
        RandomProvider.instance.set(new SecureRandomProvider());

        List<Long> messageIds;
        for (DeviceMessageCategories categories : DeviceMessageCategories.values()) {
            messageIds = new ArrayList<>();
            for (DeviceMessageSpec deviceMessageSpec : categories.get(propertySpecService, nlsService).getMessageSpecifications()) {
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