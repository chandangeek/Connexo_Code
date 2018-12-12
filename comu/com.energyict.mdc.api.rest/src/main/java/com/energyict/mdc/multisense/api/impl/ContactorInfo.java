/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonInstantAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

/**
 * Created by bvn on 9/18/15.
 */
public class ContactorInfo {
    public Status status;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Integer loadLimit;
    public Instant activationDate;
    public Integer loadTolerance;
}
