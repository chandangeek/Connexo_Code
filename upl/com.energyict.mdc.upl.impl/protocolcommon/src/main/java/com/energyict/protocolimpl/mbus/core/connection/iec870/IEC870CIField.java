/*
 * CIFieldIdentification.java
 *
 * Created on 18 juni 2003, 16:11
 */

package com.energyict.protocolimpl.mbus.core.connection.iec870;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class IEC870CIField {
    private static final List<IEC870CIField> TYPEIDS = new ArrayList<>();
    static {
        TYPEIDS.add(new IEC870CIField(0x51,"data send (master to slave)"));
        TYPEIDS.add(new IEC870CIField(0x52,"selection of slaves"));
        TYPEIDS.add(new IEC870CIField(0x72,"slave to master: 12 bytes header followed by variable format data"));
    }

    private int id;
    private String description;

    private IEC870CIField(int id, String description) {
        this.id=id;
        this.description=description;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }
    public static IEC870CIField getCIField(int id) {

        // // reserved type identification ranges
        if ((id>=114)&&(id<=119)) id=114;
        if ((id>=107)&&(id<=109)) id=107;
        if ((id>=71)&&(id<=99)) id=71;
        if ((id>=52)&&(id<=69)) id=52;
        if ((id>=41)&&(id<=44)) id=41;
        if ((id>=22)&&(id<=29)) id=22;

        for (IEC870CIField tid : TYPEIDS) {
            if (tid.getId() == id) {
                return tid;
            }
        }
        throw new IllegalArgumentException("IEC870CIField, id "+id+" not found");
    }

}