package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.exceptions.VetoDeviceProtocolPluggableClassDeletionBecauseStillUsedByDeviceTypesException;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.event.EventConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceProtocolPluggableClassDeletionMessageHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-19 (13:14)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceProtocolPluggableClassDeletionMessageHandlerTest {

    @Mock
    private JsonService jsonService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceType deviceType;

    @Before
    public void initializeMocks () {
        when(this.nlsService.getThesaurus(DeviceConfigurationService.COMPONENTNAME, Layer.DOMAIN)).thenReturn(this.thesaurus);
        when(this.deviceProtocolPluggableClass.getName()).thenReturn(DeviceProtocolPluggableClassDeletionMessageHandlerTest.class.getSimpleName());
        when(this.deviceType.getName()).thenReturn(DeviceProtocolPluggableClassDeletionMessageHandlerTest.class.getSimpleName());
    }

    @Test
    public void testDeleteEventForDeviceProtocolPluggableClassThatIsNotUsed () {
        Long deviceProtocolPluggableClassId = new Long(666);
        when(this.protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)).thenReturn(this.deviceProtocolPluggableClass);

        Message message = this.mockDeleteMessage(deviceProtocolPluggableClassId);

        // Business method
        this.newTestHandler().process(message);

        // Asserts
        verify(message).getPayload();
        verify(this.protocolPluggableService).findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId);
        verify(this.deviceConfigurationService).findDeviceTypesWithDeviceProtocol(this.deviceProtocolPluggableClass);
        // Should not throw VetoDeviceProtocolPluggableClassDeletionBecauseStillUsedByDeviceTypesException
    }

    @Test(expected = VetoDeviceProtocolPluggableClassDeletionBecauseStillUsedByDeviceTypesException.class)
    public void testDeleteEventForDeviceProtocolPluggableClassThatIsStillInUse () {
        Long deviceProtocolPluggableClassId = new Long(666);
        when(this.protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)).thenReturn(this.deviceProtocolPluggableClass);
        List<DeviceType> deviceTypes = Arrays.asList(this.deviceType);
        when(this.deviceConfigurationService.findDeviceTypesWithDeviceProtocol(this.deviceProtocolPluggableClass)).thenReturn(deviceTypes);

        Message message = this.mockDeleteMessage(deviceProtocolPluggableClassId);

        // Business method
        this.newTestHandler().process(message);

        // Asserts: expected VetoDeviceProtocolPluggableClassDeletionBecauseStillUsedByDeviceTypesException
    }

    @Test
    public void testDeleteEventForDeviceProtocolPluggableClassThatDoesNotExist () {
        Message message = this.mockDeleteMessage(new Long(666));

        // Business method
        this.newTestHandler().process(message);

        // Asserts
        verify(message).getPayload();
        // Should not throw any nasty exceptions (like e.g. NullPointerException because the device protocol pluggable class does not exist)
    }

    private Message mockDeleteMessage(Long deviceProtocolPluggableClassId) {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put(EventConstants.TIMESTAMP, System.currentTimeMillis());
        messageProperties.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/protocol/pluggable/deviceprotocol/DELETED");
        messageProperties.put("id", deviceProtocolPluggableClassId);
        when(this.jsonService.deserialize(anyString(), eq(Map.class))).thenReturn(messageProperties);
        return mock(Message.class);
    }

    private DeviceProtocolPluggableClassDeletionMessageHandler newTestHandler () {
        return new DeviceProtocolPluggableClassDeletionMessageHandler(this.deviceConfigurationService, this.protocolPluggableService, this.nlsService, this.jsonService);
    }

}