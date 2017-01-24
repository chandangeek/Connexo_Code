package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Straightforward summation of the {@link com.energyict.dlms.cosem.IPv4Setup} attributes
 *
 * Copyrights EnergyICT
 * Date: 8-jul-2010
 * Time: 10:28:52
 * </p>
 */
public enum MacAddressSetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1),
	MAC_ADDRESS(2);

    private final int attributeNumber;
    private final int shortName;

    /**
     * Constructor for the attribute types
     * @param lnAttribute the LONG_NAME attribute number
     */
    private MacAddressSetupAttributes(int lnAttribute){
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
        return DLMSClassId.MAC_ADDRESS_SETUP;
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
