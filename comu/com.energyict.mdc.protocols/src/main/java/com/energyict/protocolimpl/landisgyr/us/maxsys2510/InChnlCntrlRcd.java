/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import com.energyict.mdc.common.Unit;

class InChnlCntrlRcd {

    int form;
    int unitOfMeas;
    double kePulseValue;
    double xfmrRatio;

    static InChnlCntrlRcd parse(Assembly assembly){
        InChnlCntrlRcd iccr = new InChnlCntrlRcd();
        iccr.form = assembly.intValue();
        iccr.unitOfMeas = assembly.intValue();
        iccr.kePulseValue = assembly.doubleValue();
        iccr.xfmrRatio = assembly.doubleValue();
        return iccr;
    }

    int getForm() {
        return form;
    }

    double getKePulseValue() {
        return kePulseValue;
    }

    int getUnitOfMeas() {
        return unitOfMeas;
    }

    Unit getUnit() {
        return UnitOfMeasureCode.get( unitOfMeas ).getUnit();
    }

    double getXfmrRatio() {
        return xfmrRatio;
    }

    public String toString(){
        return new StringBuffer()
        .append( "InChnlCntrlRcd [ " )
        .append( "form " + form + " " )
        .append( UnitOfMeasureCode.get( unitOfMeas ) + " " )
        .append( "kePluseValue " + kePulseValue + " " )
        .append( "xfmrRation " + xfmrRatio + " ]" )
        .toString();
    }

}
