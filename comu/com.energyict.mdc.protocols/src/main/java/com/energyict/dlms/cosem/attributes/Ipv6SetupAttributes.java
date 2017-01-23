package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Straightforward summation of the IPv6Setup attributes
 *
 * Copyrights EnergyICT
 * Date: 1/6/15
 * Time: 8:30 AM
 */
public enum Ipv6SetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1),
    DL_REFERENCE(2),
    ADDRESS_CONFIG_MODE(3),
    UNICAST_IPV6_ADDRESSES(4),
    MULTICAST_IPV6_ADDRESSES(5),
    GATEWAY_IPV6_ADDRESSES(6),
    PRIMARY_DNS_ADDRESS(7),
    SECONDARY_DNS_ADDRESS(8),
    TRAFFIC_CLASS(9),
    NEIGHBOUR_DISCOVERY_SETUP(10),
    ;

    private final int attributeNumber;
    private final int shortName;

    Ipv6SetupAttributes(int attributeNumber) {
        this.attributeNumber = attributeNumber;
        this.shortName = (this.attributeNumber - 1) *8;
    }

    @Override
    public int getAttributeNumber() {
        return this.attributeNumber;
    }

    @Override
    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

    @Override
    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.IPV6_SETUP;
    }

    @Override
    public int getShortName() {
        return this.shortName;
    }
}
