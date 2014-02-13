package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.RegisterMapping;
import java.util.Currency;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonUnwrapped;

@XmlRootElement
public class RegisterMappingInfo {

    @JsonProperty("id")
    public long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("obisCode")
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @JsonUnwrapped
    public ReadingTypeInfo readingTypeInfo;

    public RegisterMappingInfo() {
    }

    public RegisterMappingInfo(RegisterMapping registerMapping) {
        id = registerMapping.getId();
        name = registerMapping.getName();
        obisCode = registerMapping.getObisCode();
//        readingTypeInfo = new ReadingTypeInfo(registerMapping.getReadingType());
        readingTypeInfo = new ReadingTypeInfo(); // TODO read from RegisterMapping
        readingTypeInfo.mrid = "10.2.5.3.2.8.1.1.12.1.0.255.6.65.978.0.0.0";
        readingTypeInfo.readingTypeUnit = ReadingTypeUnit.CUBICFEET;
        readingTypeInfo.metricMultiplier = MetricMultiplier.EXA;
        readingTypeInfo.phase = Phase.PHASEB;
        readingTypeInfo.consumptionTier = 2;
        readingTypeInfo.accumulation = Accumulation.CUMULATIVE;
        readingTypeInfo.aggregate = Aggregate.FOURTHMAXIMUM;
        readingTypeInfo.argumentReference = new RationalNumber(1L, 11L);
        readingTypeInfo.commodity = Commodity.CO2;
        readingTypeInfo.criticalPeakPeriod = 7;
        readingTypeInfo.flowDirection = FlowDirection.FORWARD;
        readingTypeInfo.currency = Currency.getInstance("EUR");
        readingTypeInfo.interharmonic = new RationalNumber(101L, 16546575421321L);
        readingTypeInfo.macroPeriod = MacroPeriod.DAILY;
        readingTypeInfo.measurementKind = MeasurementKind.ALARM;
        readingTypeInfo.timeAttribute = TimeAttribute.MINUTE15;
        readingTypeInfo.timeOfUse = 13;
    }
}
