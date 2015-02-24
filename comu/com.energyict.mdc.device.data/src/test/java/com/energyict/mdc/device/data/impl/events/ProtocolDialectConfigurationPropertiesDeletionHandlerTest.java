package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.ServerDeviceService;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ProtocolDialectConfigurationPropertiesDeletionHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-19 (15:55)
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtocolDialectConfigurationPropertiesDeletionHandlerTest {

    @Mock
    private DeviceDataModelService deviceDataModelService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ServerDeviceService deviceService;
    @Mock
    private LocalEvent event;
    @Mock
    private ProtocolDialectConfigurationProperties properties;

    @Before
    public void initializeMocks() {
        when(this.deviceDataModelService.thesaurus()).thenReturn(this.thesaurus);
        when(this.deviceDataModelService.deviceService()).thenReturn(this.deviceService);
        when(this.event.getSource()).thenReturn(this.properties);
    }

    @Test
    public void handlerTestUsage() {
        ProtocolDialectConfigurationPropertiesDeletionHandler handler = this.newTestHandler();

        // Business method
        handler.handle(this.event);

        // Asserts
        verify(this.deviceService).hasDevices(this.properties);
    }

    @Test(expected = VetoDeleteProtocolDialectConfigurationPropertiesException.class)
    public void handlerVetosWhenInUse() {
        ProtocolDialectConfigurationPropertiesDeletionHandler handler = this.newTestHandler();
        when(this.deviceService.hasDevices(this.properties)).thenReturn(true);

        // Business method
        handler.handle(this.event);

        // Asserts: see expected exception rule
    }

    @Test
    public void handlerDoesNotVetoWhenNotInUse() {
        ProtocolDialectConfigurationPropertiesDeletionHandler handler = this.newTestHandler();
        when(this.deviceService.hasDevices(this.properties)).thenReturn(false);

        // Business method
        handler.handle(this.event);

        // Asserts: enough to assert that there are not exceptions
    }

    private ProtocolDialectConfigurationPropertiesDeletionHandler newTestHandler() {
        return new ProtocolDialectConfigurationPropertiesDeletionHandler(this.deviceDataModelService);
    }

}