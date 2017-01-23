/*
 * SCTMRegisterSpec.java
 *
 * Created on 15 december 2004, 15:30
 */

package com.energyict.protocolimpl.sctm.base;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.obis.ObisCode;
/**
 *
 * @author  Koen
 */
public class SCTMRegisterSpec {

    int baseAddress;
    int subAddressOffset;
    int subAddressRange;
    ObisCode obisCode,obisCodeBase;
    String description;
    /** Creates a new instance of SCTMRegisterSpec */
    public SCTMRegisterSpec(int baseAddress, int subAddressOffset, ObisCode obisCode, String description) {
        this(baseAddress,subAddressOffset,1,obisCode,description);
    }

    public SCTMRegisterSpec(int baseAddress, int subAddressOffset, int subAddressRange, ObisCode obisCode,String description) {
        this.baseAddress=baseAddress;
        this.subAddressOffset=subAddressOffset;
        this.subAddressRange=subAddressRange;
        this.obisCode=obisCode;
        this.obisCodeBase = new ObisCode(obisCode.getA(),1,obisCode.getC(),obisCode.getD(),obisCode.getE(),obisCode.getF());
        this.description=description;
    }

    public boolean containsObisCode(ObisCode oc) {
        ObisCode ocBase = new ObisCode(oc.getA(),1,oc.getC(),oc.getD(),oc.getE(),oc.getF());
        // ObisCode match?
        if (obisCodeBase.equals(ocBase)) {
            // ObisCode channel within range?
            if ((oc.getB() < (obisCode.getB()+subAddressRange)) && (oc.getB() >= (obisCode.getB()))) {
                return true;
            }
        }
        return false;
    }

    public String getRegisterSpecAddress(ObisCode oc) {
        if (containsObisCode(oc)) {
            int subAddress=(oc.getB()-obisCode.getB())+subAddressOffset;
            return ProtocolUtils.buildStringDecimal(baseAddress,3)+ProtocolUtils.buildStringDecimal(subAddress,2);
        }
        return null;
    }

    public String getRegisterSpecAddressRange() {
        return ProtocolUtils.buildStringDecimal(baseAddress,3)+ProtocolUtils.buildStringDecimal(subAddressOffset,2)+
               (subAddressRange==1?"":".."+ProtocolUtils.buildStringDecimal(baseAddress,3)+ProtocolUtils.buildStringDecimal(subAddressOffset+subAddressRange-1,2));
    }

    /**
     * Getter for property description.
     * @return Value of property description.
     */
    public java.lang.String getDescription() {
        return description;
    }

    /**
     * Getter for property obisCode.
     * @return Value of property obisCode.
     */
    public ObisCode getObisCode() {
        return obisCode;
    }

    /**
     * Getter for property subAddressRange.
     * @return Value of property subAddressRange.
     */
    public int getSubAddressRange() {
        return subAddressRange;
    }



}
