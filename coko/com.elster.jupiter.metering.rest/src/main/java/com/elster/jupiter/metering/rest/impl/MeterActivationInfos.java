package com.elster.jupiter.metering.rest.impl;

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
        add(meterActivation);
    }

    MeterActivationInfos(Iterable<? extends MeterActivation> meterActivations) {
        addAll(meterActivations);
    }

    MeterActivationInfo add(MeterActivation meterActivation) {
        MeterActivationInfo result = new MeterActivationInfo(meterActivation);
        meterActivations.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends MeterActivation> meterActivations) {
        for (MeterActivation each : meterActivations) {
            add(each);
        }
    }
}