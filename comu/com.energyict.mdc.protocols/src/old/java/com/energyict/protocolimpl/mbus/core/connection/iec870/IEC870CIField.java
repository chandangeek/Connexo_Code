/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CIFieldIdentification.java
 *
 * Created on 18 juni 2003, 16:11
 */

package com.energyict.protocolimpl.mbus.core.connection.iec870;

import com.energyict.mdc.common.NotFoundException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class IEC870CIField {
    public static final List typeids = new ArrayList();
    static {
        typeids.add(new IEC870CIField(0x51,"data send (master to slave)"));
        typeids.add(new IEC870CIField(0x52,"selection of slaves"));
        typeids.add(new IEC870CIField(0x72,"slave to master: 12 bytes header followed by variable format data"));
    }

    int id;
    String description;

    /** Creates a new instance of CIFieldIdentification */
    public IEC870CIField(int id, String description) {
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

        Iterator it = typeids.iterator();
        while(it.hasNext()) {
            IEC870CIField tid = (IEC870CIField)it.next();
            if (tid.getId() == id) return tid;
        }
        throw new NotFoundException("IEC870CIField, id "+id+" not found");
    }

}
