package com.energyict.mdc.engine.impl.core.remote;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.interval.Temporals;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.temporal.TemporalAmount;

public class TemporalAmountXmlAdapter extends XmlAdapter<Integer, TemporalAmount> {

    @Override
    public TemporalAmount unmarshal(Integer v) throws Exception {
        return Temporals.toTemporalAmount(new TimeDuration(v));
    }

    @Override
    public Integer marshal(TemporalAmount v) throws Exception {
        return Temporals.toTimeDuration(v).getSeconds();
    }
}
