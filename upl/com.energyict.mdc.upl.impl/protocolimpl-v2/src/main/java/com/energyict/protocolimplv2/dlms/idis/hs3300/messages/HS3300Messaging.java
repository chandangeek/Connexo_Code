package com.energyict.protocolimplv2.dlms.idis.hs3300.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.CertificateWrapper;
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;
import com.energyict.sercurity.KeyRenewalInfo;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HS3300Messaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final TariffCalendarExtractor calendarExtractor;
    private final CertificateWrapperExtractor certificateWrapperExtractor;
    protected final DeviceMessageFileExtractor messageFileExtractor;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;
    protected HS3300MessageExecutor messageExecutor;

    public HS3300Messaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                           PropertySpecService propertySpecService, NlsService nlsService, Converter converter,
                           TariffCalendarExtractor calendarExtractor, CertificateWrapperExtractor certificateWrapperExtractor,
                           DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol);
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.calendarExtractor = calendarExtractor;
        this.certificateWrapperExtractor = certificateWrapperExtractor;
        this.messageFileExtractor = messageFileExtractor;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
    }

    protected CollectedDataFactory getCollectedDataFactory() {
        return collectedDataFactory;
    }

    protected IssueFactory getIssueFactory() {
        return issueFactory;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                PLCConfigurationDeviceMessage.SetToneMaskAttributeName.get(propertySpecService, nlsService, converter),
                PLCConfigurationDeviceMessage.WRITE_G3_PLC_BANDPLAN.get(propertySpecService, nlsService, converter),
                SecurityMessage.CHANGE_PSK_WITH_NEW_KEYS.get(propertySpecService, nlsService, converter),
                SecurityMessage.CHANGE_PSK_KEK.get(propertySpecService, nlsService, converter),
                SecurityMessage.IMPORT_CLIENT_END_DEVICE_CERTIFICATE.get(propertySpecService, nlsService, converter),
                SecurityMessage.DELETE_CERTIFICATE_BY_SERIAL_NUMBER.get(propertySpecService, nlsService, converter),
                PLCConfigurationDeviceMessage.WritePlcG3Timeout.get(propertySpecService, nlsService, converter),
                PLCConfigurationDeviceMessage.SetAdpLBPAssociationSetup_5_Parameters.get(propertySpecService, nlsService, converter),
                PLCConfigurationDeviceMessage.WRITE_ADP_LQI_RANGE.get(propertySpecService, nlsService, converter),
                DeviceActionMessage.ReadDLMSAttribute.get(propertySpecService, nlsService, converter)
        );
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessageExecutor().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.newPSKAttributeName) ||
            propertySpec.getName().equals(DeviceMessageConstants.newPSKKEKAttributeName)) {

            KeyRenewalInfo keyRenewalInfo = new KeyRenewalInfo(keyAccessorTypeExtractor, (KeyAccessorType) messageAttribute);
            return keyRenewalInfo.toJson();
        } else if (propertySpec.getName().equals(DeviceMessageConstants.certificateWrapperAttributeName)) {
            //Is it a certificate renewal or just an addition of a certificate (e.g. trusted CA certificate) to the Beacon?
            // ==> If there's a passive (temp) value, it's a renewal for sure, use this value.
            // ==> Else, use the active value.

            Optional<Object> valueToUse;
            Optional<Object> tempValue = keyAccessorTypeExtractor.passiveValue((KeyAccessorType) messageAttribute);
            if (tempValue.isPresent()) {
                valueToUse = tempValue;
            } else {
                valueToUse = keyAccessorTypeExtractor.actualValue((KeyAccessorType) messageAttribute);
            }

            if (valueToUse.isPresent()) {
                if (valueToUse.get() instanceof CertificateWrapper) {
                    Optional<X509Certificate> certificate = certificateWrapperExtractor.getCertificate((CertificateWrapper) valueToUse.get());
                    if (certificate.isPresent()) {
                        try {
                            return ProtocolTools.getHexStringFromBytes(certificate.get().getEncoded(), "");
                        } catch (CertificateEncodingException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                }
            }
            return "";  //The message executor will recognize this and set the message to failed
        }
        return messageAttribute.toString();
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    protected HS3300MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new HS3300MessageExecutor(getProtocol(), this.collectedDataFactory, this.issueFactory);
        }
        return messageExecutor;
    }

}
