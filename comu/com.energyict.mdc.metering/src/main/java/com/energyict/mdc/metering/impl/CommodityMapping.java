package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.Commodity;
import com.energyict.obis.ObisCode;

/**
 * Defines a mapping between the DLMS ObisCode and a CIM Commodity
 *
 * Copyrights EnergyICT
 * Date: 26/11/13
 * Time: 12:22
 */
enum CommodityMapping {

    ELECTRICITY(1, Commodity.ELECTRICITY_SECONDARY_METERED),
    GAS(7, Commodity.NATURALGAS)
    ;

    private final int obisCodeAField;
    private final Commodity commodity;

    CommodityMapping(int obisCodeAField, Commodity commodity) {
        this.obisCodeAField = obisCodeAField;
        this.commodity = commodity;
    }


    /**
     * Finds the appropriate Commodity for the given ObisCode.
     * If no applicable is found, then {@link Commodity#NOTAPPLICABLE} is returned.
     *
     * @param obisCode the given obisCode
     * @return the applicable Commodity
     */
    public static Commodity getCommodityFor(ObisCode obisCode) {
        for (CommodityMapping commodityMapper : values()) {
            if(commodityMapper.obisCodeAField == obisCode.getA()){
                return commodityMapper.commodity;
            }
        }
        return Commodity.NOTAPPLICABLE;
    }

    Commodity getCommodity() {
        return commodity;
    }

    int getObisCodeAField() {
        return obisCodeAField;
    }
}
