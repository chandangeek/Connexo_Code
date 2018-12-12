package com.energyict.protocolimpl.landisgyr.maxsys2510;

import java.io.IOException;

class Table8 {

    MtrInputCntrl mtrInputCntrl;

    static Table8 parse(Assembly assembly) throws IOException {
        Table8 r = new Table8();
        r.mtrInputCntrl = MtrInputCntrl.parse(assembly);
        return r;
    }

    MtrInputCntrl getMtrInputCntrl() {
        return mtrInputCntrl;
    }
    
    InChnlCntrlRcd getInChnlCntrlRcd( int index ){
        return mtrInputCntrl.getInputCntrl()[index-1];
    }

    public String toString() {
        return mtrInputCntrl.toString();
    }

}
     