package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.cbo.Password;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.protocol.exceptions.CodingException;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.DeviceMessageSpecSupplier;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaMbusDevice;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link DeviceMessageSupport} for the DSMR 2.3 Mbus slave device, that:
 * - Formats the device message attributes from objects to proper string values
 * - Executes a given message
 * - Has a list of all supported device message specs
 * <p/>
 *
 * @author sva
 * @since 29/11/13 - 14:17
 */
public class Dsmr23MbusMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final LoadProfileExtractor loadProfileExtractor;

    public Dsmr23MbusMessaging(AbstractNtaMbusDevice mbusProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, LoadProfileExtractor loadProfileExtractor) {
        super(mbusProtocol.getMeterProtocol());
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.loadProfileExtractor = loadProfileExtractor;
    }

    private DeviceMessageSpec get(DeviceMessageSpecSupplier supplier) {
        return supplier.get(this.propertySpecService, this.nlsService, this.converter);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                    this.get(ContactorDeviceMessage.CONTACTOR_OPEN),
                    this.get(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE),
                    this.get(ContactorDeviceMessage.CONTACTOR_CLOSE),
                    this.get(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE),
                    this.get(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE),
                    this.get(MBusSetupDeviceMessage.Decommission),
                    this.get(MBusSetupDeviceMessage.SetEncryptionKeys),
                    this.get(MBusSetupDeviceMessage.UseCorrectedValues),
                    this.get(MBusSetupDeviceMessage.UseUncorrectedValues),
                    this.get(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST),
                    this.get(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST));
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.loadProfileExtractor);
            case DeviceMessageConstants.openKeyAttributeName:
            case DeviceMessageConstants.transferKeyAttributeName:
                return ((Password) messageAttribute).getValue();
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());
            case DeviceMessageConstants.contactorActivationDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());  //Epoch (millis)
            default:
                return messageAttribute.toString();  //Used for String and BigDecimal attributes
        }
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, com.energyict.mdc.upl.messages.DeviceMessage deviceMessage) {
        return "";
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        throw CodingException.unsupportedMethod(this.getClass(), "updateSentMessages");
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw CodingException.unsupportedMethod(this.getClass(), "executePendingMessages");
    }
}