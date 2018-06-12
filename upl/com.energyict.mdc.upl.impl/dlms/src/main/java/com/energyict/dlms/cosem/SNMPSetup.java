package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.SNMPAttributes;
import com.energyict.dlms.cosem.methods.SNMPSetupMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * class id = 20032, version = 0, logical name = 0-128:96.194.0.255 (008060C200FF)
 * The manufacturer-specific SNMP setup IC allows configuring the SNMP agent running on the device.
 * This includes both upstream trap eventing and MIB querying.
 * Changes are applied after closing the DLMS association, since these force the SNMP agent to restart.
 * Created by cisac on 11/21/2016.
 */
public class SNMPSetup extends AbstractCosemObject {

    public static final ObisCode OLD_OBIS_CODE = ObisCode.fromString("0.17.128.0.0.255");

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.128.96.194.0.255");

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public SNMPSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.SNMP_SETUP.getClassId();
    }

    /**
     * Array of interfaces on which the SNMP agent is available. Array of interface_enum.
     * interface_enum ::= enum:
     * (0) all,
     * (1) ethernet_wan,
     * (2) ethernet_lan,
     * (3) wireless_wan,
     * (4) ip6_tunnel,
     * (5) plc_network
     *
     * @return
     * @throws IOException
     */
    public Array readEnabledInterfaces() throws IOException {
        return readDataType(SNMPAttributes.ENABLED_INTERFACES, Array.class);
    }

    /**
     * Array of SNMP users with their current configuration.
     * snmp_user_info ::= structure {
     * user_profile snmp_user_enum, -- SNMP user profile
     * user_name utf8-string, -- securityName of the user
     * user_state boolean -- True if user profile is enabled
     * }
     * snmp_user_enum ::= enum:
     * (0) public,
     * (1) read_only,
     * (2) read_write,
     * (3) management
     *
     * @return
     * @throws IOException
     */
    public Array readUsers() throws IOException {
        return readDataType(SNMPAttributes.USERS, Array.class);
    }

    /**
     * sysContact value in the MIB-2 system subtree
     *
     * @return
     * @throws IOException
     */
    public UTF8String readSystemContact() throws IOException {
        return readDataType(SNMPAttributes.SYSTEM_CONTACT, UTF8String.class);
    }

    /**
     * sysContact value in the MIB-2 system subtree
     *
     * @return
     * @throws IOException
     */
    public UTF8String readSystemLocation() throws IOException {
        return readDataType(SNMPAttributes.SYSTEM_LOCATION, UTF8String.class);
    }

    /**
     * SNMP engine ID to be used for this SNMP agent. Default to a value derived from the DLMS
     * system title.
     *
     * @return
     * @throws IOException
     */
    public OctetString readLocalEngineId() throws IOException {
        return readDataType(SNMPAttributes.LOCAL_ENGINE_ID, OctetString.class);
    }

    /**
     * Type of SNMP notifications to be dispatched.
     * snmp_notification_type ::= enum:
     * (0) none, -- Drop notifications
     * (1) trap, -- Send notifications upstream as SNMPv3 trap messages
     * (2) inform -- Send notifications upstream as SNMPv3 inform messages
     *
     * @return
     * @throws IOException
     */
    public TypeEnum readNotificationType() throws IOException {
        return readDataType(SNMPAttributes.NOTIFICATION_TYPE, TypeEnum.class);
    }

    /**
     * User profile which is applied to outgoing SNMP notifications.
     * @return
     * @throws IOException
     */
    public TypeEnum readNotificationUser() throws IOException {
        return readDataType(SNMPAttributes.NOTIFICATION_USER, TypeEnum.class);
    }

    /**
     * Destination host for SNMP notifications.
     * @return
     * @throws IOException
     */
    public OctetString readNotificationHost() throws IOException {
        return readDataType(SNMPAttributes.NOTIFICATION_HOST, OctetString.class);
    }

    /**
     * Destination port for SNMP notifications. Defaults to 162.
     * @return
     * @throws IOException
     */
    public Unsigned16 readNotificationPort() throws IOException {
        return readDataType(SNMPAttributes.NOTIFICATION_PORT, Unsigned16.class);
    }

    public void enableInterfaces(Array interfacesArray) throws IOException {
        write(SNMPAttributes.ENABLED_INTERFACES, interfacesArray.getBEREncodedByteArray());
    }

    public void writeAttribute(SNMPAttributes attribute, AbstractDataType data) throws IOException {
        write(attribute, data);
    }

    public void invokeSNMPMethod(SNMPSetupMethods snmpSetupMethod, AbstractDataType data) throws IOException {
        methodInvoke(snmpSetupMethod, data);
    }


}
