/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.Commodity;
import com.energyict.mdc.common.ObisCode;

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
