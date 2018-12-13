package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.DeviceGroupExtractor;
import com.energyict.mdc.upl.messages.DeviceMessageAttribute;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.DeviceExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupFinder;
import com.energyict.mdc.upl.messages.legacy.RegisterExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.propertyspec.MockPropertySpecService;
import com.energyict.protocolimplv2.messages.DeviceMessageSpecSupplier;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Abstract class grouping common functionality used to test the various {@link LegacyMessageConverter LegacyMessageConverters} .
 *
 * @author sva
 * @since 24/10/13 - 11:33
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractV2MessageConverterTest {

    protected static final Integer MESSAGE_ID = 1;
    private LegacyMessageConverter legacyMessageConverter;

    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    protected final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    protected final SimpleDateFormat dateTimeFormatWithTimeZone = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
    protected final SimpleDateFormat europeanDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    protected static PropertySpecService propertySpecService = new MockPropertySpecService();

    @Mock
    protected DeviceMessageFileFinder deviceMessageFileFinder;
    @Mock
    protected NumberLookupFinder numberLookupFinder;
    @Mock
    protected NumberLookupExtractor numberLookupExtractor;
    @Mock
    protected DeviceMessageFileExtractor deviceMessageFileExtractor;
    @Mock
    protected KeyAccessorTypeExtractor keyAccessorTypeExtractor;
    @Mock
    protected NlsService nlsService;
    @Mock
    protected Converter converter;
    @Mock
    protected LoadProfileExtractor loadProfileExtractor;
    @Mock
    protected TariffCalendarFinder calendarFinder;
    @Mock
    protected TariffCalendarExtractor calendarExtractor;
    @Mock
    protected RegisterExtractor registerExtractor;
    @Mock
    protected DeviceExtractor deviceExtractor;
    @Mock
    protected DeviceGroupExtractor deviceGroupExtractor;

    /**
     * Create a device message based on the given spec, and fill its attributes with dummy values.
     */
    protected OfflineDeviceMessage createMessage(DeviceMessageSpecSupplier supplier) {
        DeviceMessageSpec messageSpec = supplier.get(propertySpecService, nlsService, converter);
        return createMessage(messageSpec);
    }

    protected OfflineDeviceMessage createMessage(DeviceMessageSpec messageSpec) {
        OfflineDeviceMessage offlineMessage = getEmptyMessageMock();

        List<OfflineDeviceMessageAttribute> attributes = new ArrayList<>();
        for (PropertySpec propertySpec : messageSpec.getPropertySpecs()) {
            attributes.add(new TestOfflineDeviceMessageAttribute(getMessageConverter(), propertySpec, getPropertySpecValue(propertySpec), messageSpec.getId()));
        }

        doReturn(attributes).when(offlineMessage).getDeviceMessageAttributes();
        when(offlineMessage.getSpecification()).thenReturn(messageSpec);
        when(offlineMessage.getDeviceMessageId()).thenReturn(messageSpec.getId());
        return offlineMessage;
    }

    private OfflineDeviceMessage getEmptyMessageMock() {
        OfflineDeviceMessage mock = mock(OfflineDeviceMessage.class);
        when(mock.getTrackingId()).thenReturn("");
        return mock;
    }

    /**
     * Getter for the {@link Messaging} protocol which will be the purpose of the test
     */
    protected abstract Messaging getMessagingProtocol();

    /**
     * Getter for the {@link LegacyMessageConverter} which will be purpose of the test
     */
    protected LegacyMessageConverter getMessageConverter() {
        if (legacyMessageConverter == null) {
            legacyMessageConverter = doGetMessageConverter();
            legacyMessageConverter.setMessagingProtocol(this.getMessagingProtocol());
        }
        return legacyMessageConverter;
    }

    abstract LegacyMessageConverter doGetMessageConverter();

    /**
     * Gets the value to use for the given {@link PropertySpec}
     */
    protected abstract Object getPropertySpecValue(PropertySpec propertySpec);

    /**
     * An offline implementation version of an {@link DeviceMessageAttribute} used for test purposes.
     */
    public class TestOfflineDeviceMessageAttribute implements OfflineDeviceMessageAttribute {

        private String name;
        private String deviceMessageAttributeValue;
        private int deviceMessageId;

        public TestOfflineDeviceMessageAttribute(LegacyMessageConverter messageConverter, PropertySpec propertySpec, Object value, long messageId) {
            this.name = propertySpec.getName();
            this.deviceMessageAttributeValue = messageConverter.format(propertySpec, value);
            this.deviceMessageId = (int) messageId;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return deviceMessageAttributeValue;
        }

        @Override
        public long getDeviceMessageId() {
            return deviceMessageId;
        }

    }
}