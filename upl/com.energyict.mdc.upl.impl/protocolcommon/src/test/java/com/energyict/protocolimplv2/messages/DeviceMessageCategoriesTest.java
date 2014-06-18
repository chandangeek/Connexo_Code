package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.DataVaultProvider;
import com.energyict.mdw.core.RandomProvider;
import com.energyict.mdw.crypto.KeyStoreDataVaultProvider;
import com.energyict.mdw.crypto.SecureRandomProvider;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DeviceMessageCategoriesTest extends TestCase {

    @Test
    public void testMessageUniqueIds() {
        DataVaultProvider.instance.set(new KeyStoreDataVaultProvider());
        RandomProvider.instance.set(new SecureRandomProvider());


        List<Integer> messageIds;
        for (DeviceMessageCategories categories : DeviceMessageCategories.values()) {
            messageIds = new ArrayList<>();
            for (DeviceMessageSpec deviceMessageSpec : categories.getMessageSpecifications()) {
                assertFalse(messageIds.contains(deviceMessageSpec.getMessageId()));
                messageIds.add(deviceMessageSpec.getMessageId());
            }
        }
    }
}