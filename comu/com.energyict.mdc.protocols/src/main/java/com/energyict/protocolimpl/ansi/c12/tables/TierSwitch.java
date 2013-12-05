/*
 * TierSwitch.java
 *
 * Created on 4 november 2005, 11:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class TierSwitch {

    int tierSwitchBitfield; // 16 bit
    int newTier; // bit 2..0
    boolean summationSwitchFlag; // bit 3
    boolean demandsSwitchFlag; // bit 4
    int switchMin; // bit 10..5
    int switchHour; // bit 15..11

    int dayScheduleNumber; // 8 bit

    /** Creates a new instance of TierSwitch */
    public TierSwitch(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualTimeAndTOUTable atatt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        tierSwitchBitfield = C12ParseUtils.getInt(data,offset,2,dataOrder);
        newTier = tierSwitchBitfield & 0x0007;
        if (atatt.getTimeTOU().isSeparateSumDemandsFlag()) {
            summationSwitchFlag = (tierSwitchBitfield & 0x0008) == 0x0008;
            demandsSwitchFlag = (tierSwitchBitfield & 0x0010) == 0x0010;
        }
        switchMin = (tierSwitchBitfield >> 5) & 0x003F;
        switchHour = (tierSwitchBitfield >> 11) & 0x001F;
        offset+=2;

    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append(": \n");
        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        return 3;
    }
}
