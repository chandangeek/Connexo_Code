package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.time.Instant;

@XmlRootElement
public class MeterActivationInfo {

    public long id;
    public long version;
    public boolean active;
    public Instant start;
    public Instant end;
    public BigDecimal multiplier;
    public IdWithNameInfo usagePoint;
    public IdWithNameInfo deviceConfiguration;

}

