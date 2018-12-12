package com.energyict.common;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.FrameCounterCache;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 9/11/2016 - 17:27
 */
public class CommonCryptoMessageExecutor extends AbstractMessageExecutor {

    private static final String SEPARATOR = ",";

    public CommonCryptoMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    private String changeKeyAndUseNewKey(OfflineDeviceMessage offlineDeviceMessage, int keyId, String keyAttributeName, ObisCode clientSecuritySetupObis) throws
            IOException {

        String[] hsmKeyAndLabelAndSmartMeterKey = getDeviceMessageAttributeValue(offlineDeviceMessage, keyAttributeName).split(SEPARATOR);
        if (hsmKeyAndLabelAndSmartMeterKey.length != 2) {
            throw DeviceConfigurationException.unexpectedHsmKeyFormat();
        }

        String newKey = hsmKeyAndLabelAndSmartMeterKey[0];
        String newWrappedKey = hsmKeyAndLabelAndSmartMeterKey[1];
        byte[] keyBytes = ProtocolTools.getBytesFromHexString(newWrappedKey, "");

        Array keyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(keyId));
        keyData.addDataType(OctetString.fromByteArray(keyBytes));
        keyArray.addDataType(keyData);

        getProtocol().getDlmsSession().getCosemObjectFactory().getSecuritySetup(clientSecuritySetupObis).transferGlobalKey(keyArray);

        return newKey;
    }

    public void changeMasterKey(OfflineDeviceMessage offlineDeviceMessage, ObisCode clientSecuritySetupObis) throws IOException {
        String newKey = changeKeyAndUseNewKey(offlineDeviceMessage, SecurityMessage.KeyID.MASTER_KEY.getId(), newMasterKeyAttributeName, clientSecuritySetupObis);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeMasterKey(getHsmIrreversibleKeyBytes(newKey));
    }

    public void changeAuthKey(OfflineDeviceMessage offlineDeviceMessage, ObisCode clientSecuritySetupObis, int clientToChangeKeyFor) throws IOException {
        String newKey = changeKeyAndUseNewKey(offlineDeviceMessage, SecurityMessage.KeyID.AUTHENTICATION_KEY.getId(), newAuthenticationKeyAttributeName, clientSecuritySetupObis);

        //Use the new AK immediately, if it was renewed for the current client
        int clientInUse = getProtocol().getDlmsSession().getProperties().getClientMacAddress();
        if (isForCurrentClient(clientToChangeKeyFor, clientInUse)) {
            getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(getHsmIrreversibleKeyBytes(newKey));
        }

    }

    /**
     * Return true if the given client of the key renewal message is the same as the client that is currently used for this communication session.
     */
    private boolean isForCurrentClient(int clientToChangeKeyFor, int clientInUse) {
        return clientToChangeKeyFor == clientInUse || clientToChangeKeyFor == 0;
    }

    public void changeEncryptionKey(OfflineDeviceMessage offlineDeviceMessage, ObisCode clientSecuritySetupObis, int clientToChangeKeyFor) throws IOException {
        //TODO: see how this key will be changed? how should be implemented

        String newKey = changeKeyAndUseNewKey(offlineDeviceMessage, SecurityMessage.KeyID.GLOBAL_UNICAST_ENCRYPTION_KEY.getId(), newEncryptionKeyAttributeName, clientSecuritySetupObis);

        int clientInUse = getProtocol().getDlmsSession().getProperties().getClientMacAddress();
        //Use the new EK immediately, if it was renewed for the current client
        if (isForCurrentClient(clientToChangeKeyFor, clientInUse)) {
            getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(getHsmIrreversibleKeyBytes(newKey));
        }

        resetFCForClient(clientToChangeKeyFor, clientInUse);
    }

    private byte[] getHsmIrreversibleKeyBytes(String irreversibleKeyAndLabel) {
        return new IrreversibleKeyImpl(irreversibleKeyAndLabel).toBase64ByteArray();
    }

 /*   *//** //TODO: ServiceKey not supported atm in Connexo
     * Writing of the global keys (AK and EK) must be combined.
     * Returns the results {@link CollectedMessage}s for the messages.
     *//*
    public List<CollectedMessage> changeGlobalKeysUsingServiceKeys(Map<IdAndSecuritySetupObisCode, List<OfflineDeviceMessage>> map) {

        List<CollectedMessage> results = new ArrayList<>();

        for (IdAndSecuritySetupObisCode key : map.keySet()) {
            List<CollectedMessage> resultsPerClient = new ArrayList<>();
            List<OfflineDeviceMessage> globalKeyMessages = map.get(key);
            int clientToChangeKeyFor = key.getClientToChangeKeyFor();
            ObisCode clientSecuritySetupObis = key.getClientSecuritySetupObis();

            //Create a {@link CollectedMessage} for every message
            for (OfflineDeviceMessage globalKeyMessage : globalKeyMessages) {
                CollectedMessage collectedMessage = createCollectedMessage(globalKeyMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
                resultsPerClient.add(collectedMessage);
            }

            if (clientToChangeKeyFor == ClientSecuritySetup.Consumer_Information.getID()) {
                //The CIP client only has an EK, no AK.
                if (globalKeyMessages.size() != 1 || !isEKMessage(globalKeyMessages.get(0))) {
                    for (int i = 0; i < globalKeyMessages.size(); i++) {
                        OfflineDeviceMessage globalKeyMessage = globalKeyMessages.get(i);
                        CollectedMessage collectedMessage = resultsPerClient.get(i);
                        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                        String msg = "The customer interface client (CIP, 103) only has an encryption key, no authentication key";
                        collectedMessage.setFailureInformation(ResultType.ConfigurationError, createMessageFailedIssue(globalKeyMessage, msg));
                        collectedMessage.setDeviceProtocolInformation(msg);
                    }
                    results.addAll(resultsPerClient);
                    continue;
                }
            } else if (globalKeyMessages.size() != 2 || !containsAKAndEKMessage(globalKeyMessages)) {
                //Set the message(s) to failed if it's not an AK and EK message (per client)
                for (int i = 0; i < globalKeyMessages.size(); i++) {
                    OfflineDeviceMessage globalKeyMessage = globalKeyMessages.get(i);
                    CollectedMessage collectedMessage = resultsPerClient.get(i);
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    String msg = "Service keys can only be injected if it's exactly 1 AK and 1 EK, in the same communication session";
                    collectedMessage.setFailureInformation(ResultType.ConfigurationError, createMessageFailedIssue(globalKeyMessage, msg));
                    collectedMessage.setDeviceProtocolInformation(msg);
                }
                results.addAll(resultsPerClient);
                continue;
            }

            //If the messages are OK, write the new AK and EK in a single request
            try {
                Array globalKeyArray = new Array();
                byte[] newAK = null;
                byte[] newEK = null;

                for (int i = 0; i < globalKeyMessages.size(); i++) {
                    OfflineDeviceMessage globalKeyMessage = globalKeyMessages.get(i);
                    ServiceKeyResponse wrappedServiceKey = getWrappedServiceKey(globalKeyMessage);

                    String warning = wrappedServiceKey.getWarning();
                    if (warning != null) {
                        resultsPerClient.get(i).setDeviceProtocolInformation("Warning from the HSM because of time difference between prepare and inject: " + warning);
                    }

                    Integer keyType = null;
                    if (globalKeyMessage.getSpecification() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY ||
                            globalKeyMessage.getSpecification() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_PREDEFINED_CLIENT ||
                            globalKeyMessage.getSpecification() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT) {
                        keyType = 2;        //global authentication key
                        String newAKString = getDeviceMessageAttributeValue(globalKeyMessage, DeviceMessageConstants.newAuthenticationKeyAttributeName);
                        newAK = ProtocolTools.getBytesFromHexString(newAKString, "");
                        if (newAK.length != 16) {
                            throw new ProtocolException("The new authentication key should be 32 hex characters");
                        }
                    } else if (globalKeyMessage.getSpecification() == SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY ||
                            globalKeyMessage.getSpecification() == SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_PREDEFINED_CLIENT ||
                            globalKeyMessage.getSpecification() == SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT) {
                        keyType = 0;        //global unicast encryption key
                        String newEKString = getDeviceMessageAttributeValue(globalKeyMessage, DeviceMessageConstants.newEncryptionKeyAttributeName);
                        newEK = ProtocolTools.getBytesFromHexString(newEKString, "");
                        if (newEK.length != 16) {
                            throw new ProtocolException("The new encryption key should be 32 hex characters");
                        }
                    }

                    if (keyType != null) {
                        Structure keyData = new Structure();
                        keyData.addDataType(new TypeEnum(keyType));
                        keyData.addDataType(OctetString.fromByteArray(wrappedServiceKey.getServiceKey()));
                        globalKeyArray.addDataType(keyData);
                    }
                }

                //Now send both the AK and EK to the meter
                SecuritySetup ss = getCosemObjectFactory().getSecuritySetup(clientSecuritySetupObis);
                ss.transferGlobalKey(globalKeyArray);

                int clientInUse = getProtocol().getDlmsSession().getProperties().getClientMacAddress();
                if (newAK != null) {
                    //If the new key is for the current client: update the key in the security provider, it is used instantly
                    if (isForCurrentClient(clientToChangeKeyFor, clientInUse)) {
                        SecurityProvider securityProvider = getProtocol().getDlmsSession().getProperties().getSecurityProvider();
                        securityProvider.changeAuthenticationKey(newAK);
                        if (securityProvider instanceof CryptoSecurityProvider) {
                            ((CryptoSecurityProvider) securityProvider).setUsingServiceKeys(true);
                        }
                    }
                }
                if (newEK != null) {
                    //If the new key is for the current client: update the key in the security provider, it is used instantly
                    if (isForCurrentClient(clientToChangeKeyFor, clientInUse)) {
                        SecurityProvider securityProvider = getProtocol().getDlmsSession().getProperties().getSecurityProvider();
                        securityProvider.changeEncryptionKey(newEK);
                        if (securityProvider instanceof CryptoSecurityProvider) {
                            ((CryptoSecurityProvider) securityProvider).setUsingServiceKeys(true);
                        }
                    }

                    //Also reset the FC (taking the client into account)
                    resetFCForClient(clientToChangeKeyFor, clientInUse);
                }
            } catch (IOException e) {
                //In case of troubles, set all global key messages to failed
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                    for (int i = 0; i < resultsPerClient.size(); i++) {
                        CollectedMessage collectedMessage = resultsPerClient.get(i);
                        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                        collectedMessage.setDeviceProtocolInformation(e.getMessage());
                        collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(globalKeyMessages.get(i), e));
                    }
                }   //Else: throw communication exception
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                //In case of troubles, set all global key messages to failed
                for (int i = 0; i < resultsPerClient.size(); i++) {
                    CollectedMessage collectedMessage = resultsPerClient.get(i);
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(globalKeyMessages.get(i), e));
                }
            }

            results.addAll(resultsPerClient);
        }

        return results;
    }

    *//**
     * Writing of the global keys (AK and EK) must be combined.
     * Returns the results {@link CollectedMessage}s for the messages.
     *//*
    public List<CollectedMessage> changeGlobalKeysUsingServiceKeys(List<OfflineDeviceMessage> globalKeyMessages) {

        List<CollectedMessage> results = new ArrayList<>();

        //Create a {@link CollectedMessage} for every message
        for (OfflineDeviceMessage globalKeyMessage : globalKeyMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(globalKeyMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            results.add(collectedMessage);
        }

        //Set the message(s) to failed if it's not an AK and EK message
        if (globalKeyMessages.size() != 2 || !containsAKAndEKMessage(globalKeyMessages)) {
            for (int i = 0; i < globalKeyMessages.size(); i++) {
                OfflineDeviceMessage globalKeyMessage = globalKeyMessages.get(i);
                CollectedMessage collectedMessage = results.get(i);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                String msg = "Service keys can only be injected if it's exactly 1 AK and 1 EK, in the same communication session";
                collectedMessage.setFailureInformation(ResultType.ConfigurationError, createMessageFailedIssue(globalKeyMessage, msg));
                collectedMessage.setDeviceProtocolInformation(msg);
            }
            return results;
        }

        //If the messages are OK, write the new AK and EK in a single request
        try {
            Array globalKeyArray = new Array();
            byte[] newAK = null;
            byte[] newEK = null;

            for (int i = 0; i < globalKeyMessages.size(); i++) {
                OfflineDeviceMessage globalKeyMessage = globalKeyMessages.get(i);
                ServiceKeyResponse wrappedServiceKey = getWrappedServiceKey(globalKeyMessage);

                String warning = wrappedServiceKey.getWarning();
                if (warning != null) {
                    results.get(i).setDeviceProtocolInformation("Warning from the HSM because of time difference between prepare and inject: " + warning);
                }

                Integer keyType = null;
                if (globalKeyMessage.getSpecification() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY) {
                    keyType = 2;        //global authentication key
                    String newAKString = getDeviceMessageAttributeValue(globalKeyMessage, DeviceMessageConstants.newAuthenticationKeyAttributeName);
                    newAK = ProtocolTools.getBytesFromHexString(newAKString, "");
                    if (newAK.length != 16) {
                        throw new ProtocolException("The new authentication key should be 32 hex characters");
                    }
                } else if (globalKeyMessage.getSpecification() == SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY) {
                    keyType = 0;        //global unicast encryption key
                    String newEKString = getDeviceMessageAttributeValue(globalKeyMessage, DeviceMessageConstants.newEncryptionKeyAttributeName);
                    newEK = ProtocolTools.getBytesFromHexString(newEKString, "");
                    if (newEK.length != 16) {
                        throw new ProtocolException("The new encryption key should be 32 hex characters");
                    }
                }

                if (keyType != null) {
                    Structure keyData = new Structure();
                    keyData.addDataType(new TypeEnum(keyType));
                    keyData.addDataType(OctetString.fromByteArray(wrappedServiceKey.getServiceKey()));
                    globalKeyArray.addDataType(keyData);
                }
            }

            //Now send both the AK and EK to the meter
            SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
            ss.transferGlobalKey(globalKeyArray);

            if (newAK != null) {
                //Update the key in the security provider, it is used instantly
                getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(newAK);
            }
            if (newEK != null) {
                //Update the key in the security provider, it is used instantly
                getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(newEK);

                //Also reset the FC
                resetFC();
            }
        } catch (IOException e) {
            //In case of troubles, set all global key messages to failed
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                for (int i = 0; i < results.size(); i++) {
                    CollectedMessage collectedMessage = results.get(i);
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(globalKeyMessages.get(i), e));
                }
            }   //Else: throw communication exception
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            //In case of troubles, set all global key messages to failed
            for (int i = 0; i < results.size(); i++) {
                CollectedMessage collectedMessage = results.get(i);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setDeviceProtocolInformation(e.getMessage());
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(globalKeyMessages.get(i), e));
            }
        }

        return results;
    }

    public ServiceKeyResponse getWrappedServiceKey(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        byte[] preparedData = ProtocolTools.getBytesFromHexString(getDeviceMessageAttributeValue(pendingMessage, preparedDataAttributeName), "");
        byte[] signature = ProtocolTools.getBytesFromHexString(getDeviceMessageAttributeValue(pendingMessage, signatureAttributeName), "");
        String verifyKey = getDeviceMessageAttributeValue(pendingMessage, verificationKeyAttributeName);

        ServiceKeyResponse serviceKeyResponse;
        try {
            serviceKeyResponse = ProtocolService.INSTANCE.get().serviceKeyInjection(preparedData, signature, verifyKey);
        } catch (HsmException e) {
            throw new ProtocolException(e);
        }
        if (serviceKeyResponse == null) {
            throw new ProtocolException("Incorrect signature, cannot write the service key. HSM returned null serviceKeyResponse.");
        }
        byte[] serviceKey = serviceKeyResponse.getServiceKey();
        if (serviceKey == null) {
            throw new ProtocolException("Incorrect signature, cannot write the service key. HSM returned null serviceKey. Warning from HSM: " + serviceKeyResponse.getWarning());
        }
        return serviceKeyResponse;
    }
*/
    /**
     * Return true if the provided list of global key messages contains a message to change the AK, and a message to change the EK.
     */
    private boolean containsAKAndEKMessage(List<OfflineDeviceMessage> globalKeyMessages) {
        boolean containsEKMessage = false;
        boolean containsAKMessage = false;
        for (OfflineDeviceMessage globalKeyMessage : globalKeyMessages) {
            containsEKMessage |= isEKMessage(globalKeyMessage);
            containsAKMessage |= isAKMessage(globalKeyMessage);
        }
        return containsAKMessage && containsEKMessage;
    }

    private boolean isEKMessage(OfflineDeviceMessage globalKeyMessage) {
        return globalKeyMessage.getSpecification() == SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY ||
                globalKeyMessage.getSpecification() == SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_PREDEFINED_CLIENT ||
                globalKeyMessage.getSpecification() == SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT;
    }

    private boolean isAKMessage(OfflineDeviceMessage globalKeyMessage) {
        return globalKeyMessage.getSpecification() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY ||
                globalKeyMessage.getSpecification() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_PREDEFINED_CLIENT ||
                globalKeyMessage.getSpecification() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> list) {
        return null;    //Not used in this class
    }

    private void resetFCForClient(int clientToChangeKeyFor, int clientInUse) throws ProtocolException {
        if (isForCurrentClient(clientToChangeKeyFor, clientInUse)) {
            SecurityContext securityContext = getProtocol().getDlmsSession().getAso().getSecurityContext();
            securityContext.setFrameCounter(1);
            securityContext.getSecurityProvider().getRespondingFrameCounterHandler().setRespondingFrameCounter(-1);
        } else {
            resetCachedFCForClient(clientToChangeKeyFor);
        }
    }

    private void resetFC() {
        SecurityContext securityContext = getProtocol().getDlmsSession().getAso().getSecurityContext();
        securityContext.setFrameCounter(1);
        securityContext.getSecurityProvider().getRespondingFrameCounterHandler().setRespondingFrameCounter(-1);
    }

    private void resetCachedFCForClient(int clientToChangeKeyFor) throws ProtocolException {
        DLMSCache dlmsCache = getProtocol().getDeviceCache();
        if (dlmsCache instanceof FrameCounterCache) {
            ((FrameCounterCache) dlmsCache).setTXFrameCounter(clientToChangeKeyFor, 1);
        } else {
            throw new ProtocolException("DlmsCache instance (" + dlmsCache.getClass().toString() + ") should implement interface FrameCounterCache.");
        }
    }
}
