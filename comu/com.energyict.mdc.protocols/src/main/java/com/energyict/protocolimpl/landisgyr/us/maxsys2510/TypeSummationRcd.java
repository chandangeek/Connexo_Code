package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

class TypeSummationRcd {

    double summation;
    int unitOfMeas;

    static TypeSummationRcd parse( Assembly assembly ){
        TypeSummationRcd sr = new TypeSummationRcd();
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
    
    int getByteSize( ){
        return 10;
    }
    
    public String toString( ){
        return new StringBuffer()
        .append( "SummationRcd [" )
        .append( " summation " + summation )
        .append( " " + UnitOfMeasureCode.get( unitOfMeas ) )
        .append( "]" ).toString();
    }
    
}
