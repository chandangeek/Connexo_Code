/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IntervalFieldInfo {
    public String name;
    public Integer time;
    public Integer macro;
}