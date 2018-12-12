package com.energyict.protocolimpl.landisgyr.maxsys2510;

class SummationRcd {

    double summation;
    int unitOfMeas;

    static SummationRcd parse( Assembly assembly ){
        SummationRcd sr = new SummationRcd();
        sr.summation = assembly.doubleValue();
        sr.unitOfMeas = assembly.intValue();
        return sr;
    }
    
    double getSummation() {
        return summation;
    }

    int getUnitOfMeas() {
        return unitOfMeas;
    }
    
    public String toString( ){
        return new StringBuffer()
        .append( "SummationRcd [" )
        .append( " summation " + summation )
        .append( " " + UnitOfMeasureCode.get( unitOfMeas ) )
        .append( "]" ).toString();
    }
}
