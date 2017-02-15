/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TOURegisterIdentification.java
 *
 * Created on 2 september 2005, 16:52
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Koen
 */
public class TOURegisterIdentification {

    private static final int[][] TOU_REGISTER_DATAID = {{105,201,109,205,113,209,117,213},
                                                        {129,225,133,229,137,233,141,237},
                                                        {153,249,157,253,161,257,165,261},
                                                        {177,273,181,277,185,281,189,285},
                                                        {106,202,110,206,114,210,118,214},
                                                        {130,226,134,230,138,234,142,238},
                                                        {154,250,158,254,162,258,166,262},
                                                        {178,274,182,278,186,282,190,286},
                                                        {315,319,323,327,331,335,339,343},
                                                        {316,320,324,328,332,336,340,344},
                                                        {317,321,325,329,333,337,341,345},
                                                        {318,322,326,330,334,338,342,346},
                                                        {107,203,111,207,115,211,119,215},
                                                        {131,227,135,231,139,235,143,239},
                                                        {155,251,159,255,163,259,167,263},
                                                        {179,275,183,279,187,283,191,287},
                                                        {108,204,112,208,116,212,120,216},
                                                        {132,228,136,232,140,236,144,240},
                                                        {156,252,160,256,164,260,168,264},
                                                        {180,276,184,280,188,284,192,288},
                                                        {378,382,386,390,394,398,402,406},
                                                        {379,383,387,391,395,399,403,407},
                                                        {380,384,388,392,396,400,404,408},
                                                        {381,385,389,393,397,401,405,409}};


    static List<RegisterDataId> list = new ArrayList<>();
    static {

        int[] touAll = {5, 347, 13, 348, 21, 349, 29, 350};
        // TOTAL USAGE ALL
        for(int channel=0;channel<4;channel++) {
            for(int billing=0;billing<2;billing++) {
                list.add(new RegisterDataId(RegisterDataId.TOTAL_USAGE,RegisterDataId.LONG,touAll[channel*2+billing],channel+1, 0, billing, "TOU register, "+RegisterDataId.PHENOMENONS[RegisterDataId.TOTAL_USAGE]+" for channel "+(channel+1)+(billing==0?"":", last billing ")));
            }
        }

        // General register data ids
        for(int channel=0;channel<4;channel++) {
            for(int rate=0;rate<4;rate++) {
                for(int billing=0;billing<2;billing++) {
                    list.add(new RegisterDataId(RegisterDataId.TOTAL_USAGE,RegisterDataId.LONG,TOU_REGISTER_DATAID[rate][channel*2+billing],channel+1, rate, billing, "TOU register, "+RegisterDataId.PHENOMENONS[RegisterDataId.TOTAL_USAGE]+" for channel "+(channel+1)+", rate "+(rate+1)+(billing==0?"":", last billing ")));
                    list.add(new RegisterDataId(RegisterDataId.PEAK_DEMAND,RegisterDataId.INT,TOU_REGISTER_DATAID[rate+4][channel*2+billing],channel+1, rate, billing, "TOU register, "+RegisterDataId.PHENOMENONS[RegisterDataId.PEAK_DEMAND]+" for channel "+(channel+1)+", rate "+(rate+1)+(billing==0?"":", last billing ")));
                    list.add(new RegisterDataId(RegisterDataId.DATE_OF_PEAK,RegisterDataId.TIME,TOU_REGISTER_DATAID[rate+8][channel*2+billing],channel+1, rate, billing, "TOU register, "+RegisterDataId.PHENOMENONS[RegisterDataId.DATE_OF_PEAK]+" for channel "+(channel+1)+", rate "+(rate+1)+(billing==0?"":", last billing ")));
                    list.add(new RegisterDataId(RegisterDataId.TIME_OF_PEAK,RegisterDataId.TIME,TOU_REGISTER_DATAID[rate+12][channel*2+billing],channel+1, rate, billing, "TOU register, "+RegisterDataId.PHENOMENONS[RegisterDataId.TIME_OF_PEAK]+" for channel "+(channel+1)+", rate "+(rate+1)+(billing==0?"":", last billing ")));
                    list.add(new RegisterDataId(RegisterDataId.COINCIDENT_DEMAND,RegisterDataId.INT,TOU_REGISTER_DATAID[rate+16][channel*2+billing],channel+1, rate, billing, "TOU register, "+RegisterDataId.PHENOMENONS[RegisterDataId.COINCIDENT_DEMAND]+" for channel "+(channel+1)+", rate "+(rate+1)+(billing==0?"":", last billing ")));
                    list.add(new RegisterDataId(RegisterDataId.CUMULATIVE_DEMAND,RegisterDataId.LONG,TOU_REGISTER_DATAID[rate+20][channel*2+billing],channel+1, rate, billing, "TOU register, "+RegisterDataId.PHENOMENONS[RegisterDataId.CUMULATIVE_DEMAND]+" for channel "+(channel+1)+", rate "+(rate+1)+(billing==0?"":", last billing ")));
                }
            }
        }
    }

    public static List<RegisterDataId> getRegisterDataIds() {
        return list;
    }

}