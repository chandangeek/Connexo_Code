package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.calendar.Calendar;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * Represents the Info object for a {@link Calendar}.
 *
 * Copyrights EnergyICT
 * Date: 21/11/13
 * Time: 15:08
 */
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