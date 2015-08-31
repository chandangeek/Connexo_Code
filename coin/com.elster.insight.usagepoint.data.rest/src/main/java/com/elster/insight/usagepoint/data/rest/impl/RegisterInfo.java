package com.elster.insight.usagepoint.data.rest.impl;

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
//@JsonSubTypes({
//        @JsonSubTypes.Type(value = BillingRegisterInfo.class, name = "billing"),
//        @JsonSubTypes.Type(value = NumericalRegisterInfo.class, name = "numerical"),
//        @JsonSubTypes.Type(value = TextRegisterInfo.class, name = "text"),
//        @JsonSubTypes.Type(value = FlagsRegisterInfo.class, name = "flags")
//})
public class RegisterInfo {
    @JsonProperty("id")
    public Long id;
    @JsonProperty("readingType")
    public ReadingTypeInfo readingType;
    @JsonProperty("unitOfMeasure")
    public String unitOfMeasure;
    @JsonProperty("timeOfUse")
    public Integer timeOfUse;
//    @JsonProperty("lastReading")
//    public ReadingInfo lastReading;
    @JsonProperty("lastValueTimestamp")
    public Instant lastValueTimestamp;
    public boolean isCumulative;

    public RegisterInfo() {}

    public static RegisterInfo from(Channel channel) {
        RegisterInfo info = new RegisterInfo();
        info.id = channel.getId();
        info.readingType = new ReadingTypeInfo(channel.getMainReadingType());
        
//        info.name = channel.getName();
//        info.interval = new TimeDurationInfo(channel.getInterval());
        
        info.unitOfMeasure = channel.getMainReadingType().getUnit().toString();
        info.timeOfUse = channel.getMainReadingType().getTou();
        
//        info.lastReading = channel.getLastReading().orElse(null);
        info.lastValueTimestamp = channel.getLastDateTime();
        info.isCumulative = channel.getMainReadingType().isCumulative();
        
        
//        if (channel.getMainReadingType().isCumulative()) {
//            channel.getMainReadingType().getCalculatedReadingType().ifPresent(
//                    rt -> info.calculatedReadingType = new ReadingTypeInfo(rt));
//        }
        return info;
    }
    
    
}