package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceCommandTest {

    @Mock
    private EndDevice endDevice;

    @Mock
    private DeviceMessageSpecificationService deviceMessageSpecificationService;

    @Mock
    private ValueFactory valueFactory;


    @Mock
    private TimeService timeService;

    @Mock
    private OrmService ormService;

    @Mock
    private BeanService beanService;

    private PropertySpecService propertySpecService;


    private Map<String, Object> attributes;
    private List<DeviceMessageId> deviceMessageIds = new ArrayList<>(
            Arrays.asList(
                    DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE
            ));
    private EndDeviceControlType endDeviceControlType;
    private String commandName = "TestCommand";
    private EndDeviceCommand endDeviceCommand;

    @Before
    public void setup() {
        attributes = new HashMap<>();
        this.propertySpecService = new PropertySpecServiceImpl(this.timeService, this.ormService, this.beanService);
        DeviceMessageSpec message = mock(DeviceMessageSpec.class);
        when(deviceMessageSpecificationService.findMessageSpecById(anyLong())).thenReturn(Optional.of(message));
        PropertySpec string1 = this.propertySpecService
                .stringSpec()
                .named("string1", "One")
                .describedAs("Description for string1")
                .setDefaultValue("Value1")
                .finish();
        PropertySpec string2 = this.propertySpecService
                .stringSpec()
                .named("string2", "Two")
                .describedAs("Description for string2")
                .setDefaultValue("Value2")
                .finish();
        when(message.getPropertySpecs()).thenReturn(Arrays.asList(string1, string2));
        endDeviceCommand = new EndDeviceCommandImpl(commandName, endDevice, endDeviceControlType, deviceMessageIds, propertySpecService, deviceMessageSpecificationService)
                .init();

    }

    @Test

    public void getCommandArgumentSpecs() {

        List<PropertySpec> commandArgsSpecs = endDeviceCommand.getCommandArgumentSpecs();
        assertTrue(commandArgsSpecs.size() == 2);
        assertTrue(commandArgsSpecs.get(0).getName().equals("string1"));
        assertTrue(commandArgsSpecs.get(0).getDisplayName().equals("One"));
        assertTrue(commandArgsSpecs.get(0).getDescription().equals("Description for string1"));
        assertTrue(commandArgsSpecs.get(1).getName().equals("string2"));
        assertTrue(commandArgsSpecs.get(1).getDisplayName().equals("Two"));
        assertTrue(commandArgsSpecs.get(1).getDescription().equals("Description for string2"));
    }

    @Test
    public void setPropertyValue() {
        assertTrue(attributes.isEmpty());
        List<PropertySpec> commandArgsSpecs = endDeviceCommand.getCommandArgumentSpecs();
        PropertySpec testPropertySpec = commandArgsSpecs.get(0);
        endDeviceCommand.setPropertyValue(testPropertySpec, "testString");
        endDeviceCommand.getCommandArgumentSpecs().stream().filter(spec ->
                spec.equals(testPropertySpec)).findFirst().ifPresent(found ->
                assertTrue(found.getPossibleValues().getDefault().equals("testString")));

    }

}
