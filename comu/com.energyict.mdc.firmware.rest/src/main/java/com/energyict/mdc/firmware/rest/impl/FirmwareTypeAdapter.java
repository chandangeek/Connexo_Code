package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.firmware.FirmwareType;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class FirmwareTypeAdapter extends XmlAdapter<String, FirmwareType> {
    @Override
    public FirmwareType unmarshal(String jsonValue) throws Exception {
        if (Checks.is(jsonValue).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return FirmwareType.from(jsonValue);
    }

    @Override
    public String marshal(FirmwareType type) throws Exception {
        if (type==null) {
            return null;
        }
        return type.getType();
    }
}
