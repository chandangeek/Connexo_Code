/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SelfReadRegisterIdentification.java
 *
 * Created on 1 september 2005, 11:24
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
public class SelfReadRegisterIdentification {

    private static final int[][] SELFREAD_REGISTER_DATAID = {{415,423,431,439},
                                                            {416,424,432,440},
                                                            {417,425,433,441},
                                                            {418,426,434,442},
                                                            {480,481,482,483},
                                                            {410,428,436,444},
                                                            {411,429,437,445},
                                                            {419,427,435,443},
                                                            {470,471,472,473}};
    static List<RegisterDataId> list = new ArrayList<>();
    static {
        // General register data ids
        for(int channel=0;channel<4;channel++) {
            list.add(new RegisterDataId(RegisterDataId.TOTAL_USAGE,RegisterDataId.LONG,SELFREAD_REGISTER_DATAID[0][channel],channel+1, 0,0, "Self read register, "+RegisterDataId.PHENOMENONS[0]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.CURRENT_DEMAND,RegisterDataId.INT,SELFREAD_REGISTER_DATAID[1][channel],channel+1, 0,0, "Self read register, "+RegisterDataId.PHENOMENONS[1]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.PEAK_DEMAND,RegisterDataId.INT,SELFREAD_REGISTER_DATAID[2][channel],channel+1, 0,0, "Self read register, "+RegisterDataId.PHENOMENONS[2]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.TIME_OF_PEAK,RegisterDataId.TIME,SELFREAD_REGISTER_DATAID[3][channel],channel+1, 0,0, "Self read register, "+RegisterDataId.PHENOMENONS[3]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.DATE_OF_PEAK,RegisterDataId.TIME,SELFREAD_REGISTER_DATAID[4][channel],channel+1, 0,0, "Self read register, "+RegisterDataId.PHENOMENONS[4]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.PREVIOUS_DEMAND,RegisterDataId.INT,SELFREAD_REGISTER_DATAID[5][channel],channel+1, 0,0, "Self read register, "+RegisterDataId.PHENOMENONS[5]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.COINCIDENT_DEMAND,RegisterDataId.INT,SELFREAD_REGISTER_DATAID[6][channel],channel+1, 0,0, "Self read register, "+RegisterDataId.PHENOMENONS[6]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.CUMULATIVE_DEMAND,RegisterDataId.LONG,SELFREAD_REGISTER_DATAID[7][channel],channel+1, 0,0, "Self read register, "+RegisterDataId.PHENOMENONS[7]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.RECORDER_VALID_READING,RegisterDataId.LONG,SELFREAD_REGISTER_DATAID[8][channel],channel+1, 0,0, "Self read register, "+RegisterDataId.PHENOMENONS[8]+" for channel "+(channel+1)));
        }
    }

    public static List<RegisterDataId> getRegisterDataIds() {
        return list;
    }

}