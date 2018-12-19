/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.getmeterconfig;

import ch.iec.tc57._2011.getmeterconfig.FaultMessage;
import ch.iec.tc57._2011.getmeterconfig.Meter;
import ch.iec.tc57._2011.getmeterconfig.Name;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class GetMeterConfigParser {
    private final GetMeterConfigFaultMessageFactory faultMessageFactory;

    @Inject
    public GetMeterConfigParser(GetMeterConfigFaultMessageFactory faultMessageFactory) {
        this.faultMessageFactory = faultMessageFactory;
    }

    public MeterInfo asMeterInfo(Meter meter) throws FaultMessage {
        MeterInfo meterInfo = new MeterInfo();
        meterInfo.setDeviceName(extractDeviceNameForGet(meter));
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

    public String extractDeviceNameForGet(Meter meter) throws FaultMessage {
        return extractName(meter.getNames())
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.GET_DEVICE_IDENTIFIER_MISSING));
    }

    private String getMeterName(Meter meter){
        return meter.getNames().stream().findFirst().map(Name::getName).orElse(null);
    }
}
