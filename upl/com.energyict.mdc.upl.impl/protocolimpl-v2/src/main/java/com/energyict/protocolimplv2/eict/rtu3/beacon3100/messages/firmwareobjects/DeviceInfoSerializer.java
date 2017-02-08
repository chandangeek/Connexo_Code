package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.firmwareobjects;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.upl.DeviceGroupExtractor;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.ObjectMapperService;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.properties.DeviceGroup;
import com.energyict.mdc.upl.security.SecurityProperty;
import com.energyict.mdc.upl.security.SecurityPropertySet;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocol.exception.ProtocolRuntimeException;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.energyict.mdc.upl.DeviceProtocolDialect.Property.DEVICE_PROTOCOL_DIALECT;

/**
 * Helper class that takes a group of slave devices and returns a JSon serialized version of all their properties.
 * The result can then be used by the message executor.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/08/2015 - 15:37
 */
public class DeviceInfoSerializer {

    private final DeviceMasterDataExtractor deviceMasterDataExtractor;
    private final DeviceGroupExtractor deviceGroupExtractor;
    private final ObjectMapperService objectMapperService;
    private DeviceGroup deviceGroup;

    public DeviceInfoSerializer(DeviceMasterDataExtractor deviceMasterDataExtractor, DeviceGroupExtractor deviceGroupExtractor, ObjectMapperService objectMapperService) {
        this.deviceMasterDataExtractor = deviceMasterDataExtractor;
        this.deviceGroupExtractor = deviceGroupExtractor;
        this.objectMapperService = objectMapperService;
    }

    public String serializeDeviceInfo(Object messageAttribute) {
        return this.serializeDeviceInfo((DeviceGroup) messageAttribute);
    }

    public String serializeDeviceInfo(DeviceGroup deviceGroup) {
        this.deviceGroup = deviceGroup;
        DeviceInfo[] deviceInfos =
                this.deviceGroupExtractor
                    .members(deviceGroup)
                    .stream()
                    .map(this::toDeviceInfo)
                    .toArray(DeviceInfo[]::new);
        return jsonSerialize(deviceInfos);
    }

    private DeviceInfo toDeviceInfo(Device slaveDevice) {
        //First validate that all members of the group are AM540 devices, and have an Beacon3100 gateway configured.
        final Device beacon3100 =
                this.deviceMasterDataExtractor
                        .gateway(slaveDevice)
                        .filter(gateway -> this.deviceMasterDataExtractor.protocolJavaClassName(gateway).equals(Beacon3100.class.getName()))
                        .orElseThrow(() -> invalidFormatException("'Group of devices to upgrade'", "Group with ID " + this.deviceGroupExtractor.id(this.deviceGroup), "Group members must have a gateway that has DeviceProtocol '" + Beacon3100.class.getName() + "'"));

        if (!this.deviceMasterDataExtractor.protocolJavaClassName(slaveDevice).equals(AM540.class.getName())) {
            throw invalidFormatException("'Group of devices to upgrade'", "Group with ID " + this.deviceGroupExtractor.id(this.deviceGroup), "Group members must DeviceProtocol '" + AM540.class.getName() + "'");
        }

        //Serialize all necessary device properties, some of them are not included in OfflineDevice
        final TypedProperties generalProperties = TypedProperties.empty();
        generalProperties.setAllProperties(this.deviceMasterDataExtractor.properties(slaveDevice));
        generalProperties.setAllProperties(this.deviceMasterDataExtractor.protocolProperties(slaveDevice));
        generalProperties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName(), this.deviceMasterDataExtractor.serialNumber(slaveDevice));
        generalProperties.setProperty(DlmsProtocolProperties.READCACHE_PROPERTY, false);

        //Get the dialect properties, from the Beacon3100 gateway device (configured in its connection task)
        final TypedProperties dialectProperties =
                this.deviceMasterDataExtractor
                        .dialectProperties(beacon3100, DeviceProtocolDialectNameEnum.BEACON_GATEWAY_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName())
                        .map(TypedProperties::copyOf)
                        .orElseGet(TypedProperties::empty);

        //Add the name of the gateway dialect in the properties, need to communicate directly to the device, using the gateway
        dialectProperties.setProperty(DEVICE_PROTOCOL_DIALECT.getName(), DeviceProtocolDialectNameEnum.BEACON_GATEWAY_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName());

        //Add the first security set. Any security set is fine, as long as it can be used to create a unicast session to the meter.
        final Iterator<DeviceMasterDataExtractor.SecurityPropertySet> securityPropertySets = this.deviceMasterDataExtractor.securityPropertySets(slaveDevice).iterator();
        Collection<DeviceMasterDataExtractor.SecurityProperty> protocolSecurityProperties = new ArrayList<>();
        if (securityPropertySets.hasNext()) {
            protocolSecurityProperties = this.deviceMasterDataExtractor.securityProperties(slaveDevice, securityPropertySets.next());
        }
        return new DeviceInfo(generalProperties, dialectProperties, this.toUpl(protocolSecurityProperties), this.deviceMasterDataExtractor.id(slaveDevice));
    }

    private List<SecurityProperty> toUpl(Collection<DeviceMasterDataExtractor.SecurityProperty> properties) {
        return properties
                .stream()
                .map(SecurityPropertyAdapter::new)
                .collect(Collectors.toList());
    }

    private String jsonSerialize(Object object) {
        ObjectMapper mapper = this.objectMapperService.newJacksonMapper();
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, object);
        } catch (IOException e) {
            throw DataParseException.generalParseException(e);
        }
        return writer.toString();
    }

    private static ProtocolRuntimeException invalidFormatException(String propertyName, String propertyValue, String message) {
        return DeviceConfigurationException.invalidPropertyFormat(propertyName, propertyValue, message);
    }

    private static class SecurityPropertyAdapter implements SecurityProperty {
        final DeviceMasterDataExtractor.SecurityProperty actual;

        private SecurityPropertyAdapter(DeviceMasterDataExtractor.SecurityProperty actual) {
            this.actual = actual;
        }

        @Override
        public String getName() {
            return this.actual.name();
        }

        @Override
        public Object getValue() {
            return this.actual.value();
        }

        @Override
        public SecurityPropertySet getSecurityPropertySet() {
            throw new UnsupportedOperationException("Getting SecurityPropertySet during serialization to DeviceInfo class is not supported");
        }
    }

}