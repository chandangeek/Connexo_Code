package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;

public class OutputRegisterDataInfo {

    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;

    public Instant timeStamp;

}
