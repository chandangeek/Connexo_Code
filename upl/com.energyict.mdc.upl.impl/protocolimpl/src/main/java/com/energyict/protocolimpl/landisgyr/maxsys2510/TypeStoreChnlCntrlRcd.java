package com.energyict.protocolimpl.landisgyr.maxsys2510;

class TypeStoreChnlCntrlRcd {

    TypeChannelSelectRcd chnlNo;
    double kePulseValue;
    
    static TypeStoreChnlCntrlRcd parse( Assembly assembly ){
        TypeStoreChnlCntrlRcd rcd = new TypeStoreChnlCntrlRcd();
        rcd.chnlNo = TypeChannelSelectRcd.parse( assembly, 0, 0 );
        
        return rcd;
    }
    
    public String toString(){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "TypeStoreChnlCntrlRcd [\n" );
        rslt.append( "    " + chnlNo.toString() + "\n" );
        rslt.append( "    " + kePulseValue + "\n" );
        rslt.append( "    ]\n" );
        return rslt.toString();
    }
    
}
