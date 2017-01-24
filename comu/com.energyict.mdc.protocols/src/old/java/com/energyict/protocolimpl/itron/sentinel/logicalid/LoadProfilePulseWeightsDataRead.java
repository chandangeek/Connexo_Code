/*
 * ConstantsDataRead.java
 *
 * Created on 2 november 2006, 16:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class LoadProfilePulseWeightsDataRead extends AbstractDataRead {

    private int[] loadProfilePulseWeights;

    /** Creates a new instance of ConstantsDataRead */
    public LoadProfilePulseWeightsDataRead(DataReadFactory dataReadFactory) {
        super(dataReadFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfilePulseWeightsDataRead:\n");
        for (int i=0;i<getLoadProfilePulseWeights().length;i++) {
            strBuff.append("       loadProfilePulseWeights["+i+"]="+getLoadProfilePulseWeights()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {

        int offset=0;
        int dataOrder = getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        setLoadProfilePulseWeights(new int[getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels()]);
        for (int channel=0;channel<getLoadProfilePulseWeights().length;channel++) {
            getLoadProfilePulseWeights()[channel] = C12ParseUtils.getInt(data,offset, 2, dataOrder);
            offset+=2;
        }
    }

    protected void prepareBuild() throws IOException {

        long[] lids = new long[]{LogicalIDFactory.findLogicalId("LP_CHAN_1_PULSE_WT").getId(),
                                 LogicalIDFactory.findLogicalId("LP_CHAN_2_PULSE_WT").getId(),
                                 LogicalIDFactory.findLogicalId("LP_CHAN_3_PULSE_WT").getId(),
                                 LogicalIDFactory.findLogicalId("LP_CHAN_4_PULSE_WT").getId(),
                                 LogicalIDFactory.findLogicalId("LP_CHAN_5_PULSE_WT").getId(),
                                 LogicalIDFactory.findLogicalId("LP_CHAN_6_PULSE_WT").getId(),
                                 LogicalIDFactory.findLogicalId("LP_CHAN_7_PULSE_WT").getId(),
                                 LogicalIDFactory.findLogicalId("LP_CHAN_8_PULSE_WT").getId()};

        setDataReadDescriptor(new DataReadDescriptor(0x00, getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels(), lids));

    } // protected void prepareBuild() throws IOException

    public int[] getLoadProfilePulseWeights() {
        return loadProfilePulseWeights;
    }

    public void setLoadProfilePulseWeights(int[] loadProfilePulseWeights) {
        this.loadProfilePulseWeights = loadProfilePulseWeights;
    }

} // public class ConstantsDataRead extends AbstractDataRead
