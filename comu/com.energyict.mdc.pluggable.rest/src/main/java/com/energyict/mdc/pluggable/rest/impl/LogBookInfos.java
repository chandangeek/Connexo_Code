/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;

@XmlRootElement
public class LogBookInfos {

    @XmlElement
    @XmlElementWrapper(name = "LogBook")
    public Collection<? super LogBookInfo> logBookInfos = new HashSet<>();

}
