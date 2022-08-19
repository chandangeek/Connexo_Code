package com.energyict.protocolimplv2.dlms.idis.hs3300.messages;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.PLCConfigurationDeviceMessageExecutor;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;
import com.energyict.sercurity.KeyRenewalInfo;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static com.energyict.dlms.aso.SecurityPolicy.REQUESTS_SIGNED_FLAG;
import static com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper.G3_PLC_BANDPLAN;
import static com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper.PSK_KEK_RENEWAL_OBISCODE;
import static com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper.PSK_RENEWAL_OBISCODE;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.certificateIssuerAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.keyAccessorTypeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.meterSerialNumberAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPSKAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPSKKEKAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.resumeFirmwareUpdateAttributeName;

public class HS3300MessageExecutor extends AbstractMessageExecutor {

    private static final ObisCode ADP_LQI_RANGE = ObisCode.fromString("0.0.94.33.16.255");
    protected static final ObisCode PLC_CLIENT_SECURITY_SETUP = ObisCode.fromString("0.0.43.0.4.255");

    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;
    private PLCConfigurationDeviceMessageExecutor plcConfigurationDeviceMessageExecutor;

    public HS3300MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory,
                                 KeyAccessorTypeExtractor keyAccessorTypeExtractor, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED); // Optimistic
            try {
                CollectedMessage plcMessageResult = getPLCConfigurationDeviceMessageExecutor().executePendingMessage(pendingMessage, collectedMessage);
                if (plcMessageResult != null) {
                    collectedMessage = plcMessageResult;
                } else { // if it was not a PLC message
                    collectedMessage = executeMessage(pendingMessage, collectedMessage);
                }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }   //Else: throw communication exception
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setDeviceProtocolInformation(e.getMessage());
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            } catch (Exception e) {
                //in case we get an exception and we did not managed to put the collected message to failed, we will do it here
                if (!collectedMessage.getNewDeviceMessageStatus().equals(DeviceMessageStatus.FAILED)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }
                final ProtocolException protocolException = new ProtocolException(e, "Error while executing message:"+pendingMessage.getSpecification().getName()+ ": " + e.getMessage());
                throw com.energyict.protocol.exception.ConnectionCommunicationException.unExpectedProtocolError(protocolException); // this leaves the connection intact
            }
            result.addCollectedMessage(collectedMessage);
        }
        return result;
    }

    private PLCConfigurationDeviceMessageExecutor getPLCConfigurationDeviceMessageExecutor() {
        if (plcConfigurationDeviceMessageExecutor == null) {
            plcConfigurationDeviceMessageExecutor = new PLCConfigurationDeviceMessageExecutor(getProtocol().getDlmsSession(), getProtocol().getOfflineDevice(), getCollectedDataFactory(), getIssueFactory());
        }
        return plcConfigurationDeviceMessageExecutor;
    }

    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.WRITE_G3_PLC_BANDPLAN)) {
            writeG3PLCBandplan(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY)) {
            changeAuthenticationKeyAndUseNewKey(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY)) {
            changeEncryptionKeyAndUseNewKey(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.KEY_RENEWAL)) {
            renewKey(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_PSK_WITH_NEW_KEYS)) {
            changePSK(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_PSK_KEK)) {
            changePSKKEK(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.IMPORT_CLIENT_END_DEVICE_CERTIFICATE)) {
            importClientEndDeviceCertificate(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.DELETE_CERTIFICATE_BY_SERIAL_NUMBER)) {
            deleteCertificateBySerialNumber(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.AGREE_NEW_ENCRYPTION_KEY)) {
            collectedMessage = agreeNewKey(collectedMessage, 0);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.AGREE_NEW_AUTHENTICATION_KEY)) {
            collectedMessage = agreeNewKey(collectedMessage, 2);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.WRITE_ADP_LQI_RANGE)) {
            writeADPLQIRange(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.ReadDLMSAttribute)) {
            collectedMessage = this.readDlmsAttribute(collectedMessage, pendingMessage);
        } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER)) {
            upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
        } else {    // Unsupported message
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
        }
        return collectedMessage;
    }

    private void writeG3PLCBandplan(OfflineDeviceMessage pendingMessage) throws IOException {
        final PLCConfigurationDeviceMessage.PLCBandplanType bandplan = PLCConfigurationDeviceMessage.PLCBandplanType.entryForDescription(
                MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.G3_PLC_BANDPLAN).getValue()
        );
        final Data g3PLCBandplan = getCosemObjectFactory().getData(G3_PLC_BANDPLAN);
        
        g3PLCBandplan.setValueAttr( new TypeEnum( bandplan.getId() ) );
    }

    private void changeAuthenticationKeyAndUseNewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        KeyRenewalInfo keyRenewalInfo = KeyRenewalInfo.fromJson(getDeviceMessageAttributeValue(pendingMessage, newAuthenticationKeyAttributeName));
        byte[] newSymmetricKey = ProtocolTools.getBytesFromHexString(keyRenewalInfo.keyValue, "");
        byte[] wrappedKey = ProtocolTools.getBytesFromHexString(keyRenewalInfo.wrappedKeyValue, "");

        renewKey(wrappedKey, SecurityMessage.KeyID.AUTHENTICATION_KEY.getId());

        // Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(newSymmetricKey);
    }

    private void changeEncryptionKeyAndUseNewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        KeyRenewalInfo keyRenewalInfo = KeyRenewalInfo.fromJson(getDeviceMessageAttributeValue(pendingMessage, newEncryptionKeyAttributeName));
        byte[] newSymmetricKey = ProtocolTools.getBytesFromHexString(keyRenewalInfo.keyValue, "");
        byte[] wrappedKey = ProtocolTools.getBytesFromHexString(keyRenewalInfo.wrappedKeyValue, "");

        renewKey(wrappedKey, SecurityMessage.KeyID.GLOBAL_UNICAST_ENCRYPTION_KEY.getId());

        SecurityContext securityContext = getProtocol().getDlmsSession().getAso().getSecurityContext();

        securityContext.setFrameCounter(1);
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(newSymmetricKey);

        securityContext.getSecurityProvider().getRespondingFrameCounterHandler().setRespondingFrameCounter(-1);
    }

    protected void renewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String keyAccessorTypeNameAndTempValue = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, keyAccessorTypeAttributeName).getValue();
        if (keyAccessorTypeNameAndTempValue == null) {
            throw new ProtocolException("The security accessor corresponding to the provided keyAccessorType does not have a valid passive value.");
        }

        String[] values;
        ByteArrayInputStream in = new ByteArrayInputStream(DatatypeConverter.parseHexBinary(keyAccessorTypeNameAndTempValue));
        try {
            values = (String[]) new ObjectInputStream(in).readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw DataParseException.generalParseException(e);
        }
        String keyAccessorName = values[0];
        KeyRenewalInfo keyRenewalInfo = KeyRenewalInfo.fromJson(values[1]);
        byte[] newSymmetricKey = ProtocolTools.getBytesFromHexString(keyRenewalInfo.keyValue, "");
        byte[] wrappedKey = ProtocolTools.getBytesFromHexString(keyRenewalInfo.wrappedKeyValue, "");

        Optional<String> securityAttribute = keyAccessorTypeExtractor.correspondingSecurityAttribute(
                keyAccessorName,
                getProtocol().getDlmsSessionProperties().getSecurityPropertySet().getName()
        );
        if (securityAttribute.isPresent() && securityAttribute.get().equals(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.getKey())) {
            renewKey(wrappedKey, 2);
            getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(newSymmetricKey);
        } else if (securityAttribute.isPresent() && securityAttribute.get().equals(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.getKey())) {
            renewKey(wrappedKey, 0);
            getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(newSymmetricKey);
            resetFC();
        } else {
            throw new ProtocolException("The security accessor corresponding to the provided keyAccessorType is not used as authentication or encryption key in the security setting. Therefore it is not clear which key should be renewed.");
        }
    }

    private void resetFC() {
        SecurityContext securityContext = getProtocol().getDlmsSession().getAso().getSecurityContext();
        securityContext.setFrameCounter(1);
        securityContext.getSecurityProvider().getRespondingFrameCounterHandler().setRespondingFrameCounter(-1);
    }

    protected void changePSK(OfflineDeviceMessage pendingMessage) throws IOException {
        byte[] wrappedKey = getWrappedKey(pendingMessage, newPSKAttributeName);
        Data pskRenewalObject = getProtocol().getDlmsSession().getCosemObjectFactory().getData(PSK_RENEWAL_OBISCODE);
        try {
            getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().setBit(REQUESTS_SIGNED_FLAG);
            pskRenewalObject.setValueAttr(OctetString.fromByteArray(wrappedKey));
        } catch (ConnectionCommunicationException e) {
            //Swallow this exception. It is the Beacon that responds an error because the logical device of the meter no longer exists.
            //Indeed, this is expected behaviour because the meter disconnects immediately after the PSK is written.
        } finally {
            getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().unsetBit(REQUESTS_SIGNED_FLAG);
        }
    }

    protected void changePSKKEK(OfflineDeviceMessage pendingMessage) throws IOException {
        byte[] wrappedKey = getWrappedKey(pendingMessage, newPSKKEKAttributeName);
        Data pskKEKRenewalObject = getProtocol().getDlmsSession().getCosemObjectFactory().getData(PSK_KEK_RENEWAL_OBISCODE);
        try {
            getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().setBit(REQUESTS_SIGNED_FLAG);
            pskKEKRenewalObject.setValueAttr(OctetString.fromByteArray(wrappedKey));
        } catch (ConnectionCommunicationException e) {
            //Swallow this exception. It is the Beacon that responds an error because the logical device of the meter no longer exists.
            //Indeed, this is expected behaviour because the meter disconnects immediately after the PSK is written.
        } finally {
            getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().unsetBit(REQUESTS_SIGNED_FLAG);
        }
    }

    protected void importClientEndDeviceCertificate(OfflineDeviceMessage pendingMessage) throws IOException {
        String encodedCertificateString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.certificateWrapperAttributeName).getValue();
        if (encodedCertificateString == null || encodedCertificateString.isEmpty()) {
            throw new ProtocolException("The provided Certificate cannot be resolved to a valid encoded value");
        }

        byte[] encodedCertificate = ProtocolTools.getBytesFromHexString(encodedCertificateString, "");

        try {
            getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().setBit(REQUESTS_SIGNED_FLAG);
            getCosemObjectFactory().getSecuritySetup(PLC_CLIENT_SECURITY_SETUP).importCertificate(encodedCertificate);
        } finally {
            getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().unsetBit(REQUESTS_SIGNED_FLAG);
        }
    }

    protected void deleteCertificateBySerialNumber(OfflineDeviceMessage pendingMessage) throws IOException {
        String serialNumber = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, meterSerialNumberAttributeName).getValue();
        String certificateIssuer = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, certificateIssuerAttributeName).getValue();

        try {
            getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().setBit(REQUESTS_SIGNED_FLAG);
            // pass the issuer as simple octet-string representation
            OctetString osIssuer = OctetString.fromString(certificateIssuer);

            getCosemObjectFactory().getSecuritySetup(PLC_CLIENT_SECURITY_SETUP).deleteCertificate(serialNumber, osIssuer);
        } finally {
            getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().unsetBit(REQUESTS_SIGNED_FLAG);
        }
    }

    /**
     * Override method in {@link CryptoHS3300MessageExecutor}
     */
    protected CollectedMessage agreeNewKey(CollectedMessage collectedMessage, int keyId) throws IOException {
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol, please use the Crypto protocol variant.");
        return collectedMessage;
    }

    private void writeADPLQIRange(OfflineDeviceMessage pendingMessage) throws IOException {
        final Data adpLQIRange = getProtocol().getDlmsSession().getCosemObjectFactory().getData(ADP_LQI_RANGE);
        final Integer adpLowLQI = Integer.parseInt( MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ADP_LOW_LQI).getValue() );
        final Integer adpHighLQI = Integer.parseInt( MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ADP_HIGH_LQI).getValue() );

        if (adpLowLQI < 0 || adpLowLQI > 255) {
            throw new ProtocolException("ADP Low LQI must be between 0 and 255.");
        }
        if (adpHighLQI < 0 || adpHighLQI > 255) {
            throw new ProtocolException("ADP High LQI must be between 0 and 255.");
        }

        final Structure adpLQIRangeValue = new Structure();
        adpLQIRangeValue.addDataType( new Unsigned8( adpLowLQI ) );
        adpLQIRangeValue.addDataType( new Unsigned8( adpHighLQI ) );

        adpLQIRange.setValueAttr(adpLQIRangeValue);
    }

    protected CollectedMessage readDlmsAttribute(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        String obisCodeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.obisCode).getValue();
        int attributeId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.attributeId).getValue());
        int classId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.classId).getValue());

        obisCodeString = obisCodeString.replace(":", ".").replace("-", ".").replace(" ", "");
        ObisCode obisCode = ObisCode.fromString(obisCodeString);

        DLMSAttribute dlmsAttribute = new DLMSAttribute(obisCode, attributeId, classId);

        try {
            ComposedCosemObject composeObject = getCosemObjectFactory().getComposedCosemObject(dlmsAttribute);
            AbstractDataType abstractDataType = composeObject.getAttribute(dlmsAttribute);
            collectedMessage.setDeviceProtocolInformation(abstractDataType.toString());
        } catch (IOException e) {
            e.printStackTrace();
            collectedMessage.setDeviceProtocolInformation(e.toString());
        }

        return collectedMessage;
    }

    protected void upgradeFirmwareWithActivationDateAndImageIdentifier(OfflineDeviceMessage pendingMessage) throws IOException {
        String path = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateFileAttributeName).getValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName)
                .getValue();   // Will return empty string if the MessageAttribute could not be found
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName)
                .getValue(); // Will return empty string if the MessageAttribute could not be found

        boolean additionalZeros = false;

        getProtocol().journal("Using firmware file: " + path);
        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        if (isResume(pendingMessage)) {
            int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                getProtocol().journal("Resuming transfer from block: " + lastTransferredBlockNumber);
                it.setStartIndex(lastTransferredBlockNumber - 1);
            }
        }

        it.setUsePollingVerifyAndActivate(true); // Poll verification
        it.setPollingDelay(10000);
        it.setPollingRetries(30);
        it.setDelayBeforeSendingBlocks(5000);

        try (RandomAccessFile file = new RandomAccessFile(new File(path), "r")) {
            String actualIdentifier = ImageTransfer.DEFAULT_IMAGE_NAME;
            if (!imageIdentifier.isEmpty()) {
                actualIdentifier = imageIdentifier;
            }
            getProtocol().journal("Starting block transfer of image file using identifier " + actualIdentifier);
            it.upgrade(new ImageTransfer.RandomAccessFileImageBlockSupplier(file), additionalZeros, actualIdentifier, false);
            getProtocol().journal("Block transfer finished");
        }

        if (activationDate.isEmpty()) {
            try {
                getProtocol().journal("Activating immediately");
                it.setUsePollingVerifyAndActivate(false); // Don't use polling for the activation!
                it.imageActivation();
            } catch (DataAccessResultException e) {
                if (isTemporaryFailure(e)) {
                    getProtocol().journal("Received temporary failure. Meter will activate the image when this communication session is closed, moving on.");
                } else {
                    getProtocol().journal(Level.WARNING, e.getLocalizedMessage());
                    throw e;
                }
            }
        } else {
            getProtocol().journal("Setting future activation date: " + activationDate);
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
            Array dateArray = convertEpochToDateTimeArray(activationDate);
            sas.writeExecutionTime(dateArray);
        }
    }

    private boolean isResume(OfflineDeviceMessage pendingMessage) {
        return Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, resumeFirmwareUpdateAttributeName).getValue());
    }

    private boolean isTemporaryFailure(Throwable e) {
        if (e == null) {
            return false;
        } else if (e instanceof DataAccessResultException) {
            return (((DataAccessResultException) e).getDataAccessResult() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode());
        } else {
            return false;
        }
    }
}
