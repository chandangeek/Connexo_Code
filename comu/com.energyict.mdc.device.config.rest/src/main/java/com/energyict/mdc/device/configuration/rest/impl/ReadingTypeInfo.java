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
import com.elster.jupiter.metering.ReadingType;
import java.util.Currency;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class ReadingTypeInfo {

    @JsonProperty("mrid")
    public String mrid;
    @JsonProperty("description")
    public String description;
    @JsonProperty("timePeriodOfInterest")
    @XmlJavaTypeAdapter(MacroPeriodAdapter.class)
    public MacroPeriod macroPeriod;
    @JsonProperty("dataQualifier")
    @XmlJavaTypeAdapter(AggregateAdapter.class)
    public Aggregate aggregate;
    @JsonProperty("timeAttributeEnumerations")
    @XmlJavaTypeAdapter(TimeAttributeAdapter.class)
    public TimeAttribute timeAttribute;
    @JsonProperty("accumulationBehaviour")
    @XmlJavaTypeAdapter(AccumulationAdapter.class)
    public Accumulation accumulation;
    @XmlJavaTypeAdapter(FlowDirectionAdapter.class)
    @JsonProperty("directionOfFlow")
    public FlowDirection flowDirection;
    @JsonProperty("commodity")
    @XmlJavaTypeAdapter(CommodityAdapter.class)
    public Commodity commodity;
    @JsonProperty("measurementKind")
    @XmlJavaTypeAdapter(MeasurementKindAdapter.class)
    public MeasurementKind measurementKind;
    @JsonProperty("interharmonics")
    @XmlJavaTypeAdapter(RationalNumberAdapter.class)
    public RationalNumber interharmonic;
    @JsonProperty("argumentReference")
    @XmlJavaTypeAdapter(RationalNumberAdapter.class)
    public RationalNumber argumentReference;
    @JsonProperty("timeOfUse")
    public Integer timeOfUse;
    @JsonProperty("criticalPeakPeriod")
    public Integer criticalPeakPeriod;
    @JsonProperty("consumptionTier")
    public Integer consumptionTier;
    @JsonProperty("phase")
    @XmlJavaTypeAdapter(PhaseAdapter.class)
    public Phase phase;
    @JsonProperty("powerOfTenMultiplier")
    @XmlJavaTypeAdapter(MetricMultiplierAdapter.class)
    MetricMultiplier metricMultiplier;
    @JsonProperty("unitOfMeasure")
    @XmlJavaTypeAdapter(ReadingTypeUnitAdapter.class)
    ReadingTypeUnit readingTypeUnit;
    @JsonProperty("currency")
    @XmlJavaTypeAdapter(CurrencyAdapter.class)
    public Currency currency;

    public ReadingTypeInfo() {
    }

    public ReadingTypeInfo(ReadingType readingType) {
        mrid = readingType.getMRID();
        description = readingType.getDescription();
        macroPeriod = readingType.getMacroPeriod();
        aggregate = readingType.getAggregate();
        timeAttribute = readingType.getMeasuringPeriod();
        accumulation = readingType.getAccumulation();
        flowDirection = readingType.getFlowDirection();
        commodity = readingType.getCommodity();
        measurementKind = readingType.getMeasurementKind();
        interharmonic = readingType.getInterharmonic();
        argumentReference = readingType.getArgument();

        timeOfUse = readingType.getTou();
        criticalPeakPeriod = readingType.getCpp();
        consumptionTier = readingType.getConsumptionTier();
        phase = readingType.getPhases();
        metricMultiplier = readingType.getMultiplier();
        readingTypeUnit = readingType.getUnit();
        currency = readingType.getCurrency();
    }
}
