package com.energyict.protocolimpl.landisgyr.maxsys2510;

import java.io.IOException;


class MtrInputCntrl {

    int staticInputTime;
    InChnlCntrlRcd inputCntrl[];

    static MtrInputCntrl parse( Assembly assembly ) throws IOException{
        TypeMaximumValues tmv = assembly.getMaxSys().getTable0().getTypeMaximumValues();
        MtrInputCntrl mic = new MtrInputCntrl();
        mic.staticInputTime = assembly.intValue();
        mic.inputCntrl = new InChnlCntrlRcd[tmv.getMaxMtrInputs()];
        for (int i = 0; i < mic.inputCntrl.length; i++) {
            mic.inputCntrl[i] = InChnlCntrlRcd.parse(assembly);
        }
        return mic;
    }
    
    InChnlCntrlRcd[] getInputCntrl() {
        return inputCntrl;
    }

    int getStaticInputTime() {
        return staticInputTime;
    }

    public String toString() {
        StringBuffer rslt = new StringBuffer();
        rslt.append( "MtrInputCntrl [ " ); 
        rslt.append( "staticInputTime " + staticInputTime + " \n" ); 
        for (int i = 0; i < inputCntrl.length; i++) {
            rslt.append( inputCntrl[i].toString() + "\n" );
        }
        rslt.append( "\n" );
        return rslt.toString();
    }
}
