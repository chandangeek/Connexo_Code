/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EnergyTypeCode.java
 *
 * Created on 15 juli 2004, 16:31
 */

package com.energyict.protocolimpl.gmc.u1600;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @author  Koen
 */
public class EnergyTypeCode {

    static List list = new ArrayList();
    static {
        list.add(new EnergyTypeCode(1,"Active Import, "));
        list.add(new EnergyTypeCode(2,"Active Export, "));
        list.add(new EnergyTypeCode(3,"Reactive Import, "));
        list.add(new EnergyTypeCode(4,"Reactive Export, "));
        list.add(new EnergyTypeCode(5,"Reactive Q1, "));
        list.add(new EnergyTypeCode(6,"Reactive Q2, "));
        list.add(new EnergyTypeCode(7,"Reactive Q3, "));
        list.add(new EnergyTypeCode(8,"Reactive Q4, "));
        list.add(new EnergyTypeCode(9,"Apparent, "));
        list.add(new EnergyTypeCode(129,"Reactive, "));
        list.add(new EnergyTypeCode(82,"Unitless Quantity, "));
    }

    int obisCCode;
    String description;

    /** Creates a new instance of EnergyTypeCode */
    public EnergyTypeCode(int obisCCode,String description) {
        this.obisCCode=obisCCode;
        this.description=description;
    }

    static public String getCompountInfoFromObisC(int obisC, boolean energy) throws NoSuchRegisterException {

        Iterator it = list.iterator();
        while(it.hasNext()) {
            EnergyTypeCode eit = (EnergyTypeCode)it.next();
            if (eit.getObisCCode() == obisC)
                return eit.getDescription()+(energy?"energy":"power");
        }
        throw new NoSuchRegisterException("EnergyTypeCode, getCompountInfoFromObisC, invalid obis C code, "+obisC);
    }

    /**
     * Getter for property obisCCode.
     * @return Value of property obisCCode.
     */
    public int getObisCCode() {
        return obisCCode;
    }

    /**
     * Setter for property obisCCode.
     * @param obisCCode New value of property obisCCode.
     */
    public void setObisCCode(int obisCCode) {
        this.obisCCode = obisCCode;
    }

    /**
     * Getter for property description.
     * @return Value of property description.
     */
    public java.lang.String getDescription() {
        return description;
    }

    /**
     * Setter for property description.
     * @param description New value of property description.
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }

}
