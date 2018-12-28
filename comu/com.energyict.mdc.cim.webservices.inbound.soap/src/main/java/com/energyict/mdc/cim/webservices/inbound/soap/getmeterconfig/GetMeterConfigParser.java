/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.getmeterconfig;

import ch.iec.tc57._2011.getmeterconfig.FaultMessage;
import ch.iec.tc57._2011.getmeterconfig.Meter;
import ch.iec.tc57._2011.getmeterconfig.Name;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import java.util.List;
import java.util.Optional;

public class GetMeterConfigParser {

    public GetMeterConfigParser() {
    }

    public MeterInfo asMeterInfo(Meter meter) throws FaultMessage {
        MeterInfo meterInfo = new MeterInfo();
        meterInfo.setDeviceName(extractName(meter.getNames()).orElse(null));
        meterInfo.setmRID(extractMrid(meter).orElse(null));
        return meterInfo;
    }

    public Optional<String> extractMrid(Meter meter) {
        return Optional.ofNullable(meter.getMRID())
                .filter(mrid -> !Checks.is(mrid).emptyOrOnlyWhiteSpace());
    }

    public Optional<String> extractName(List<Name> names) {
        return names.stream()
                .map(Name::getName)
                .filter(name -> !Checks.is(name).emptyOrOnlyWhiteSpace())
                .findFirst();
    }

}
