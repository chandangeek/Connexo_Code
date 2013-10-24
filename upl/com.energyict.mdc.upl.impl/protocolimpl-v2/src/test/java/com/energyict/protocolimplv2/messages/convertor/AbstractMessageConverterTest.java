package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageAttribute;
import com.energyict.mdc.messages.DeviceMessageAttributeImpl;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.core.DataVaultProvider;
import com.energyict.mdw.core.RandomProvider;
import com.energyict.mdw.crypto.KeyStoreDataVaultProvider;
import com.energyict.mdw.crypto.SecureRandomProvider;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Abstract class grouping common functionality used to test the various {@link LegacyMessageConverter LegacyMessageConverters}.
 *
 * @author sva
 * @since 24/10/13 - 11:33
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractMessageConverterTest {

    @Before
    public void mockMessages() {
        mockProviders();
    }

    private void mockProviders() {
        DataVaultProvider.instance.set(new KeyStoreDataVaultProvider());
        RandomProvider.instance.set(new SecureRandomProvider());
    }

    /**
     * Create a device message based on the given spec, and fill its attributes with dummy values.
     */
    protected OfflineDeviceMessage createMessage(DeviceMessageSpec messageSpec) {
        OfflineDeviceMessage message = getEmptyMessageMock();
        List<OfflineDeviceMessageAttribute> attributes = new ArrayList<>();

        for (PropertySpec propertySpec : messageSpec.getPropertySpecs()) {
            TypedProperties propertyStorage = TypedProperties.empty();
            propertyStorage.setProperty(propertySpec.getName(), getPropertySpecValue(propertySpec));
            attributes.add(new TestOfflineDeviceMessageAttribute(new DeviceMessageAttributeImpl(propertySpec, null, propertyStorage), getMessageConverter()));
        }
        when(message.getDeviceMessageAttributes()).thenReturn(attributes);
        when(message.getSpecification()).thenReturn(messageSpec);
        return message;
    }

    private OfflineDeviceMessage getEmptyMessageMock() {
        OfflineDeviceMessage mock = mock(OfflineDeviceMessage.class);
        when(mock.getTrackingId()).thenReturn("");
        return mock;
    }

    /**
     * getter for the {@link LegacyMessageConverter} which will be purpose of the test
     */
    abstract protected LegacyMessageConverter getMessageConverter();

    /**
     * Gets the value to use for the given {@link PropertySpec}
     */
    abstract protected Object getPropertySpecValue(PropertySpec propertySpec);

    /**
     * An offline implementation version of an {@link com.energyict.mdc.messages.DeviceMessageAttribute} used for test purposes.
     */
    private class TestOfflineDeviceMessageAttribute implements OfflineDeviceMessageAttribute {

        private final DeviceMessageAttribute deviceMessageAttribute;
        private final LegacyMessageConverter messageConverter;
        private PropertySpec propertySpec;
        private String name;
        private String deviceMessageAttributeValue;
        private DeviceMessage deviceMessage;

        public TestOfflineDeviceMessageAttribute(DeviceMessageAttribute deviceMessageAttribute, LegacyMessageConverter messageConverter) {
            this.deviceMessageAttribute = deviceMessageAttribute;
            this.messageConverter = messageConverter;
            goOffline();
        }

        private void goOffline() {
            this.propertySpec = this.deviceMessageAttribute.getSpecification();
            this.name = this.deviceMessageAttribute.getName();
            this.deviceMessageAttributeValue = messageConverter.format(propertySpec, this.deviceMessageAttribute.getValue());
            this.deviceMessage = this.deviceMessageAttribute.getMessage();
        }

        @Override
        public PropertySpec getPropertySpec() {
            return propertySpec;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDeviceMessageAttributeValue() {
            return deviceMessageAttributeValue;
        }

        @Override
        public DeviceMessage getDeviceMessage() {
            return deviceMessage;
        }
    }
}
