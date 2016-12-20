package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.ManagerImpl;
import com.energyict.mdc.interfaces.mdw.Mdw2MdcInterfaceImpl;
import com.energyict.mdc.messages.DeviceMessageAttribute;
import com.energyict.mdc.messages.DeviceMessageAttributeImpl;
import com.energyict.mdc.messages.DeviceMessageSpecFactoryImpl;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdw.core.DataVaultProvider;
import com.energyict.mdw.core.RandomProvider;
import com.energyict.mdw.crypto.KeyStoreDataVaultProvider;
import com.energyict.mdw.crypto.SecureRandomProvider;
import com.energyict.mdw.interfacing.mdc.DefaultMdcInterfaceProvider;
import com.energyict.mdw.interfacing.mdc.MdcInterfaceProvider;
import com.energyict.protocol.messaging.Messaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Abstract class grouping common functionality used to test the various {@link LegacyMessageConverter LegacyMessageConverters} .
 *
 * @author sva
 * @since 24/10/13 - 11:33
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractMessageConverterTest {

    protected static final Integer MESSAGE_ID = 1;

    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    protected final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    protected final SimpleDateFormat dateTimeFormatWithTimeZone = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
    protected final SimpleDateFormat europeanDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


    private DeviceMessageSpecFactoryImpl deviceMessageSpecFactory;
    private LegacyMessageConverter legacyMessageConverter;

    @Before
    public void mockMessages() {
        mockProviders();
    }

    private void mockProviders() {
        DefaultMdcInterfaceProvider provider = spy(new DefaultMdcInterfaceProvider());
        Mdw2MdcInterfaceImpl mdcInterface = spy(new Mdw2MdcInterfaceImpl());
        ManagerImpl manager = spy(new ManagerImpl());
        deviceMessageSpecFactory = spy(new DeviceMessageSpecFactoryImpl());
        when(manager.getDeviceMessageSpecFactory()).thenReturn(deviceMessageSpecFactory);
        when(mdcInterface.getManager()).thenReturn(manager);
        when(provider.getMdcInterface()).thenReturn(mdcInterface);
        MdcInterfaceProvider.instance.set(provider);

        DataVaultProvider.instance.set(new KeyStoreDataVaultProvider());
        RandomProvider.instance.set(new SecureRandomProvider());
    }

    /**
     * Create a device message based on the given spec, and fill its attributes with dummy values.
     */
    protected OfflineDeviceMessage createMessage(DeviceMessageSpec messageSpec) {
        when(deviceMessageSpecFactory.fromPrimaryKey(messageSpec.getPrimaryKey().getValue())).thenReturn(messageSpec);
        DeviceMessage message = mock(DeviceMessage.class);
        when(message.getId()).thenReturn(MESSAGE_ID);
        OfflineDeviceMessage offlineMessage = getEmptyMessageMock();
        List<OfflineDeviceMessageAttribute> attributes = new ArrayList<>();

        for (PropertySpec propertySpec : messageSpec.getPropertySpecs()) {
            TypedProperties propertyStorage = TypedProperties.empty();
            propertyStorage.setProperty(propertySpec.getName(), getPropertySpecValue(propertySpec));
            attributes.add(new TestOfflineDeviceMessageAttribute(new DeviceMessageAttributeImpl(propertySpec, message, propertyStorage), getMessageConverter()));
        }
        when(offlineMessage.getDeviceMessageAttributes()).thenReturn(attributes);
        when(offlineMessage.getSpecification()).thenReturn(messageSpec);
        when(offlineMessage.getDeviceMessageSpecPrimaryKey()).thenReturn(messageSpec.getPrimaryKey().getValue());
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
            legacyMessageConverter.setMessagingProtocol(getMessagingProtocol());
        }
        return legacyMessageConverter;
    }

    abstract LegacyMessageConverter doGetMessageConverter();

    /**
     * Gets the value to use for the given {@link PropertySpec}
     */
    protected abstract Object getPropertySpecValue(PropertySpec propertySpec);

    /**
     * An offline implementation version of an {@link com.energyict.mdc.messages.DeviceMessageAttribute} used for test purposes.
     */
    private class TestOfflineDeviceMessageAttribute implements OfflineDeviceMessageAttribute {

        private final DeviceMessageAttribute deviceMessageAttribute;
        private final LegacyMessageConverter messageConverter;
        private PropertySpec propertySpec;
        private String name;
        private String deviceMessageAttributeValue;
        private int deviceMessageId;

        TestOfflineDeviceMessageAttribute(DeviceMessageAttribute deviceMessageAttribute, LegacyMessageConverter messageConverter) {
            this.deviceMessageAttribute = deviceMessageAttribute;
            this.messageConverter = messageConverter;
            goOffline();
        }

        private void goOffline() {
            this.propertySpec = this.deviceMessageAttribute.getSpecification();
            this.name = this.deviceMessageAttribute.getName();
            this.deviceMessageAttributeValue = messageConverter.format(propertySpec, this.deviceMessageAttribute.getValue());
            this.deviceMessageId = this.deviceMessageAttribute.getMessage().getId();
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
