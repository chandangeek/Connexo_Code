package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast;

import com.energyict.mdc.upl.offline.OfflineDevice;

import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.MasterDataSerializer;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.util.ArrayList;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 1/04/2016 - 15:28
 */
public class MulticastSerializer {

    private static final String SEPARATOR = ",";

    private static MeteringWarehouse getMeteringWarehouse() {
        final MeteringWarehouse meteringWarehouse = MeteringWarehouse.getCurrent();
        if (meteringWarehouse == null) {
            MeteringWarehouse.createBatchContext();
            return MeteringWarehouse.getCurrent();
        } else {
            return meteringWarehouse;
        }
    }

    /**
     * Fetch the relevant information (keys etc) from the given list of AM540 slave devices.
     * Return it in a serialized form (so it can also be used on the remote comserver).
     */
    public static String serialize(OfflineDevice offlineDevice, com.energyict.mdc.upl.messages.DeviceMessage deviceMessage) {
        Object value = deviceMessage.getAttributes().get(0).getValue();
        if (!(value instanceof String)) {
            throw DeviceConfigurationException.invalidPropertyFormat("Device IDs", value.toString(), "Should be a comma separated list of integers");
        }
        ArrayList<MulticastMeterConfig> meterConfigs = new ArrayList<>();
        String deviceIds = (String) value;
        String[] split = deviceIds.split(SEPARATOR);
        //Fetch the keys and information of all the slave devices
        for (String deviceId : split) {
            int id;
            try {
                id = Integer.parseInt(deviceId);
            } catch (NumberFormatException e) {
                throw DeviceConfigurationException.invalidPropertyFormat("Device IDs", deviceIds, "Should be a comma separated list of integers");
            }
            Device slaveDevice = getMeteringWarehouse().getDeviceFactory().find(id);
            if (slaveDevice == null) {
                throw DeviceConfigurationException.invalidPropertyFormat("Device ID", String.valueOf(id), "Device with ID '" + id + "' does not exist");
            }
            Device beaconDevice = slaveDevice.getGateway();
            if (beaconDevice == null || beaconDevice.getId() != offlineDevice.getId()) {
                throw DeviceConfigurationException.invalidPropertyFormat("Device ID", String.valueOf(id), "Device with ID '" + id + "' is not GW linked to Beacon device with ID '" + offlineDevice.getId() + "'");
            }
            String serialNumber = slaveDevice.getSerialNumber();
            if (serialNumber == null || serialNumber.isEmpty()) {
                throw DeviceConfigurationException.missingProperty("SerialNumber", "Device with ID '" + slaveDevice.getId() + "'");
            }

            final byte[] dlmsMeterKEK = MasterDataSerializer.parseKey(offlineDevice.getId(), Beacon3100ConfigurationSupport.DLMS_METER_KEK, beaconDevice.getProtocolProperties().getStringProperty(Beacon3100ConfigurationSupport.DLMS_METER_KEK));
            String macAddress = MasterDataSerializer.parseCallHomeId(slaveDevice);

            //Find the keys in a security set that has clientMacAddress 1 (management client)
            byte[] ak = MasterDataSerializer.getSecurityKey(slaveDevice, SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), 1);
            byte[] ek = MasterDataSerializer.getSecurityKey(slaveDevice, SecurityPropertySpecName.ENCRYPTION_KEY.toString(), 1);
            byte[] password = MasterDataSerializer.getSecurityKey(slaveDevice, SecurityPropertySpecName.PASSWORD.toString(), 1);
            final String wrappedAK = ak == null ? "" : ProtocolTools.getHexStringFromBytes(ProtocolTools.aesWrap(ak, dlmsMeterKEK), "");
            final String wrappedEK = ek == null ? "" : ProtocolTools.getHexStringFromBytes(ProtocolTools.aesWrap(ek, dlmsMeterKEK), "");
            final String wrappedPassword = password == null ? "" : ProtocolTools.getHexStringFromBytes(ProtocolTools.aesWrap(password, dlmsMeterKEK), "");

            //Find the keys in a security set that has clientMacAddress 102 (broadcast client)
            byte[] broadCastAK = MasterDataSerializer.getSecurityKey(slaveDevice, SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), 102);
            byte[] broadCastEK = MasterDataSerializer.getSecurityKey(slaveDevice, SecurityPropertySpecName.ENCRYPTION_KEY.toString(), 102);
            byte[] broadCastPassword = MasterDataSerializer.getSecurityKey(slaveDevice, SecurityPropertySpecName.PASSWORD.toString(), 102);
            final String wrappedMulticastAK = broadCastAK == null ? "" : ProtocolTools.getHexStringFromBytes(ProtocolTools.aesWrap(broadCastAK, dlmsMeterKEK), "");
            final String wrappedMulticastEK = broadCastEK == null ? "" : ProtocolTools.getHexStringFromBytes(ProtocolTools.aesWrap(broadCastEK, dlmsMeterKEK), "");
            final String wrappedMulticastPassword = broadCastPassword == null ? "" : ProtocolTools.getHexStringFromBytes(ProtocolTools.aesWrap(broadCastPassword, dlmsMeterKEK), "");

            MulticastKeySet unicastSecurity = new MulticastKeySet(new MulticastGlobalKeySet(new MulticastKey(wrappedAK), new MulticastKey(wrappedEK), new MulticastKey(wrappedPassword)));
            MulticastKeySet multicastSecurity = new MulticastKeySet(new MulticastGlobalKeySet(new MulticastKey(wrappedMulticastAK), new MulticastKey(wrappedMulticastEK), new MulticastKey(wrappedMulticastPassword)));

            MulticastMeterConfig multicastMeterConfig = new MulticastMeterConfig(macAddress, slaveDevice.getSerialNumber(), unicastSecurity, multicastSecurity);

            meterConfigs.add(multicastMeterConfig);
        }

        ArrayList<MulticastProperty> multicastProperties = new ArrayList<>(); //Note that the multicastProperties here are still empty. They are modelled as message attributes and will be used in the message executor.
        MulticastProtocolConfiguration protocolConfiguration = new MulticastProtocolConfiguration(0, 0, multicastProperties, meterConfigs);
        return MasterDataSerializer.jsonSerialize(protocolConfiguration);
    }
}