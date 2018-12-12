package com.elster.us.protocolimpl.landisgyr.quad4;

import com.energyict.cbo.Unit;

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
