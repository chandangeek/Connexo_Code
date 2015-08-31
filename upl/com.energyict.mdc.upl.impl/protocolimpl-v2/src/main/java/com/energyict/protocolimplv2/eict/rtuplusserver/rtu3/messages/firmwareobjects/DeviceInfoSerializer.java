package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.firmwareobjects;

import com.energyict.cpo.BusinessObject;
import com.energyict.cpo.ObjectMapperFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.mdc.protocol.security.SecurityProperty;
import com.energyict.mdc.protocol.security.SecurityPropertySet;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.ProtocolDialectProperties;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.Group;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.RTU3;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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

    public static String serializeDeviceInfo(Object messageAttribute) {
        final List<? extends BusinessObject> businessObjects = ((Group) messageAttribute).getMembers();
        DeviceInfo[] deviceInfos = new DeviceInfo[businessObjects.size()];

        for (int index = 0; index < businessObjects.size(); index++) {
            final BusinessObject businessObject = businessObjects.get(index);

            if (businessObject instanceof Device) {
                final Device slaveDevice = (Device) businessObject;

                //First validate that all members of the group are AM540 devices, and have an RTU3 gateway configured.
                final Device rtu3 = slaveDevice.getGateway();
                if (rtu3 == null || !rtu3.getDeviceProtocolPluggableClass().getJavaClassName().equals(RTU3.class.getName())) {
                    throw invalidFormatException("'Group of devices to upgrade'", "Group with ID " + ((Group) messageAttribute).getId(), "Group members must have a gateway that has DeviceProtocol '" + RTU3.class.getName() + "'");
                }

                if (!slaveDevice.getDeviceProtocolPluggableClass().getJavaClassName().equals(AM540.class.getName())) {
                    throw invalidFormatException("'Group of devices to upgrade'", "Group with ID " + ((Group) messageAttribute).getId(), "Group members must DeviceProtocol '" + AM540.class.getName() + "'");
                }

                //Serialize all necessary device properties, some of them are not included in OfflineDevice
                final TypedProperties generalProperties = TypedProperties.empty();
                generalProperties.setAllProperties(slaveDevice.getProperties());
                generalProperties.setAllProperties(slaveDevice.getProtocolProperties());
                generalProperties.setProperty(MeterProtocol.SERIALNUMBER, slaveDevice.getSerialNumber());
                generalProperties.setProperty(DlmsProtocolProperties.READCACHE_PROPERTY, false);

                //Get the dialect properties, from the RTU3 gateway device (configured in its connection task)
                final TypedProperties dialectProperties = TypedProperties.empty();
                final List<ProtocolDialectProperties> allProtocolDialectProperties = rtu3.getAllProtocolDialectProperties();

                //Look for the dialect with name 'GatewayTcpDlmsDialect'
                for (ProtocolDialectProperties protocolDialectProperties : allProtocolDialectProperties) {
                    if (protocolDialectProperties.getProtocolDialectConfigurationProperties().getDeviceProtocolDialectName().equals(DeviceProtocolDialectNameEnum.BEACON_GATEWAY_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName())) {
                        dialectProperties.setAllProperties(protocolDialectProperties.getTypedProperties());
                        break;
                    }
                }

                //Add the name of the gateway dialect in the properties, need to communicate directly to the device, using the gateway
                dialectProperties.setProperty(DeviceProtocolDialect.DEVICE_PROTOCOL_DIALECT_NAME, DeviceProtocolDialectNameEnum.BEACON_GATEWAY_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName());

                //Add the first security set. Any security set is fine, as long as it can be used to create a unicast session to the meter.
                final List<SecurityPropertySet> securityPropertySets = slaveDevice.getConfiguration().getCommunicationConfiguration().getSecurityPropertySets();
                List<SecurityProperty> protocolSecurityProperties = new ArrayList<>();
                if (!securityPropertySets.isEmpty()) {
                    protocolSecurityProperties = slaveDevice.getProtocolSecurityProperties(securityPropertySets.get(0));
                }

                deviceInfos[index] = new DeviceInfo(generalProperties, dialectProperties, protocolSecurityProperties, slaveDevice.getId());
            } else {
                throw invalidFormatException("'Group of devices to upgrade'", "Group with ID " + ((Group) messageAttribute).getId(), "Group should only contain members of type 'Device'");
            }
        }

        return jsonSerialize(deviceInfos);
    }

    private static String jsonSerialize(Object object) {
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, object);
        } catch (IOException e) {
            throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
        }
        return writer.toString();
    }

    private static ComServerExecutionException invalidFormatException(String propertyName, String propertyValue, String message) {
        return MdcManager.getComServerExceptionFactory().createInvalidPropertyFormatException(propertyName, propertyValue, message);
    }
}