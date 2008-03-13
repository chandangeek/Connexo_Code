/*
 * LogicalID.java
 *
 * Created on 3 november 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import com.energyict.cbo.*;
import com.energyict.obis.*;

/**
 *
 * @author Koen
 */
public class LogicalID {
    
    private String description;
    private long id;
    private Unit unit;
    private ObisCode obisCode;
    
    /** Creates a new instance of LogicalID */
    public LogicalID(String description, long id, ObisCode obisCode, Unit unit) {
        this.setDescription(description);
        this.setId(id);
        this.setUnit(unit);
        this.setObisCode(obisCode);
    }

    public String toString() {
        return description+", 0x"+Long.toHexString(id)+", "+unit+", "+obisCode;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }
    
}
