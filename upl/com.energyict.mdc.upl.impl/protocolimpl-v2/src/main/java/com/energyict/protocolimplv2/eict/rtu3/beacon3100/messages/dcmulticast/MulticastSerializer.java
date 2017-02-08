package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast;

import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.ObjectMapperService;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.MasterDataSerializer;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 1/04/2016 - 15:28
 */
public class MulticastSerializer {

    private static final String SEPARATOR = ",";

    private final DeviceMasterDataExtractor extractor;
    private final MasterDataSerializer serializer;

    public MulticastSerializer(ObjectMapperService objectMapperService, PropertySpecService propertySpecService, DeviceMasterDataExtractor extractor) {
        this(extractor, new MasterDataSerializer(objectMapperService, propertySpecService, extractor));
    }

    public MulticastSerializer(DeviceMasterDataExtractor extractor, MasterDataSerializer serializer) {
        this.extractor = extractor;
        this.serializer = serializer;
    }

    /**
     * Fetch the relevant information (keys etc) from the given list of AM540 slave devices.
     * Return it in a serialized form (so it can also be used on the remote comserver).
     */
    public String serialize(Device device, OfflineDevice offlineDevice, com.energyict.mdc.upl.messages.DeviceMessage deviceMessage) {
        Object value = deviceMessage.getAttributes().get(0).getValue();
        if (!(value instanceof String)) {
            throw DeviceConfigurationException.invalidPropertyFormat("Device IDs", value.toString(), "Should be a comma separated list of integers");
        }
        ArrayList<MulticastMeterConfig> meterConfigs = new ArrayList<>();
        String deviceIds = (String) value;
        String[] split = deviceIds.split(SEPARATOR);
        //Fetch the keys and information of all the slave devices
        for (String deviceId : split) {
            long id;
            try {
                id = Long.parseLong(deviceId);
            } catch (NumberFormatException e) {
                throw DeviceConfigurationException.invalidPropertyFormat("Device IDs", deviceIds, "Should be a comma separated list of integers");
            }
            Device slaveDevice = this.extractor.find(id).orElseThrow(() -> DeviceConfigurationException.invalidPropertyFormat("Device ID", String.valueOf(id), "Device with ID '" + id + "' does not exist"));
            Device beaconDevice;
            Optional<Device> gateway = this.extractor.gateway(slaveDevice);
            if (!gateway.isPresent() || this.extractor.id(gateway.get()) != offlineDevice.getId()) {
                throw DeviceConfigurationException.invalidPropertyFormat("Device ID", String.valueOf(id), "Device with ID '" + id + "' is not GW linked to Beacon device with ID '" + offlineDevice.getId() + "'");
            } else {
                beaconDevice = gateway.get();
            }
            String serialNumber = this.extractor.serialNumber(slaveDevice);
            if (serialNumber == null || serialNumber.isEmpty()) {
                throw DeviceConfigurationException.missingProperty("SerialNumber", "Device with ID '" + id + "'");
            }

            final byte[] dlmsMeterKEK =
                    this.serializer.parseKey(
                            device,
                            Beacon3100ConfigurationSupport.DLMS_METER_KEK,
                            TypedProperties
                                    .copyOf(this.extractor.protocolProperties(beaconDevice))
                                    .getStringProperty(Beacon3100ConfigurationSupport.DLMS_METER_KEK));
            String macAddress = this.serializer.parseCallHomeId(slaveDevice);

            //Find the keys in a security set that has clientMacAddress 1 (management client)
            byte[] ak = this.serializer.getSecurityKey(slaveDevice, SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), 1);
            byte[] ek = this.serializer.getSecurityKey(slaveDevice, SecurityPropertySpecName.ENCRYPTION_KEY.toString(), 1);
            byte[] password = this.serializer.getSecurityKey(slaveDevice, SecurityPropertySpecName.PASSWORD.toString(), 1);
            final String wrappedAK = ak == null ? "" : ProtocolTools.getHexStringFromBytes(ProtocolTools.aesWrap(ak, dlmsMeterKEK), "");
            final String wrappedEK = ek == null ? "" : ProtocolTools.getHexStringFromBytes(ProtocolTools.aesWrap(ek, dlmsMeterKEK), "");
            final String wrappedPassword = password == null ? "" : ProtocolTools.getHexStringFromBytes(ProtocolTools.aesWrap(password, dlmsMeterKEK), "");

            //Find the keys in a security set that has clientMacAddress 102 (broadcast client)
            byte[] broadCastAK = this.serializer.getSecurityKey(slaveDevice, SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), 102);
            byte[] broadCastEK = this.serializer.getSecurityKey(slaveDevice, SecurityPropertySpecName.ENCRYPTION_KEY.toString(), 102);
            byte[] broadCastPassword = this.serializer.getSecurityKey(slaveDevice, SecurityPropertySpecName.PASSWORD.toString(), 102);
            final String wrappedMulticastAK = broadCastAK == null ? "" : ProtocolTools.getHexStringFromBytes(ProtocolTools.aesWrap(broadCastAK, dlmsMeterKEK), "");
            final String wrappedMulticastEK = broadCastEK == null ? "" : ProtocolTools.getHexStringFromBytes(ProtocolTools.aesWrap(broadCastEK, dlmsMeterKEK), "");
            final String wrappedMulticastPassword = broadCastPassword == null ? "" : ProtocolTools.getHexStringFromBytes(ProtocolTools.aesWrap(broadCastPassword, dlmsMeterKEK), "");

            MulticastKeySet unicastSecurity = new MulticastKeySet(new MulticastGlobalKeySet(new MulticastKey(wrappedAK), new MulticastKey(wrappedEK), new MulticastKey(wrappedPassword)));
            MulticastKeySet multicastSecurity = new MulticastKeySet(new MulticastGlobalKeySet(new MulticastKey(wrappedMulticastAK), new MulticastKey(wrappedMulticastEK), new MulticastKey(wrappedMulticastPassword)));

            MulticastMeterConfig multicastMeterConfig = new MulticastMeterConfig(macAddress, this.extractor.serialNumber(slaveDevice), unicastSecurity, multicastSecurity);

            meterConfigs.add(multicastMeterConfig);
        }

        List<MulticastProperty> multicastProperties = new ArrayList<>(); //Note that the multicastProperties here are still empty. They are modelled as message attributes and will be used in the message executor.
        MulticastProtocolConfiguration protocolConfiguration = new MulticastProtocolConfiguration(0, 0, multicastProperties, meterConfigs);
        return this.serializer.jsonSerialize(protocolConfiguration);
    }

}