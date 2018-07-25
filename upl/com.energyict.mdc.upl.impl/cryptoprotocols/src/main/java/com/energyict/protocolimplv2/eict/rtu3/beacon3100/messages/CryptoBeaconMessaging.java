package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages;

import com.energyict.common.CommonCryptoMessageExecutor;
import com.energyict.common.CommonCryptoMessaging;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.mdc.upl.DeviceGroupExtractor;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.ObjectMapperService;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.CryptoBeacon3100;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.CryptoMasterDataSerializer;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.CryptoMasterDataSync;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.MasterDataSync;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 2/11/2016 - 16:55
 */
public class CryptoBeaconMessaging extends Beacon3100Messaging {

    private final CommonCryptoMessageExecutor executor;
    private final CryptoMasterDataSerializer cryptoMasterDataSerializer;
    private CommonCryptoMessaging commonCryptoMessaging;
    private CryptoMasterDataSync masterDataSync;

    public CryptoBeaconMessaging(CryptoBeacon3100 beacon3100, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, ObjectMapperService objectMapperService, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMasterDataExtractor deviceMasterDataExtractor, DeviceGroupExtractor deviceGroupExtractor, DeviceExtractor deviceExtractor, CertificateWrapperExtractor certificateWrapperExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(beacon3100, collectedDataFactory, issueFactory, objectMapperService, propertySpecService, nlsService, converter, deviceMasterDataExtractor, deviceGroupExtractor, deviceExtractor, certificateWrapperExtractor, keyAccessorTypeExtractor);
        this.executor = new CommonCryptoMessageExecutor(beacon3100, collectedDataFactory, issueFactory);
        this.cryptoMasterDataSerializer = new CryptoMasterDataSerializer(objectMapperService, propertySpecService, deviceMasterDataExtractor, beacon3100.getDlmsSessionProperties(), nlsService);
        commonCryptoMessaging = new CommonCryptoMessaging(propertySpecService, nlsService, converter);
    }

    @Override
    public Optional<String> prepareMessageContext(com.energyict.mdc.upl.meterdata.Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        if (deviceMessage.getMessageId() == DeviceActionMessage.SyncMasterdataForDC.id()) {
            return Optional.ofNullable(cryptoMasterDataSerializer.serializeMasterData(device, readOldObisCodes()));
        } else if (deviceMessage.getMessageId() == DeviceActionMessage.SyncDeviceDataForDC.id()) {
            return Optional.ofNullable(cryptoMasterDataSerializer.serializeMeterDetails(device));
        } else if (deviceMessage.getMessageId() == DeviceActionMessage.SyncOneConfigurationForDC.id()) {
            int configId = ((BigDecimal) deviceMessage.getAttributes().get(0).getValue()).intValue();
            return Optional.ofNullable(cryptoMasterDataSerializer.serializeMasterDataForOneConfig(configId, readOldObisCodes()));
        } else {
            Optional<String> context = Optional.ofNullable(commonCryptoMessaging.prepareMessageContext(device, deviceMessage));
            return context == null ? super.prepareMessageContext(device, offlineDevice, deviceMessage) : context;
        }
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> supportedMessages = super.getSupportedMessages();
        addSpec(supportedMessages, SecurityMessage.CHANGE_HLS_SECRET_USING_SERVICE_KEY);
        addSpec(supportedMessages, SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY);
        addSpec(supportedMessages, SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT);
        addSpec(supportedMessages, SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY);
        addSpec(supportedMessages, SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT);
        return supportedMessages;
    }

    private void addSpec(List<DeviceMessageSpec> supportedMessages, SecurityMessage spec) {
        if (!supportedMessages.contains(spec)) {
            supportedMessages.add(spec.get(getPropertySpecService(), getNlsService(), getConverter()));
        }
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        String formattedString = commonCryptoMessaging.format(offlineDeviceMessage, propertySpec, messageAttribute);
        return formattedString == null ? super.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute) : formattedString;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList collectedMessageList = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        //TODO: ServiceKey not supported atm in Connexo
/*        //Messages to change the AK or EK need to be combined, and executed separately
        ArrayList<OfflineDeviceMessage> globalKeyMessages = new ArrayList<>();
        Iterator<OfflineDeviceMessage> iterator = pendingMessages.iterator();
        while (iterator.hasNext()) {
            OfflineDeviceMessage pendingMessage = iterator.next();
            if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY)
                    || pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY)
                    || pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT)
                    || pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT)) {
                globalKeyMessages.add(pendingMessage);
                iterator.remove();
            }
        }

        //Execute the message(s) to change global key(s) combined!
        List<CollectedMessage> globalKeyMessageResults = new ArrayList<>();
        if (!globalKeyMessages.isEmpty()) {
            Map<IdAndSecuritySetupObisCode, List<OfflineDeviceMessage>> map = new HashMap<>();

            for (OfflineDeviceMessage globalKeyMessage : globalKeyMessages) {
                ObisCode clientSecuritySetupObis;
                int clientToChangeKeyFor;
                try {
                    clientToChangeKeyFor = getClientId((globalKeyMessage));
                    clientSecuritySetupObis = getSecuritySetupObis(clientToChangeKeyFor);
                } catch (IOException e) {
                    clientToChangeKeyFor = 0;
                    clientSecuritySetupObis = SecuritySetup.getDefaultObisCode();
                }

                IdAndSecuritySetupObisCode key = new IdAndSecuritySetupObisCode(clientToChangeKeyFor, clientSecuritySetupObis);
                map.computeIfAbsent(key, k -> new ArrayList<>());
                map.get(key).add(globalKeyMessage);
            }

            globalKeyMessageResults = executor.changeGlobalKeysUsingServiceKeys(map);
        }*/

        //Now execute all other messages
        CollectedMessageList otherMessageResults = super.executePendingMessages(pendingMessages);
        collectedMessageList.addCollectedMessages(otherMessageResults);

//        for (CollectedMessage globalKeyMessageResult : globalKeyMessageResults) {
//            collectedMessageList.addCollectedMessage(globalKeyMessageResult);
//        }
        return collectedMessageList;
    }

    @Override
    protected CollectedMessage changeEncryptionKey(CollectedMessage collectedMessage, OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        int clientId = getClientId(offlineDeviceMessage);
        ObisCode clientSecuritySetupObis = getSecuritySetupObis(clientId);
        executor.changeEncryptionKey(offlineDeviceMessage, clientSecuritySetupObis, clientId);
        return collectedMessage;
    }

    @Override
    protected CollectedMessage changeAuthKey(CollectedMessage collectedMessage, OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        int clientId = getClientId(offlineDeviceMessage);
        ObisCode clientSecuritySetupObis = getSecuritySetupObis(clientId);
        executor.changeAuthKey(offlineDeviceMessage, clientSecuritySetupObis, clientId);
        return collectedMessage;
    }

    @Override
    protected CollectedMessage changeMasterKey(CollectedMessage collectedMessage, OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        ObisCode clientSecuritySetupObis = getSecuritySetupObis(getClientId(offlineDeviceMessage));
        executor.changeMasterKey(offlineDeviceMessage, clientSecuritySetupObis);
        return collectedMessage;
    }

//    @Override //TODO: ServiceKey not supported atm in Connexo
//    protected CollectedMessage changeHLSSecretUsingServiceKey(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
//        ServiceKeyResponse wrappedServiceKey = executor.getWrappedServiceKey(pendingMessage);
//
//        String warning = wrappedServiceKey.getWarning();
//        if (warning != null) {
//            collectedMessage.setDeviceProtocolInformation("Warning from the HSM because of time difference between prepare and inject: " + warning);
//        }
//
//        getCosemObjectFactory().getAssociationLN().changeHLSSecret(wrappedServiceKey.getServiceKey());
//        return collectedMessage;
//    }

    protected SecuritySetup getSecuritySetup() throws IOException {
        return getSecuritySetup(0); //default security Setup Object for legacy Beacon versions
    }

    protected ObisCode getSecuritySetupObis(int clientId) throws IOException {
        if (clientId != 0) {
            Beacon3100.ClientConfiguration client = Beacon3100.ClientConfiguration.getByID(clientId);
            if (client != null) {
                return client.getSecuritySetupOBIS();
            } else {
                throw new IOException("Could not get Beacon3100 client with id " + clientId);
            }
        }
        // legacy Beacon version Support
        return SecuritySetup.getDefaultObisCode();
    }

    @Override
    protected MasterDataSync getMasterDataSync() {
        if (masterDataSync == null) {
            masterDataSync = new CryptoMasterDataSync(this, getObjectMapperService(), getIssueFactory(), getPropertySpecService(), getDeviceMasterDataExtractor(), getNlsService());
        }
        return masterDataSync;
    }
}