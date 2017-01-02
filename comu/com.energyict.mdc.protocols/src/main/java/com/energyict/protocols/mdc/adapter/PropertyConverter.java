package com.energyict.protocols.mdc.adapter;

import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HexString;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 14/12/2016 - 15:12
 */
public class PropertyConverter implements Converter {

    @Override
    public HexString hexFromString(String value) {
        return new com.energyict.mdc.common.HexString(value);
    }
}