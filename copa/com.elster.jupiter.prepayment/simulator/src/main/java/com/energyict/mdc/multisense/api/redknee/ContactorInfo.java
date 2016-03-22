package com.energyict.mdc.multisense.api.redknee;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Created by bvn on 9/18/15.
 */
public class ContactorInfo {

    public Status status;
    public LoadLimit loadLimit;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Instant activationDate;
    public Integer loadTolerance;
    public Integer[] tariffs;
    public String readingType;
    public String callback;

    public class LoadLimit {
        public BigDecimal limit;
        public String unit;
    }
}
