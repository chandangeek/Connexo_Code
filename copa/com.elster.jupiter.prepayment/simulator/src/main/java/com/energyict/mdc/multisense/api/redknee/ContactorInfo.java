package com.energyict.mdc.multisense.api.redknee;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Created by bvn on 9/18/15.
 */
public class ContactorInfo {

    public Status status;
    public BigDecimal loadLimit;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Instant activationDate;
    public Integer loadTolerance;
    public Integer[] tariffs;
    public String readingType;
    public String callback;
}
