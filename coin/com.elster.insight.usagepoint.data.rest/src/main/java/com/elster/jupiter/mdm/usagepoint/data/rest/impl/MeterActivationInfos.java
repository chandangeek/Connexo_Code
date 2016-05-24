package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MeterActivation;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class MeterActivationInfos {
    public int total;
    public List<MeterActivationInfo> meterActivations = new ArrayList<>();

    MeterActivationInfos() {
    }

    MeterActivationInfos(MeterActivation meterActivation) {
        add(meterActivation, true);
    }

    MeterActivationInfos(Iterable<? extends MeterActivation> meterActivations) {
        addAll(meterActivations, true);
    }

    MeterActivationInfos(Iterable<? extends MeterActivation> meterActivations, boolean includeMeterInfo) {
        addAll(meterActivations, includeMeterInfo);
    }

    MeterActivationInfo add(MeterActivation meterActivation, boolean includeMeterInfo) {
        MeterActivationInfo result = new MeterActivationInfo(meterActivation, includeMeterInfo);
        meterActivations.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends MeterActivation> meterActivations, boolean includeMeterInfo) {
        for (MeterActivation each : meterActivations) {
            add(each, includeMeterInfo);
        }
    }
}