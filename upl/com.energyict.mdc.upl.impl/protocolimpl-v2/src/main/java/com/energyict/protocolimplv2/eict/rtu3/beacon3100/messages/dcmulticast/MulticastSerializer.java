package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast;

import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageAttribute;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.MasterDataSerializer;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MulticastClientWPort;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UnicastClientWPort;

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

    public MulticastSerializer(DeviceMasterDataExtractor extractor, MasterDataSerializer serializer) {
        this.extractor = extractor;
        this.serializer = serializer;
    }

    /**
     * Fetch the relevant information (keys etc) from the given list of AM540 slave devices.
     * Return it in a serialized form (so it can also be used on the remote comserver).
     */
    public String serialize(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage, Beacon3100Properties beacon3100Properties) {
        Object value = deviceMessage.getAttributes().get(0).getValue();
        if (!(value instanceof String)) {
            throw DeviceConfigurationException.invalidPropertyFormat("Device IDs", value.toString(), "Should be a comma separated list of integers");
        }
        int unicastClient = Integer.parseInt(getDeviceMessageAttributeValueAsString(deviceMessage, UnicastClientWPort));
        int multicastClient = Integer.parseInt(getDeviceMessageAttributeValueAsString(deviceMessage, MulticastClientWPort));

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
            byte[] ak = this.serializer.getSecurityKey(slaveDevice, SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString(), unicastClient);
            byte[] ek = this.serializer.getSecurityKey(slaveDevice, SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), unicastClient);
            byte[] password = this.serializer.getSecurityKey(slaveDevice, SecurityPropertySpecTranslationKeys.PASSWORD.toString(), unicastClient);
            final String wrappedAK = ak == null ? "" : ProtocolTools.getHexStringFromBytes(serializer.wrap(dlmsMeterKEK, ak), "");
            final String wrappedEK = ek == null ? "" : ProtocolTools.getHexStringFromBytes(serializer.wrap(dlmsMeterKEK, ek), "");
            final String wrappedPassword = password == null ? "" : ProtocolTools.getHexStringFromBytes(serializer.wrap(dlmsMeterKEK, password), "");

            //Find the keys in a security set that has clientMacAddress 102 (multicast client)
            byte[] multiCastAK = this.serializer.getSecurityKey(slaveDevice, SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString(), multicastClient);
            byte[] multiCastEK = this.serializer.getSecurityKey(slaveDevice, SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), multicastClient);
            byte[] multiCastPassword = this.serializer.getSecurityKey(slaveDevice, SecurityPropertySpecTranslationKeys.PASSWORD.toString(), multicastClient);
            final String wrappedMulticastAK = multiCastAK == null ? "" : ProtocolTools.getHexStringFromBytes(serializer.wrap(dlmsMeterKEK, multiCastAK), "");
            final String wrappedMulticastEK = multiCastEK == null ? "" : ProtocolTools.getHexStringFromBytes(serializer.wrap(dlmsMeterKEK, multiCastEK), "");
            final String wrappedMulticastPassword = multiCastPassword == null ? "" : ProtocolTools.getHexStringFromBytes(serializer.wrap(dlmsMeterKEK, multiCastPassword), "");

            MulticastKeySet unicastSecurity = new MulticastKeySet(new MulticastGlobalKeySet(new MulticastKey(wrappedAK), new MulticastKey(wrappedEK), new MulticastKey(wrappedPassword)));
            MulticastKeySet multicastSecurity = new MulticastKeySet(new MulticastGlobalKeySet(new MulticastKey(wrappedMulticastAK), new MulticastKey(wrappedMulticastEK), new MulticastKey(wrappedMulticastPassword)));

            MulticastMeterConfig multicastMeterConfig = new MulticastMeterConfig(macAddress, this.extractor.serialNumber(slaveDevice), unicastSecurity, multicastSecurity);

            meterConfigs.add(multicastMeterConfig);
        }

        List<MulticastProperty> multicastProperties = new ArrayList<>(); //Note that the multicastProperties here are still empty. They are modelled as message attributes and will be used in the message executor.
        MulticastProtocolConfiguration protocolConfiguration = new MulticastProtocolConfiguration(0, 0, multicastProperties, meterConfigs);
        return this.serializer.jsonSerialize(protocolConfiguration);
    }

    private static DeviceMessageAttribute getDeviceMessageAttribute(DeviceMessage deviceMessage, String attributeName) {
        for (DeviceMessageAttribute deviceMessageAttribute : deviceMessage.getAttributes()) {
            if (deviceMessageAttribute.getName().equals(attributeName)) {
                return deviceMessageAttribute;
            }
        }
        return null;
    }

    /**
     * be aware that this method can be used only for object types on which the toString method will return the expected value. For other objects specific code must be written.
     *
     * @param deviceMessage
     * @param attributeName
     * @return
     */
    protected static String getDeviceMessageAttributeValueAsString(com.energyict.mdc.upl.messages.DeviceMessage deviceMessage, String attributeName) {
        DeviceMessageAttribute deviceMessageAttribute = getDeviceMessageAttribute(deviceMessage, attributeName);
        if (deviceMessageAttribute == null) {
            return null;
        }
        return deviceMessageAttribute.getValue().toString();
    }
}
