package com.energyict.mdc.protocol.api.impl.device.messages;

import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link DeviceMessageSpecPrimaryKeyImpl} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/02/13
 * Time: 15:33
 */
public class DeviceMessageSpecPrimaryKeyTest {

    @Test
    public void correctNumberTest(){
        DeviceMessageSpecPrimaryKeyImpl deviceMessageSpecPrimaryKey = new DeviceMessageSpecPrimaryKeyImpl(ActivityCalendarDeviceMessage.ACTIVATE_PASSIVE_CALENDAR, ActivityCalendarDeviceMessage.ACTIVATE_PASSIVE_CALENDAR.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.isNumeric("12")).isTrue();
    }

    @Test
    public void incorrectNumberTest(){
        DeviceMessageSpecPrimaryKeyImpl deviceMessageSpecPrimaryKey = new DeviceMessageSpecPrimaryKeyImpl(ActivityCalendarDeviceMessage.ACTIVATE_PASSIVE_CALENDAR, ActivityCalendarDeviceMessage.ACTIVATE_PASSIVE_CALENDAR.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.isNumeric("I'mNotAnNumber :)")).isFalse();
    }

    @Test
    public void invalidNumberTest(){
        DeviceMessageSpecPrimaryKeyImpl deviceMessageSpecPrimaryKey = new DeviceMessageSpecPrimaryKeyImpl(ActivityCalendarDeviceMessage.ACTIVATE_PASSIVE_CALENDAR, ActivityCalendarDeviceMessage.ACTIVATE_PASSIVE_CALENDAR.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.isNumeric(null)).isFalse();
    }

    @Test
    public void cleanUpSimpleClassNameTest() {
        // We actually don't care about the spec at all
        DeviceMessageSpecPrimaryKeyImpl deviceMessageSpecPrimaryKey = new DeviceMessageSpecPrimaryKeyImpl(ActivityCalendarDeviceMessage.ACTIVATE_PASSIVE_CALENDAR, ActivityCalendarDeviceMessage.ACTIVATE_PASSIVE_CALENDAR.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.cleanUpClassName(AllSimple.ONE.getClass().getName())).
                isEqualTo("com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageSpecPrimaryKeyTest$AllSimple");
    }

    @Test
    public void cleanUpClassNameTest() {
        // We actually don't care about the spec at all
        DeviceMessageSpecPrimaryKeyImpl deviceMessageSpecPrimaryKey = new DeviceMessageSpecPrimaryKeyImpl(ActivityCalendarDeviceMessage.ACTIVATE_PASSIVE_CALENDAR, ActivityCalendarDeviceMessage.ACTIVATE_PASSIVE_CALENDAR.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.cleanUpClassName(WithBehavior.ONE.getClass().getName())).
                isEqualTo("com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageSpecPrimaryKeyTest$WithBehavior");
    }

    private enum AllSimple { ONE, TWO, THREE};

    private enum WithBehavior {
        ONE {
            @Override
            protected String something() {
                return "1";
            }
        },

        TWO {
            @Override
            protected String something() {
                return "2";
            }
        },

        THREE {
            @Override
            protected String something() {
                return "3";
            }
        };

        protected abstract String something();

    };

}