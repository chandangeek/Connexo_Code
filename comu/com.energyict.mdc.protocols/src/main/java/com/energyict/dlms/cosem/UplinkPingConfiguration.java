package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.attributes.UplinkPingConfigurationAttributes;
import com.energyict.dlms.cosem.methods.UplinkPingConfigurationMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 9/27/12
 * Time: 10:51 AM
 */
public class UplinkPingConfiguration extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.128.0.7.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public UplinkPingConfiguration(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.UPLINK_PING_SETUP.getClassId();
    }

    public void enableUplinkPing(boolean enable) throws IOException {
        if (enable) {
            methodInvoke(UplinkPingConfigurationMethods.ENABLE, new NullData().getBEREncodedByteArray());
        } else {
            methodInvoke(UplinkPingConfigurationMethods.DISABLE, new NullData().getBEREncodedByteArray());
        }
    }

    public AbstractDataType readUplinkPingEnabled() throws IOException {
        return readDataType(UplinkPingConfigurationAttributes.ENABLE, BooleanObject.class);
    }

    public void writeDestAddress(String destAddress) throws IOException {
        write(UplinkPingConfigurationAttributes.DEST_ADDRESS, OctetString.fromString(destAddress));
    }

    public AbstractDataType readDestAddress() throws IOException {
        return readDataType(UplinkPingConfigurationAttributes.DEST_ADDRESS, OctetString.class);
    }

    public void writeTimeout(int timeout) throws IOException {
        write(UplinkPingConfigurationAttributes.TIMEOUT, new Unsigned32(timeout));
    }

    public AbstractDataType readTimeout() throws IOException {
        return readDataType(UplinkPingConfigurationAttributes.TIMEOUT);
    }

    public void writeInterval(int interval) throws IOException {
        write(UplinkPingConfigurationAttributes.INTERVAL, new Unsigned32(interval));
    }

    public AbstractDataType readInterval() throws IOException {
        return readDataType(UplinkPingConfigurationAttributes.INTERVAL);
    }
}