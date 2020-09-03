package com.energyict.protocolimplv2.dlms.idis.hs3300.messages;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.PLCConfigurationDeviceMessageExecutor;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.util.List;

import static com.energyict.dlms.aso.SecurityPolicy.REQUESTS_SIGNED_FLAG;
import static com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper.G3_PLC_BANDPLAN;
import static com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper.PSK_KEK_RENEWAL_OBISCODE;
import static com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper.PSK_RENEWAL_OBISCODE;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.certificateIssuerAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.meterSerialNumberAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPSKAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPSKKEKAttributeName;

public class HS3300MessageExecutor extends AbstractMessageExecutor {

    private static final ObisCode ADP_LQI_RANGE = ObisCode.fromString("0.0.94.33.16.255");
    protected static final ObisCode PLC_CLIENT_SECURITY_SETUP = ObisCode.fromString("0.0.43.0.4.255");

    private PLCConfigurationDeviceMessageExecutor plcConfigurationDeviceMessageExecutor;

    public HS3300MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
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
                throw e;
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
            getCosemObjectFactory().getSecuritySetup(PLC_CLIENT_SECURITY_SETUP).deleteCertificate(serialNumber, certificateIssuer);
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

    private CollectedMessage readDlmsAttribute(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
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
}
