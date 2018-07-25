package com.energyict.protocolimplv2.dlms.idis.am540.messaging;

import com.energyict.common.CommonCryptoMessageExecutor;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540MessageExecutor;

import java.io.IOException;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/10/2016 - 10:27
 */
public class CryptoAM540MessageExecutor extends AM540MessageExecutor {

    private final CommonCryptoMessageExecutor executor;

    public CryptoAM540MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        this.executor = new CommonCryptoMessageExecutor(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList collectedMessageList = getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        //TODO: ServiceKey not supported atm in Connexo
//        //Messages to change the AK or EK need to be combined, and executed separately
//        ArrayList<OfflineDeviceMessage> globalKeyMessages = new ArrayList<>();
//        Iterator<OfflineDeviceMessage> iterator = pendingMessages.iterator();
//        while (iterator.hasNext()) {
//            OfflineDeviceMessage pendingMessage = iterator.next();
//            if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY)
//                    || pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY)
//                    || pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_PREDEFINED_CLIENT)
//                    || pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_PREDEFINED_CLIENT)) {
//                globalKeyMessages.add(pendingMessage);
//                iterator.remove();
//            }
//        }
//
//        //Execute the message(s) to change global key(s) combined!
//        List<CollectedMessage> globalKeyMessageResults = new ArrayList<>();
//        if (!globalKeyMessages.isEmpty()) {
//            Map<IdAndSecuritySetupObisCode, List<OfflineDeviceMessage>> map = new HashMap<>();
//            for (OfflineDeviceMessage globalKeyMessage : globalKeyMessages) {
//                int clientToChangeKeyFor = getClientId(globalKeyMessage);
//                ObisCode clientSecuritySetupObis = getClientSecuritySetupObis(globalKeyMessage);
//
//                IdAndSecuritySetupObisCode key = new IdAndSecuritySetupObisCode(clientToChangeKeyFor, clientSecuritySetupObis);
//                map.computeIfAbsent(key, k -> new ArrayList<>());
//                map.get(key).add(globalKeyMessage);
//            }
//
//            globalKeyMessageResults = executor.changeGlobalKeysUsingServiceKeys(map);
//        }

        //Now execute all other messages
        CollectedMessageList otherMessageResults = super.executePendingMessages(pendingMessages);
        collectedMessageList.addCollectedMessages(otherMessageResults);

//        for (CollectedMessage globalKeyMessageResult : globalKeyMessageResults) {/TODO: ServiceKey not supported atm in Connexo
//            collectedMessageList.addCollectedMessage(globalKeyMessageResult);
//        }
        return collectedMessageList;
    }

//    @Override //TODO: ServiceKey not supported atm in Connexo
//    protected CollectedMessage changePSKUsingServiceKey(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
//        ServiceKeyResponse wrappedServiceKey = executor.getWrappedServiceKey(pendingMessage);
//
//        String warning = wrappedServiceKey.getWarning();
//        if (warning != null) {
//            collectedMessage.setDeviceProtocolInformation("Warning from the HSM because of time difference between prepare and inject: " + warning);
//        }
//
//        changePSK(wrappedServiceKey.getServiceKey());
//        return collectedMessage;
//    }

    @Override
    protected CollectedMessage changeAuthenticationKeyAndUseNewKey(CollectedMessage collectedMessage, OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        ObisCode clientSecuritySetupObis = getClientSecuritySetupObis(offlineDeviceMessage);
        int clientId = getClientId(offlineDeviceMessage);
        executor.changeAuthKey(offlineDeviceMessage, clientSecuritySetupObis, clientId);
        return collectedMessage;
    }

    @Override
    protected CollectedMessage changeEncryptionKeyAndUseNewKey(CollectedMessage collectedMessage, OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        ObisCode clientSecuritySetupObis = getClientSecuritySetupObis(offlineDeviceMessage);
        int clientId = getClientId(offlineDeviceMessage);
        executor.changeEncryptionKey(offlineDeviceMessage, clientSecuritySetupObis, clientId);
        return collectedMessage;
    }

    @Override
    protected CollectedMessage changeMasterKeyAndUseNewKey(CollectedMessage collectedMessage, OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        ObisCode clientSecuritySetupObis = getClientSecuritySetupObis(offlineDeviceMessage);
        executor.changeMasterKey(offlineDeviceMessage, clientSecuritySetupObis);
        return collectedMessage;
    }

//    /**
//     * Parse the given 'plain' key (message attribute) into the format as expected by the security provider.
//     */
//    private byte[] convertPlainKey(String propertyName, String plainKey) {
//        SecurityPropertyValueParser securityPropertyValueParser = new SecurityPropertyValueParser();
//        return securityPropertyValueParser.parseSecurityPropertyValue(propertyName, plainKey);
//    }
}