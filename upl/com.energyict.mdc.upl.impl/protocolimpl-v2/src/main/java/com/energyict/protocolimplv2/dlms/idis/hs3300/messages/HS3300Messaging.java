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
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.validators.KeyMessageChangeValidator;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;
import com.energyict.sercurity.KeyRenewalInfo;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
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

    protected KeyAccessorTypeExtractor getKeyAccessorTypeExtractor() {
        return keyAccessorTypeExtractor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                PLCConfigurationDeviceMessage.SetToneMaskAttributeName.get(propertySpecService, nlsService, converter),
                PLCConfigurationDeviceMessage.WRITE_G3_PLC_BANDPLAN.get(propertySpecService, nlsService, converter),
                SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY.get(propertySpecService, nlsService, converter),
                SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY.get(propertySpecService, nlsService, converter),
                SecurityMessage.KEY_RENEWAL.get(propertySpecService, nlsService, converter),
                SecurityMessage.CHANGE_PSK_WITH_NEW_KEYS.get(propertySpecService, nlsService, converter),
                SecurityMessage.CHANGE_PSK_KEK.get(propertySpecService, nlsService, converter),
                SecurityMessage.IMPORT_CLIENT_END_DEVICE_CERTIFICATE.get(propertySpecService, nlsService, converter),
                SecurityMessage.DELETE_CERTIFICATE_BY_SERIAL_NUMBER.get(propertySpecService, nlsService, converter),
                PLCConfigurationDeviceMessage.WritePlcG3Timeout.get(propertySpecService, nlsService, converter),
                PLCConfigurationDeviceMessage.SetAdpLBPAssociationSetup_5_Parameters.get(propertySpecService, nlsService, converter),
                PLCConfigurationDeviceMessage.WRITE_ADP_LQI_RANGE.get(propertySpecService, nlsService, converter),
                DeviceActionMessage.ReadDLMSAttribute.get(propertySpecService, nlsService, converter),
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER.get(propertySpecService, nlsService, converter)
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
            propertySpec.getName().equals(DeviceMessageConstants.newPSKKEKAttributeName) ||
            propertySpec.getName().equals(DeviceMessageConstants.newEncryptionKeyAttributeName) ||
            propertySpec.getName().equals(DeviceMessageConstants.newAuthenticationKeyAttributeName)) {

            KeyRenewalInfo keyRenewalInfo = new KeyRenewalInfo(keyAccessorTypeExtractor, (KeyAccessorType) messageAttribute);
            return keyRenewalInfo.toJson();
        } else if (propertySpec.getName().equals(DeviceMessageConstants.keyAccessorTypeAttributeName)) {
            return convertKeyAccessorType((KeyAccessorType) messageAttribute, this.keyAccessorTypeExtractor);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());  // Epoch (millis)
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
                            this.getProtocol().journal("Certificate exception:" + e.getMessage());
                            throw new IllegalArgumentException(e);
                        }
                    }
                }
            }
            return "";  //The message executor will recognize this and set the message to failed
        }
        return messageAttribute.toString();
    }

    private String convertKeyAccessorType(KeyAccessorType messageAttribute, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        KeyRenewalInfo keyRenewalInfo = new KeyRenewalInfo(keyAccessorTypeExtractor, messageAttribute);
        String[] values = new String[]{keyAccessorTypeExtractor.name(messageAttribute), keyRenewalInfo.toJson()};
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(out).writeObject(values);
            return DatatypeConverter.printHexBinary(out.toByteArray());
        } catch (IOException e) {
            throw DataParseException.generalParseException(e);
        }
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        if (deviceMessage.getMessageId() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY.id()) {
            new KeyMessageChangeValidator().validateNewKeyValue(device, deviceMessage, SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY);
        } else if (deviceMessage.getMessageId() == SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY.id()) {
            new KeyMessageChangeValidator().validateNewKeyValue(device, deviceMessage, SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY);
        }
        return Optional.empty();
    }

    protected HS3300MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new HS3300MessageExecutor(getProtocol(), this.collectedDataFactory, this.keyAccessorTypeExtractor, this.issueFactory);
        }
        return messageExecutor;
    }

}
