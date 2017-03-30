/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.device.data.LogBook;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class LogBookInfo {

    public int id;
    public String name;

    public LogBookInfo() {
    }

    public LogBookInfo(Map<String, Object> map) {
        this.id = (int) map.get("id");
        this.name = (String) map.get("name");
    }

    public LogBookInfo(LogBook logBook) {
        id = (int) logBook.getId();
        name = logBook.getLogBookType().getName();
    }

}
