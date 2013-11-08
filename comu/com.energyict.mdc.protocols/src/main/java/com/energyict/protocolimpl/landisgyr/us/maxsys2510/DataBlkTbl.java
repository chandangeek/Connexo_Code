package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.IOException;
import java.util.Date;

class DataBlkTbl {

    Date endDateTime;
    int season;
    TypeDataBlkRcd dataBlock[];
    
    static DataBlkTbl parse(Assembly assembly) throws IOException {
        DataBlkTbl dbt = new DataBlkTbl();
        dbt.endDateTime = TypeDateTimeRcd.parse(assembly).toDate();
        dbt.season = assembly.intValue();
        
        TypeMaximumValues tmv = assembly.getMaxSys().getTable0().getTypeMaximumValues(); 
        int maxSelfReads = tmv.getMaxDataBlks();
        dbt.dataBlock = new TypeDataBlkRcd[maxSelfReads];
        for( int i = 0; i < maxSelfReads; i ++ ){
            dbt.dataBlock[i] = TypeDataBlkRcd.parse(assembly);
        }
        
        return dbt;
    }

    TypeDataBlkRcd[] getDataBlock() {
        return dataBlock;
    }
    
    TypeDataBlkRcd getDataBlock( int index ) {
        return dataBlock[index];
    }
    
    Date getEndDateTime() {
        return endDateTime;
    }

    int getSeason() {
        return season;
    }

    public String toString( ){
        StringBuffer r = new StringBuffer();
        r.append( "DataBlkTbl [ \n " );
        r.append( " endDateTime " + endDateTime + "\n" );
        r.append( " season " + season + "\n" );
        for (int i = 0; i < dataBlock.length; i++) {
            r.append( dataBlock[i].toString() + "\n" );
        }
        r.append( " ]");
        return r.toString();
    }

}
