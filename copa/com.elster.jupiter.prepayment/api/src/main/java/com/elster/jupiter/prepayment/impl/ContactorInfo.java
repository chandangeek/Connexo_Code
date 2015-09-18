package com.elster.jupiter.prepayment.impl;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

/**
 * Created by bvn on 9/18/15.
 */
public class ContactorInfo {
    public Status status;
    public Integer loadLimit;
    public Instant activationDate;
}
