/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.calendar.Calendar;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class CalendarInfo {

    public long codeTableId;
    public String name;

    public CalendarInfo() {
    }

    public CalendarInfo(Map<String, Object> map) {
        this.codeTableId = (int) map.get("codeTableId");
        this.name = (String) map.get("name");
    }

    public CalendarInfo(Calendar calendar) {
        codeTableId = calendar.getId();
        name = calendar.getName();
    }

}