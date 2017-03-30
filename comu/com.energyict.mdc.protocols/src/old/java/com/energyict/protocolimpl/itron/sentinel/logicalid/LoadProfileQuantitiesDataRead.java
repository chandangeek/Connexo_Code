/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
public class LoadProfileQuantitiesDataRead extends AbstractDataRead {

    private LogicalID[] logicalIDs;


    /** Creates a new instance of ConstantsDataRead */
    public LoadProfileQuantitiesDataRead(DataReadFactory dataReadFactory) {
        super(dataReadFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileQuantitiesDataRead:\n");
        for (int i=0;i<getLogicalIDs().length;i++) {
            strBuff.append("       logicalIDs["+i+"]="+getLogicalIDs()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {

        int offset=0;
        int dataOrder = getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setLogicalIDs(new LogicalID[getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels()]);
        for (int channel=0;channel<getLogicalIDs().length;channel++) {
           getLogicalIDs()[channel] = LogicalIDFactory.findLogicalId(C12ParseUtils.getLong(data, offset, 4, dataOrder));
           offset+=4;
        }
    }

    protected void prepareBuild() throws IOException {

        long[] lids = new long[]{LogicalIDFactory.findLogicalId("LP_CHAN_1_QUANTITY").getId(),
                                 LogicalIDFactory.findLogicalId("LP_CHAN_2_QUANTITY").getId(),
                                 LogicalIDFactory.findLogicalId("LP_CHAN_3_QUANTITY").getId(),
                                 LogicalIDFactory.findLogicalId("LP_CHAN_4_QUANTITY").getId(),
                                 LogicalIDFactory.findLogicalId("LP_CHAN_5_QUANTITY").getId(),
                                 LogicalIDFactory.findLogicalId("LP_CHAN_6_QUANTITY").getId(),
                                 LogicalIDFactory.findLogicalId("LP_CHAN_7_QUANTITY").getId(),
                                 LogicalIDFactory.findLogicalId("LP_CHAN_8_QUANTITY").getId()};

        setDataReadDescriptor(new DataReadDescriptor(0x00, getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels(), lids));

    } // protected void prepareBuild() throws IOException

    public LogicalID[] getLogicalIDs() {
        return logicalIDs;
    }

    public void setLogicalIDs(LogicalID[] logicalIDs) {
        this.logicalIDs = logicalIDs;
    }

} // public class ConstantsDataRead extends AbstractDataRead
