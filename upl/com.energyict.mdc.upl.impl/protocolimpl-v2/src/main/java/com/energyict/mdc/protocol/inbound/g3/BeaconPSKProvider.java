package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDeviceCache;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.Beacon3100Messaging;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 23/11/2015 - 12:04
 */
public class BeaconPSKProvider extends G3GatewayPSKProvider {

    private final boolean provideProtocolJavaClasName;
    protected InboundDiscoveryContext context;

    public BeaconPSKProvider(DeviceIdentifier deviceIdentifier, boolean provideProtocolJavaClasName) {
        super(deviceIdentifier);
        this.provideProtocolJavaClasName = provideProtocolJavaClasName;
    }

    protected DeviceProtocol newGatewayProtocol(InboundDiscoveryContext context) {
        this.context = context;
        return new Beacon3100(context.getPropertySpecService(), context.getNlsService(), context.getConverter(), context.getCollectedDataFactory(), context.getIssueFactory(), context.getObjectMapperService(), context.getDeviceMasterDataExtractor(), context.getDeviceGroupExtractor(), context.getCertificateWrapperExtractor(), context.getKeyAccessorTypeExtractor(), context.getDeviceExtractor(), context.getMessageFileExtractor());
    }

    protected DlmsSession getDlmsSession(DeviceProtocol gatewayProtocol) {
        return ((Beacon3100) gatewayProtocol).getDlmsSession();
    }

    /**
     * After that the PSKs have been provided, make sure to update the device cache since it can contain the FC
     */
    @Override
    protected void clearInstancesAndStoreCache() {
        try {
            if (this.gatewayProtocol != null) {
                gatewayProtocol.terminate();
                DeviceProtocolCache deviceCache = gatewayProtocol.getDeviceCache();
                CollectedDeviceCache collectedDeviceCache = context.getCollectedDataFactory().createCollectedDeviceCache(getDeviceIdentifier(), deviceCache);
                clearPreviousCollectedDeviceCacheObjects();
                getCollectedDataList().add(collectedDeviceCache);
            }
        } finally {
            super.clearInstancesAndStoreCache();
        }
    }

    private void clearPreviousCollectedDeviceCacheObjects() {
        List<CollectedData> collectedDeviceCacheToRemove = new ArrayList<>();
        for (CollectedData collectedData : getCollectedDataList()) {
            if (collectedData instanceof CollectedDeviceCache) {
                collectedDeviceCacheToRemove.add(collectedData);
            }
        }
        getCollectedDataList().removeAll(collectedDeviceCacheToRemove);
    }

    /**
     * AES wrap the PSK key with the PSK_ENCRYPTION_KEY
     */
    @Override
    protected OctetString wrap(TypedProperties properties, byte[] pskBytes) {
        return this.wrap(com.energyict.mdc.upl.TypedProperties.copyOf(properties), pskBytes);
    }

    protected OctetString wrap(com.energyict.mdc.upl.TypedProperties properties, byte[] pskBytes) {
        final String pskEncryptionKey = properties.getStringProperty(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY);

        if (pskEncryptionKey == null || pskEncryptionKey.isEmpty()) {
            throw DeviceConfigurationException.missingProperty(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY, this.getDeviceIdentifier().toString());
        }

        final byte[] pskEncryptionKeyBytes = parseKey(pskEncryptionKey);

        if (pskEncryptionKeyBytes == null) {
            throw DeviceConfigurationException.invalidPropertyFormat(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY, "(hidden)", "Should be 32 hex characters");
        }

        return OctetString.fromByteArray(ProtocolTools.aesWrap(pskBytes, pskEncryptionKeyBytes));
    }

    /**
     * Extension of the structure: also add the protocol java class name of the slave device.
     * The Beacon3100 then uses this to read out the e-meter serial number using the public client.
     */
    @Override
    protected Structure createMacAndKeyPair(OctetString macAddressOctetString, OctetString wrappedPSKKey, DeviceIdentifier slaveDeviceIdentifier, InboundDiscoveryContext context) {
        final Structure macAndKeyPair = super.createMacAndKeyPair(macAddressOctetString, wrappedPSKKey, slaveDeviceIdentifier, context);

        // Only add the protocol java class name if it is indicated by the property.
        if (provideProtocolJavaClasName) {
            macAndKeyPair.addDataType(OctetString.fromString(context.getInboundDAO().getDeviceProtocolClassName(slaveDeviceIdentifier)));
        }

        return macAndKeyPair;
    }

    protected G3NetworkManagement getG3NetworkManagement(DeviceProtocol gatewayProtocol, DlmsSession dlmsSession) throws NotInObjectListException {
        if (((Beacon3100) gatewayProtocol).getDlmsSessionProperties().getReadOldObisCodes()) {
            return dlmsSession.getCosemObjectFactory().getG3NetworkManagement();
        } else {
            return dlmsSession.getCosemObjectFactory().getG3NetworkManagement(Beacon3100Messaging.G3_NETWORK_MANAGEMENT_NEW_OBISCODE);
        }
    }

}