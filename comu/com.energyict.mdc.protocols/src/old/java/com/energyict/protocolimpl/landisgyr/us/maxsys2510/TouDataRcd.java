/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.IOException;

class TouDataRcd {

    DataBlkTbl currentData;
    DataBlkTbl pastSeasonData;
    DataBlkTbl selfReadData[];

    static TouDataRcd parse(Assembly assembly) throws IOException {
        TouDataRcd tdr = new TouDataRcd();
        tdr.currentData = DataBlkTbl.parse(assembly);
        tdr.pastSeasonData = DataBlkTbl.parse(assembly);

        TypeMaximumValues tmv = assembly.getMaxSys().getTable0().getTypeMaximumValues();
        int maxSelfReads = tmv.getMaxSelfReads();
        tdr.selfReadData = new DataBlkTbl[maxSelfReads];
        for (int i = 0; i < maxSelfReads; i++) {
            tdr.selfReadData[i] = DataBlkTbl.parse(assembly);
        }

        return tdr;
    }

    DataBlkTbl getCurrentData() {
        return currentData;
    }

    DataBlkTbl getPastSeasonData() {
        return pastSeasonData;
    }

    DataBlkTbl[] getSelfReadData() {
        return selfReadData;
    }

    public String toString() {
        StringBuffer r = new StringBuffer();

        r.append("TouDataRcd [ \n");
        r.append(" currentData \n" );
        r.append( currentData + "\n\n");
        r.append(" pastSeasonData " + pastSeasonData + "\n\n");

        for (int i = 0; i < selfReadData.length; i++) {
            r.append(" selfReadData \n " + selfReadData[i] + "\n\n");
        }
        r.append("]");

        return r.toString();
    }

}
