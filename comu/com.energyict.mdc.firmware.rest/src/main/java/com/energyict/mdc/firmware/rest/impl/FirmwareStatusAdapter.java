package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.firmware.FirmwareStatus;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class FirmwareStatusAdapter extends XmlAdapter<String, FirmwareStatus> {
    @Override
    public FirmwareStatus unmarshal(String jsonValue) throws Exception {
        if (Checks.is(jsonValue).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return FirmwareStatus.from(jsonValue);
    }

    @Override
    public String marshal(FirmwareStatus status) throws Exception {
        if (status==null) {
            return null;
        }
        return status.getStatus();
    }
}
