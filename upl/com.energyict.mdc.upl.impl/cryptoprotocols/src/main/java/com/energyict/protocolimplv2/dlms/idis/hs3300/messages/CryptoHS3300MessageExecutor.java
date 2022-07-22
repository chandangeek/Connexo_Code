package com.energyict.protocolimplv2.dlms.idis.hs3300.messages;

import com.energyict.common.CommonCryptoMessageExecutor;
import com.energyict.common.framework.CryptoSecurityContext;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.Data;
import com.energyict.encryption.asymetric.util.KeyUtils;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.crypto.IrreversibleKey;
import com.energyict.mdc.upl.crypto.KeyRenewalAgree2EGenerateResponse;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolcommon.exceptions.CodingException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.hs3300.CryptoHS3300SecurityProvider;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.Arrays;

import static com.energyict.dlms.aso.SecurityPolicy.REQUESTS_SIGNED_FLAG;
import static com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper.PSK_KEK_RENEWAL_OBISCODE;
import static com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper.PSK_RENEWAL_OBISCODE;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPSKAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPSKKEKAttributeName;

public class CryptoHS3300MessageExecutor extends HS3300MessageExecutor {

    private static final String SEPARATOR = ",";

    private final CommonCryptoMessageExecutor executor;

    public CryptoHS3300MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory,
                                       KeyAccessorTypeExtractor keyAccessorTypeExtractor, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, keyAccessorTypeExtractor, issueFactory);
        this.executor = new CommonCryptoMessageExecutor(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected CollectedMessage agreeNewKey(CollectedMessage collectedMessage, int keyId) throws IOException {
        if (getProtocol().getDlmsSessionProperties().getSecuritySuite() == 0) {
            throw new ProtocolException("Key agreement is not supported in DLMS suite 0.");
        }

        CryptoSecurityContext securityContext = (CryptoSecurityContext) getProtocol().getDlmsSession().getAso().getSecurityContext();
        CryptoHS3300SecurityProvider securityProvider = (CryptoHS3300SecurityProvider) getProtocol().getDlmsSessionProperties().getSecurityProvider();
        if (securityProvider == null) {
            throw CodingException.protocolImplementationError("General signing and ciphering is not yet supported in the protocol you are using");
        }
        final String clientPrivateSigningKey = securityProvider.getClientPrivateSigningKeyLabel();
        final String storageKeyLabel = securityProvider.getEekStorageLabel();

        getProtocol().journal("Using clientPrivateSigningKey label: " + clientPrivateSigningKey);
        getProtocol().journal("Using storageKeyLabel: " + storageKeyLabel);
        getProtocol().journal("Executing keyRenewalAgree2EGenerate HSM function call.");

        // obtain the required data that device expects for keyAgreement.
        KeyRenewalAgree2EGenerateResponse keyRenewalAgree2EGenerateResponse = Services.hsmService().keyRenewalAgree2EGenerate(securityContext.getSecuritySuite(), keyId, clientPrivateSigningKey, storageKeyLabel);

        // remove the keyAgreement ID form the first position
        byte[] ephemeralPublicKeyEncoded = ProtocolTools.getSubArray(keyRenewalAgree2EGenerateResponse.getAgreementData(), 1, keyRenewalAgree2EGenerateResponse.getAgreementData().length);
        // create the keyData expected by device. The public key of ephemeral key pair, concatenated with digital signature
        byte[] keyData = ProtocolTools.concatByteArrays(ephemeralPublicKeyEncoded, keyRenewalAgree2EGenerateResponse.getSignature());

        Structure keyAgreementData = new Structure();
        keyAgreementData.addDataType(new TypeEnum(keyId));
        keyAgreementData.addDataType(OctetString.fromByteArray(keyData));
        Array array = new Array();
        array.addDataType(keyAgreementData);

        byte[] response;
        try {
            getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().setBit(REQUESTS_SIGNED_FLAG);
            response = getCosemObjectFactory().getSecuritySetup(PLC_CLIENT_SECURITY_SETUP).keyAgreement(array);
        } finally {
            getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().unsetBit(REQUESTS_SIGNED_FLAG);
        }

        // Now verify the received server ephemeral public key and use it to derive the shared secret.
        Array resultArray = AXDRDecoder.decode(response, Array.class);
        AbstractDataType dataType = resultArray.getDataType(0);
        Structure structure = dataType.getStructure();
        if (structure == null) {
            throw new ProtocolException("Expected the response of the key agreement to be an array of structures. However, the first element of the array was of type '" + dataType.getClass()
                    .getSimpleName() + "'.");
        }
        if (structure.nrOfDataTypes() != 2) {
            throw new ProtocolException("Expected the response of the key agreement to be structures with 2 elements each. However, the received structure contains " + structure.nrOfDataTypes() + " elements.");
        }
        OctetString octetString = structure.getDataType(1).getOctetString();
        if (octetString == null) {
            throw new ProtocolException("The responding key_data should be of type octetstring, but was of type '" + structure.getDataType(1).getClass().getSimpleName() + "'.");
        }
        byte[] serverKeyData = octetString.getOctetStr();

        int keySize = KeyUtils.getKeySize(securityContext.getECCCurve());
        byte[] serverEphemeralPublicKeyBytes = ProtocolTools.getSubArray(serverKeyData, 0, keySize);
        byte[] serverSignature = ProtocolTools.getSubArray(serverKeyData, keySize, serverKeyData.length);

        String caCertLabel = securityProvider.getRootCAAlias(SecurityPropertySpecTranslationKeys.SERVER_SIGNING_CERTIFICATE.toString());
        Certificate[] serverSignatureKeyCertificateChain = securityProvider.getCertificateChain(SecurityPropertySpecTranslationKeys.SERVER_SIGNING_CERTIFICATE.toString());

        getProtocol().journal("Using caCertLabel: " + caCertLabel);
        getProtocol().journal("Executing keyRenewalAgree2EFinalise HSM function call.");

        // finalize process and obtain the new HSM key.
        IrreversibleKey newIrreversibleKey = Services.hsmService().keyRenewalAgree2EFinalise(
                securityContext.getSecuritySuite(),
                keyId,
                keyRenewalAgree2EGenerateResponse.getPrivateEccKey(),
                serverEphemeralPublicKeyBytes,
                serverSignature,
                caCertLabel,
                serverSignatureKeyCertificateChain,
                securityContext.getKdfOtherInfo(securityContext.getSystemTitle(), securityContext.getResponseSystemTitle()),
                storageKeyLabel);

        byte[] newHsmKey = newIrreversibleKey.toBase64ByteArray();

        String securityPropertyName = "";

        if (keyId == 0) {
            securityPropertyName = SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString();
            getProtocol().getDlmsSessionProperties().getSecurityProvider().changeEncryptionKey(newHsmKey);
            byte[] oldEncryptionKey = getProtocol().getDlmsSession().getProperties().getSecurityProvider().getGlobalKey();
            if (!Arrays.equals(oldEncryptionKey, newHsmKey)) { //reset FC values after the EK key change
                securityContext.setFrameCounter(1);
                securityContext.getSecurityProvider().getRespondingFrameCounterHandler().setRespondingFrameCounter(-1);
            }
        } else if (keyId == 2) {
            securityPropertyName = SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString();
            getProtocol().getDlmsSessionProperties().getSecurityProvider().changeAuthenticationKey(newHsmKey);
        }

        // Special kind of collected message: it includes the update of the relevant security property with the new, agreed key.
        collectedMessage = this.getCollectedDataFactory().createCollectedMessageWithUpdateSecurityProperty(
                new DeviceIdentifierById(getProtocol().getOfflineDevice().getId()),
                collectedMessage.getMessageIdentifier(),
                securityPropertyName,
                ProtocolTools.getHexStringFromBytes(newIrreversibleKey.getEncryptedKey(), ""));//TODO: also label should be updated....

        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessage;
    }

    private String changeKey(OfflineDeviceMessage pendingMessage, String keyAttributeName, ObisCode keyObis) throws IOException {
        String[] hsmKeyAndLabelAndSmartMeterKey = getDeviceMessageAttributeValue(pendingMessage, keyAttributeName).split(SEPARATOR);
        if (hsmKeyAndLabelAndSmartMeterKey.length != 2) {
            throw DeviceConfigurationException.unexpectedHsmKeyFormat();
        }

        final String newKey = hsmKeyAndLabelAndSmartMeterKey[0];
        final String newWrappedKey = hsmKeyAndLabelAndSmartMeterKey[1];
        byte[] keyBytes = ProtocolTools.getBytesFromHexString(newWrappedKey, "");

        Data keyRenewalObject = getProtocol().getDlmsSession().getCosemObjectFactory().getData(keyObis);
        try {
            getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().setBit(REQUESTS_SIGNED_FLAG);
            keyRenewalObject.setValueAttr(OctetString.fromByteArray(keyBytes));
        } catch (ConnectionCommunicationException e) {
            //Swallow this exception. It is the Beacon that responds an error because the logical device of the meter no longer exists.
            //Indeed, this is expected behaviour because the meter disconnects immediately after the PSK is written.
        } finally {
            getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().unsetBit(REQUESTS_SIGNED_FLAG);
        }

        return newKey;
    }

    @Override
    protected void changePSK(OfflineDeviceMessage pendingMessage) throws IOException {

        String hsmKeyAndLabel = changeKey(pendingMessage, newPSKAttributeName, PSK_RENEWAL_OBISCODE);

    }

    @Override
    protected void changePSKKEK(OfflineDeviceMessage pendingMessage) throws IOException {

        String hsmKeyAndLabel = changeKey(pendingMessage, newPSKKEKAttributeName, PSK_KEK_RENEWAL_OBISCODE);

    }

}
