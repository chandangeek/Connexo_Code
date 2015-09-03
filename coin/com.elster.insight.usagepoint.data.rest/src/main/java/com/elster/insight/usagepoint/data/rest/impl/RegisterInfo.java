package com.elster.insight.usagepoint.data.rest.impl;

import java.math.BigDecimal;
import java.time.Instant;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.elster.insight.common.rest.UnitAdapter;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.impl.ReadingInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public class RegisterInfo {
    @JsonProperty("id")
    public Long id;
    @JsonProperty("readingType")
    public ReadingTypeInfo readingType;
    @JsonProperty("unitOfMeasure")
    public String unitOfMeasure;
    @JsonProperty("timeOfUse")
    public Integer timeOfUse;
    @JsonProperty("lastValueTimestamp")
    public Instant lastValueTimestamp;
    public BigDecimal lastReadingValue;
    public boolean isCumulative;

    public RegisterInfo() {}

    public static RegisterInfo from(Channel channel) {
        RegisterInfo info = new RegisterInfo();
        info.id = channel.getId();
        info.readingType = new ReadingTypeInfo(channel.getMainReadingType());
        info.unitOfMeasure = channel.getMainReadingType().getMultiplier().getSymbol() + channel.getMainReadingType().getUnit().getSymbol();
        info.timeOfUse = channel.getMainReadingType().getTou();
        info.lastValueTimestamp = channel.getLastDateTime();
        info.lastReadingValue = channel.getReading(channel.getLastDateTime()).get().getValue();
        info.isCumulative = channel.getMainReadingType().isCumulative();
        return info;
    }
    
    
}