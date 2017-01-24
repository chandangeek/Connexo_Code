package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * <p>
 *
 * Straightforward summation of the {@link com.energyict.dlms.cosem.IPv4Setup} attributes
 *
 * Copyrights EnergyICT
 * Date: 8-jul-2010
 * Time: 10:28:52
 * </p>
 */
public enum Ipv4SetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1),
	DL_REFERENCE(2),
	IP_ADDRESS(3),
	MULTICAST_IP_ADDRESS(4),
	IP_OPTIONS(5),
	SUBNET_MASK(6),
	GATEWAY_IP_ADDRESS(7),
	USE_DHCP_FLAG(8),
	PRIMARY_DNS_ADDRESS(9),
	SECONDARY_DNS_ADDRESS(10);

    private final int attributeNumber;
    private final int shortName;

    /**
     * Constructor for the attribute types
     * @param lnAttribute the LONG_NAME attribute number
     */
    private Ipv4SetupAttributes(int lnAttribute){
        this.attributeNumber = lnAttribute;
        this.shortName = (this.attributeNumber - 1) *8;
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return this.attributeNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.IPV4_SETUP;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

}
