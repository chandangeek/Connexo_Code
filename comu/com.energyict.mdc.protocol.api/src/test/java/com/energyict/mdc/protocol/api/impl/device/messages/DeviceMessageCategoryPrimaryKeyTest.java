package com.energyict.mdc.protocol.api.impl.device.messages;

import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link DeviceMessageCategoryPrimaryKeyImpl} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/02/13
 * Time: 8:49
 */
public class DeviceMessageCategoryPrimaryKeyTest {

    @Test
    public void correctNumberTest(){
        DeviceMessageCategoryPrimaryKeyImpl deviceMessageCategoryPrimaryKey = new DeviceMessageCategoryPrimaryKeyImpl(DeviceMessageCategories.SECURITY, DeviceMessageCategories.SECURITY.name());

        // asserts
        assertThat(deviceMessageCategoryPrimaryKey.isNumeric("12")).isTrue();
    }

    @Test
    public void incorrectNumberTest(){
        DeviceMessageCategoryPrimaryKeyImpl deviceMessageCategoryPrimaryKey = new DeviceMessageCategoryPrimaryKeyImpl(DeviceMessageCategories.SECURITY, DeviceMessageCategories.SECURITY.name());

        // asserts
        assertThat(deviceMessageCategoryPrimaryKey.isNumeric("I'mNotAnNumber :)")).isFalse();
    }

    @Test
    public void invalidNumberTest(){
        DeviceMessageCategoryPrimaryKeyImpl deviceMessageCategoryPrimaryKey = new DeviceMessageCategoryPrimaryKeyImpl(DeviceMessageCategories.SECURITY, DeviceMessageCategories.SECURITY.name());

        // asserts
        assertThat(deviceMessageCategoryPrimaryKey.isNumeric(null)).isFalse();
    }

    @Test
    public void cleanUpClassNameTest() {
        DeviceMessageCategoryPrimaryKeyImpl deviceMessageCategoryPrimaryKey = new DeviceMessageCategoryPrimaryKeyImpl(DeviceMessageCategories.SECURITY, DeviceMessageCategories.SECURITY.name());

        // asserts
        assertThat(deviceMessageCategoryPrimaryKey.cleanUpClassName(DeviceMessageCategories.SECURITY.getClass().getName())).
                isEqualTo("com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageCategories");
    }

}
