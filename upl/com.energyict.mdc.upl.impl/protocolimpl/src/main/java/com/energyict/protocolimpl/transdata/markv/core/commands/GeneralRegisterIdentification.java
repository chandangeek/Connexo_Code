/*
 * RegisterIdentification.java
 *
 * Created on 10 augustus 2005, 10:38
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
 * @author koen
 */
public class GeneralRegisterIdentification {
    
    // data ID for the first 4 channels
    private static final int[][] GENERAL_REGISTER_DATAID = {{5,13,21,29},
                                                            {6,14,22,30},
                                                            {7,15,23,31},
                                                            {8,16,24,32},
                                                            {300,301,302,303},
                                                            {10,18,26,34},
                                                            {11,19,27,35},
                                                            {9,17,25,33},
                                                            {363,364,365,366}};
            
    static List list = new ArrayList();
    static {
        // General register data ids
        for(int channel=0;channel<4;channel++) {
            list.add(new RegisterDataId(RegisterDataId.TOTAL_USAGE,RegisterDataId.LONG,GENERAL_REGISTER_DATAID[0][channel],channel+1, 0,0, "General register, "+RegisterDataId.PHENOMENONS[0]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.CURRENT_DEMAND,RegisterDataId.INT,GENERAL_REGISTER_DATAID[1][channel],channel+1, 0,0, "General register, "+RegisterDataId.PHENOMENONS[1]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.PEAK_DEMAND,RegisterDataId.INT,GENERAL_REGISTER_DATAID[2][channel],channel+1, 0,0, "General register, "+RegisterDataId.PHENOMENONS[2]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.TIME_OF_PEAK,RegisterDataId.TIME,GENERAL_REGISTER_DATAID[3][channel],channel+1, 0,0, "General register, "+RegisterDataId.PHENOMENONS[3]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.DATE_OF_PEAK,RegisterDataId.TIME,GENERAL_REGISTER_DATAID[4][channel],channel+1, 0,0, "General register, "+RegisterDataId.PHENOMENONS[4]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.PREVIOUS_DEMAND,RegisterDataId.INT,GENERAL_REGISTER_DATAID[5][channel],channel+1, 0,0, "General register, "+RegisterDataId.PHENOMENONS[5]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.COINCIDENT_DEMAND,RegisterDataId.INT,GENERAL_REGISTER_DATAID[6][channel],channel+1, 0,0, "General register, "+RegisterDataId.PHENOMENONS[6]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.CUMULATIVE_DEMAND,RegisterDataId.LONG,GENERAL_REGISTER_DATAID[7][channel],channel+1, 0,0, "General register, "+RegisterDataId.PHENOMENONS[7]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.RECORDER_VALID_READING,RegisterDataId.LONG,GENERAL_REGISTER_DATAID[8][channel],channel+1, 0,0, "General register, "+RegisterDataId.PHENOMENONS[8]+" for channel "+(channel+1)));
            
            list.add(new RegisterDataId(RegisterDataId.TOTAL_USAGE,RegisterDataId.LONG,(-1)*GENERAL_REGISTER_DATAID[0][channel],channel+5, 0,0, "General register, "+RegisterDataId.PHENOMENONS[0]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.CURRENT_DEMAND,RegisterDataId.INT,(-1)*GENERAL_REGISTER_DATAID[1][channel],channel+5, 0,0, "General register, "+RegisterDataId.PHENOMENONS[1]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.PEAK_DEMAND,RegisterDataId.INT,(-1)*GENERAL_REGISTER_DATAID[2][channel],channel+5, 0,0, "General register, "+RegisterDataId.PHENOMENONS[2]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.TIME_OF_PEAK,RegisterDataId.TIME,(-1)*GENERAL_REGISTER_DATAID[3][channel],channel+5, 0,0, "General register, "+RegisterDataId.PHENOMENONS[3]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.DATE_OF_PEAK,RegisterDataId.TIME,(-1)*GENERAL_REGISTER_DATAID[4][channel],channel+5, 0,0, "General register, "+RegisterDataId.PHENOMENONS[4]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.PREVIOUS_DEMAND,RegisterDataId.INT,(-1)*GENERAL_REGISTER_DATAID[5][channel],channel+5, 0,0, "General register, "+RegisterDataId.PHENOMENONS[5]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.COINCIDENT_DEMAND,RegisterDataId.INT,(-1)*GENERAL_REGISTER_DATAID[6][channel],channel+5, 0,0, "General register, "+RegisterDataId.PHENOMENONS[6]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.CUMULATIVE_DEMAND,RegisterDataId.LONG,(-1)*GENERAL_REGISTER_DATAID[7][channel],channel+5, 0,0, "General register, "+RegisterDataId.PHENOMENONS[7]+" for channel "+(channel+1)));
            list.add(new RegisterDataId(RegisterDataId.RECORDER_VALID_READING,RegisterDataId.LONG,(-1)*GENERAL_REGISTER_DATAID[8][channel],channel+5, 0,0, "General register, "+RegisterDataId.PHENOMENONS[8]+" for channel "+(channel+1)));
        }
        
    }
                                                            
    /** Creates a new instance of SelfReadRegisterIdentification */
    private GeneralRegisterIdentification() {
    }
    
    static public List getRegisterDataIds() {
        return list;
    }
}