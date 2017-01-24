package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.IOException;

class DataBlkRcd {

    SummationRcd summations[];
    RateBlkRcd peakRates[];
    int unitOfMeasure[];
    RateBlkRcd minRate[];
    double cumRate;
    
    static DataBlkRcd parse( Assembly assembly ) throws IOException{
        DataBlkRcd dbr = new DataBlkRcd();
        
        TypeMaximumValues tmv = assembly.getMaxSys().getTable0().getTypeMaximumValues();
        
        int maxSummations = tmv.getMaxSummations();
        dbr.summations = new SummationRcd[maxSummations];
        for( int i = 0; i < maxSummations; i ++ ){
            dbr.summations[i] = SummationRcd.parse(assembly);
        }
        
        int maxPeakRates = tmv.getMaxRatePeaks();
        dbr.peakRates = new RateBlkRcd[maxPeakRates];
        for (int i = 0; i < maxPeakRates; i++) {
            dbr.peakRates[i] = RateBlkRcd.parse(assembly);
        }
        
        int maxConcValues = tmv.getMaxConcValues();
        dbr.unitOfMeasure = new int[maxConcValues];
        for( int i = 0; i < maxConcValues; i ++ ){
            dbr.unitOfMeasure[i] = assembly.intValue();
        }
        
        int maxRateMins = tmv.getMaxRateMins();
        dbr.minRate = new RateBlkRcd[maxRateMins];
        for( int i = 0; i < maxRateMins; i ++ ){
            dbr.minRate[i] = RateBlkRcd.parse(assembly);
        }

        dbr.cumRate = assembly.doubleValue();
        
        return dbr;
    }

    double getCumRate() {
        return cumRate;
    }

    RateBlkRcd[] getMinRate() {
        return minRate;
    }

    RateBlkRcd[] getPeakRates() {
        return peakRates;
    }

    SummationRcd[] getSummations() {
        return summations;
    }

    int[] getUnitOfMeasure() {
        return unitOfMeasure;
    }
    
    public String toString(){
        StringBuffer r = new StringBuffer()
        .append( " DataBlkRcd [" );
       
        for (int i = 0; i < summations.length; i++) {
            r.append( "\n   summation " + i + " " + summations[i].toString() );
        }
        r.append( "\n" );

        for (int i = 0; i < peakRates.length; i++) {
            r.append( "\n   peak rate " + i + " " + peakRates[i].toString() );
        }
        r.append( "\n" );
        
        for( int i = 0; i < unitOfMeasure.length; i ++ ){
            r.append( "\n   unit" + i + " " + UnitOfMeasureCode.get( unitOfMeasure[i] ) );
        }
        r.append( "\n" );
        
        for( int i = 0; i < minRate.length; i ++ ) {
            r.append( "\n   min rate" + i + " " + minRate[i].toString() );
        }
        
        r.append( "cumRate " + cumRate  + "\n\n");
        r.append( " ]");
        
        return r.toString();
    }

}
