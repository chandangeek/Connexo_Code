package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperty;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ProtocolDialectConfigurationPropertyValueDeletionHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-19 (15:55)
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtocolDialectConfigurationPropertyValueDeletionHandlerTest {
    private static final String PROPERTY_NAME = "ForTestingPurposesOnly";

    @Mock
    private DeviceDataModelService deviceDataModelService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ServerDeviceService deviceService;
    @Mock
    private LocalEvent event;
    @Mock
    private ProtocolDialectConfigurationProperty property;
    @Mock
    private ProtocolDialectConfigurationProperties configurationProperties;
    @Mock
    private PropertySpec propertySpec;

    @Before
    public void initializeMocks() {
        when(this.deviceDataModelService.thesaurus()).thenReturn(this.thesaurus);
        when(this.deviceDataModelService.deviceService()).thenReturn(this.deviceService);
        when(this.event.getSource()).thenReturn(this.property);
        when(this.property.getName()).thenReturn(PROPERTY_NAME);
        when(this.property.getProtocolDialectConfigurationProperties()).thenReturn(this.configurationProperties);
        DeviceProtocolDialect deviceProtocolDialect = mock(DeviceProtocolDialect.class);
        when(deviceProtocolDialect.getPropertySpec(PROPERTY_NAME)).thenReturn(this.propertySpec);
        when(this.configurationProperties.getDeviceProtocolDialect()).thenReturn(deviceProtocolDialect);
    }

    @Test
    public void handlerTestsThatPropertySpecIsRequired() {
        ProtocolDialectConfigurationPropertyValueDeletionHandler handler = this.newTestHandler();

        // Business method
        handler.handle(this.event);

        // Asserts
        verify(this.propertySpec).isRequired();
    }

    @Test(expected = VetoDeleteProtocolDialectConfigurationPropertyException.class)
    public void handlerVetosWhenInUse() {
        ProtocolDialectConfigurationPropertyValueDeletionHandler handler = this.newTestHandler();
        when(this.propertySpec.isRequired()).thenReturn(true);
        when(this.deviceService.hasDevices(this.configurationProperties, this.propertySpec)).thenReturn(true);

        // Business method
        handler.handle(this.event);

        // Asserts: see expected exception rule
    }

    @Test
    public void handlerDoesNotVetoWhenNotInUse() {
        ProtocolDialectConfigurationPropertyValueDeletionHandler handler = this.newTestHandler();
        when(this.deviceService.hasDevices(this.configurationProperties, this.propertySpec)).thenReturn(false);

        // Business method
        handler.handle(this.event);

        // Asserts: sufficient to find that there were not exceptions
    }

    @Test
    public void handlerDoesNotVetoWhenPropertyDoesNotExist() {
        ProtocolDialectConfigurationPropertyValueDeletionHandler handler = this.newTestHandler();
        DeviceProtocolDialect deviceProtocolDialect = mock(DeviceProtocolDialect.class);
        when(deviceProtocolDialect.getPropertySpec(PROPERTY_NAME)).thenReturn(null);
        when(this.configurationProperties.getDeviceProtocolDialect()).thenReturn(deviceProtocolDialect);

        // Business method
        handler.handle(this.event);

        // Asserts: sufficient to find that there were not exceptions
    }

    private ProtocolDialectConfigurationPropertyValueDeletionHandler newTestHandler() {
        ProtocolDialectConfigurationPropertyValueDeletionHandler handler = new ProtocolDialectConfigurationPropertyValueDeletionHandler();
        handler.setDeviceDataModelService(this.deviceDataModelService);
        return handler;
    }

}