package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.exceptions.DeviceProtocolPropertyException;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.google.common.base.Strings;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import org.junit.rules.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.device.data.impl.DeviceProtocolPropertyImpl} component
 * <p>
 * Copyrights EnergyICT
 * Date: 24/03/14
 * Time: 09:58
 */
public class DeviceProtocolPropertyImplTest extends PersistenceTestWithMockedDeviceProtocol {

    private static final String MRID = "MyUniqueMRID";

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private void setupStringPropertyWithName(String name) {
        PropertySpec stringPropertySpec = new PropertySpecServiceImpl().basicPropertySpec(name, false, new StringFactory());
        List<PropertySpec> propertySpecs = deviceProtocol.getPropertySpecs();
        if (propertySpecs == null) {
            propertySpecs = new ArrayList<>();
        }
        propertySpecs.add(stringPropertySpec);
        when(deviceProtocol.getPropertySpecs()).thenReturn(propertySpecs);
    }

    private Device createSimpleDeviceWithProperty(String name, String value) {
        setupStringPropertyWithName(name);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithProperties", MRID);
        device.setProtocolProperty(name, value);
        device.save();
        return device;
    }

    @Test
    @Transactional
    public void successfulCreateTest() {
        String name = "MyProperty";
        String value = "MyValueOfTheProperty";
        Device device = createSimpleDeviceWithProperty(name, value);

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getDeviceProtocolProperties().getProperty(name)).isEqualTo(value);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED + "}")
    public void createWithEmptyPropertyValue() {
        String name = "MyProperty";
        String value = "";
        Device device = createSimpleDeviceWithProperty(name, value);
    }

    @Test
    @Transactional
    public void updateDevicePropertyTest() {
        String name = "MyProperty";
        String value = "MyValueOfTheProperty";
        String updatedValue = "TheUpdatedValue";
        Device device = createSimpleDeviceWithProperty(name, value);

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.setProtocolProperty(name, updatedValue);

        Device deviceWithUpdatedProperties = getReloadedDevice(reloadedDevice);

        assertThat(deviceWithUpdatedProperties.getDeviceProtocolProperties().getProperty(name)).isEqualTo(updatedValue);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED + "}")
    public void updateWithNullValueTest() {
        String name = "MyProperty";
        String value = "MyValueOfTheProperty";
        String updatedValue = null;
        Device device = createSimpleDeviceWithProperty(name, value);

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.setProtocolProperty(name, updatedValue);
    }

    @Test
    @Transactional
    public void removePropertyTest() {
        String propertyName1 = "MyProperty1";
        String value1 = "MyValueOfTheProperty1";
        String propertyName2 = "MyProperty2";
        String value2 = "MyValueOfTheProperty2";
        String propertyName3 = "MyProperty3";
        String value3 = "MyValueOfTheProperty3";
        setupStringPropertyWithName(propertyName1);
        setupStringPropertyWithName(propertyName2);
        setupStringPropertyWithName(propertyName3);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithProperties", MRID);
        device.setProtocolProperty(propertyName1, value1);
        device.setProtocolProperty(propertyName2, value2);
        device.setProtocolProperty(propertyName3, value3);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.removeProtocolProperty(propertyName2);

        Device deviceWithoutProperty2 = getReloadedDevice(reloadedDevice);
        assertThat(deviceWithoutProperty2.getDeviceProtocolProperties().getProperty(propertyName2)).isNull();
    }

    @Test
    @Transactional
    public void removeUnknownPropertyTest() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithProperties", MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.removeProtocolProperty("UnknownProperty");

        Device updatedDevice = getReloadedDevice(reloadedDevice);
        assertThat(updatedDevice.getDeviceProtocolProperties().localSize()).isZero();
    }

    @Test
    @Transactional
    public void addAPropertyThatAlreadyExistsTest() {
        String name = "MyProperty";
        String value = "MyValueOfTheProperty";
        String updateValue = "ValueFromTheSecondAdd";
        Device device = createSimpleDeviceWithProperty(name, value);

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.setProtocolProperty(name, updateValue);
        reloadedDevice.save();

        Device updatedDevice = getReloadedDevice(reloadedDevice);
        assertThat(updatedDevice.getDeviceProtocolProperties().getProperty(name)).isEqualTo(updateValue);
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    public void addPropertyThatIsTooBig() {
        String name = "MyProperty";
        String value = Strings.repeat("AB-Normal", Table.SHORT_DESCRIPTION_LENGTH);

        // Business method
        createSimpleDeviceWithProperty(name, value);

        // Asserts: see expected violation rule
    }

    @Test
    @Transactional
    public void updateAPropertyThatDoesntExistYetTest() {
        String name = "MyProperty";
        String value = "MyValueOfTheProperty";
        setupStringPropertyWithName(name);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithProperties", MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.setProtocolProperty(name, value);
        reloadedDevice.save();

        Device updatedDevice = getReloadedDevice(reloadedDevice);
        assertThat(updatedDevice.getDeviceProtocolProperties().getProperty(name)).isEqualTo(value);
    }

    @Test(expected = DeviceProtocolPropertyException.class)
    @Transactional
    public void addPropertyThatDoesntExistOnDeviceProtocolTest() {
        String name = "MyProperty";
        String value = "MyValueOfTheProperty";
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithProperties", MRID);
        device.setProtocolProperty(name, value);
        try {
            device.save();
        } catch (DeviceProtocolPropertyException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.DEVICE_PROPERTY_NOT_ON_DEVICE_PROTOCOL)) {
                fail("Should have gotten exception indicating that you tried to add a property that is not defined by the device protocol, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test
    @Transactional
    public void removePropertiesWhenDeleteOfDeviceTest() {
        String name = "MyProperty";
        String value = "MyValueOfTheProperty";
        Device device = createSimpleDeviceWithProperty(name, value);

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.delete();

        assertThat(inMemoryPersistence.getDataModel().mapper(DeviceProtocolProperty.class).find()).isEmpty();
    }
}
