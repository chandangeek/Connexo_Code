/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ReadingQualityInfo {

    @XmlAttribute
    public String cimCode;

    @XmlAttribute
    public String systemName;

    @XmlAttribute
    public String categoryName;

    @XmlAttribute
    public String indexName;

    @XmlAttribute
    public String comment;
}
