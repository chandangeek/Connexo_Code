package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.IPv4Setup;

import java.io.IOException;

/**
 * Created by cisac on 1/6/2016.
 */
public class IPv4SetupAttributesMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 10;

    public IPv4SetupAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return IPv4Setup.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getE() >= MIN_ATTR) &&
                (obisCode.getE() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final IPv4Setup iPv4Setup = getCosemObjectFactory().getIPv4Setup(obisCode);
        return parse(obisCode, readAttribute(obisCode, iPv4Setup));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, IPv4Setup iPv4Setup) throws IOException {
        switch (obisCode.getE()) {
            // Logical name
            case 1:
                return OctetString.fromObisCode(IPv4Setup.getDefaultObisCode());
            case 2:
                return iPv4Setup.getDLReference();
            case 3:
                return iPv4Setup.readIPAddress();
            case 4:
                return iPv4Setup.readMulticastIPAddress();
            case 5:
                return iPv4Setup.readIPOptions();
            case 6:
                return iPv4Setup.readSubnetMask();
            case 7:
                return iPv4Setup.readGatewayIPAddress();
            case 8:
                return iPv4Setup.readDHCPFlag();
            case 9:
                return iPv4Setup.readPrimaryDNSAddress();
            case 10:
                return iPv4Setup.readSecondaryDNSAddress();
            default:
                throw new NoSuchRegisterException("BeaconEventPushNotificationConfig attribute [" + obisCode.getB() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getE()) {

            // Logical name
            case 1:
                return new RegisterValue(obisCode, IPv4Setup.getDefaultObisCode().toString());
            case 2:
                return new RegisterValue(obisCode, "DL reference: " + abstractDataType.getOctetString().stringValue());
            case 3:
                return new RegisterValue(obisCode, "IP address: " + getIPAddressString(abstractDataType));
            case 4:
                return new RegisterValue(obisCode, "Multicast IP address: " + abstractDataType.getArray());
            case 5:
                return new RegisterValue(obisCode, "IP options: " + abstractDataType.getArray());
            case 6:
                return new RegisterValue(obisCode, "Subnet mask: " + getIPAddressString(abstractDataType));
            case 7:
                return new RegisterValue(obisCode, "Gateway IP address: " + getIPAddressString(abstractDataType));
            case 8:
                return new RegisterValue(obisCode, "Use DHCP flag: " + abstractDataType.getBooleanObject());
            case 9:
                return new RegisterValue(obisCode, "Primary DNS address: " + getIPAddressString(abstractDataType));
            case 10:
                return new RegisterValue(obisCode, "Secondary DNS address: " + getIPAddressString(abstractDataType));
            default:
                throw new NoSuchRegisterException("IPv4Setup attribute [" + obisCode.getE() + "] not supported!");

        }
    }

    public String getIPAddressString(AbstractDataType abstractDataType) throws IOException {
        StringBuffer builder = new StringBuffer();
        Unsigned32 ipAddress = abstractDataType.getUnsigned32();
        if (ipAddress != null) {
            for (int i = 1; i < ipAddress.getBEREncodedByteArray().length; i++) {
                if (i != 1) {
                    builder.append("");
                }
                builder.append(Integer.toString(ipAddress.getBEREncodedByteArray()[i] & 0xff));
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get a correct IP-address format");
        }
    }
}
