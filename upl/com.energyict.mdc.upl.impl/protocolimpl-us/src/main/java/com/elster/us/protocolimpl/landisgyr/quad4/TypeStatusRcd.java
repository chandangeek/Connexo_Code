package com.elster.us.protocolimpl.landisgyr.quad4;

class TypeStatusRcd {
    
    int noOfBadCrcs;
    int noOfBadPasswds;
    int noOfMalfunctions;
    int noOfPwrFailures;
    
    static TypeStatusRcd parse(Quad4 quad4, Assembly assembly ){
        TypeStatusRcd tsr = new TypeStatusRcd();
        tsr.noOfBadCrcs = assembly.intValue();
        tsr.noOfBadPasswds = assembly.intValue();
        tsr.noOfMalfunctions = assembly.intValue();
        tsr.noOfMalfunctions = assembly.intValue();
        return tsr;
    }
    
    /** bin no. CRC's since last stat clr */
    int getNoOfBadCrcs() {
        return noOfBadCrcs;
    }
    
    /** bin no. bad pswds since last clr */
    int getNoOfBadPasswds() {
        return noOfBadPasswds;
    }

    /** bin no. prog errs since last clr */
    int getNoOfMalfunctions() {
        return noOfMalfunctions;
    }
    
    /** bin no. pwr fails since last clr */
    int getNoOfPwrFailures() {
        return noOfPwrFailures;
    }
    
    public String toString( ){
        StringBuffer sb = new StringBuffer()
        .append( "TypeStatusRcd [ " )
        .append( "noOfBadCrcs " + noOfBadCrcs + " " )
        .append( "noOfBadPasswds " + noOfBadPasswds + " " )
        .append( "noOfMalfunctions " + noOfMalfunctions + " " )
        .append( "noOfPwrFailures " + noOfPwrFailures + " " )
        .append( "]" );
        return sb.toString();
    }
    
}
