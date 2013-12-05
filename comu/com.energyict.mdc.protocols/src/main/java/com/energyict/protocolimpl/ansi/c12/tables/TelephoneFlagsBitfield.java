/*
 * TelephoneFlagsBitfield.java
 *
 * Created on 23 februari 2006, 11:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class TelephoneFlagsBitfield {

    private boolean answerFlag;
    private boolean sAnchorDateFlag;
    private boolean offhookDetectFlag;
    private int bitRate;
    private boolean idInPurpose;
    private boolean noLockoutParm;



    /** Creates a new instance of TelephoneFlagsBitfield */
    public TelephoneFlagsBitfield(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        int temp = C12ParseUtils.getInt(data,offset++);
        answerFlag = (temp & 0x01) == 0x01;
        sAnchorDateFlag = (temp & 0x02) == 0x02;
        offhookDetectFlag = (temp & 0x04) == 0x04;
        bitRate = (temp & 0x18) >> 3;
        idInPurpose = (temp & 0x20) == 0x20;
        noLockoutParm = (temp & 0x40) == 0x40;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TelephoneFlagsBitfield:\n");
        strBuff.append("   SAnchorDateFlag="+isSAnchorDateFlag()+"\n");
        strBuff.append("   answerFlag="+isAnswerFlag()+"\n");
        strBuff.append("   bitRate="+getBitRate()+"\n");
        strBuff.append("   idInPurpose="+isIdInPurpose()+"\n");
        strBuff.append("   noLockoutParm="+isNoLockoutParm()+"\n");
        strBuff.append("   offhookDetectFlag="+isOffhookDetectFlag()+"\n");
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        return 1;
    }

    public boolean isAnswerFlag() {
        return answerFlag;
    }

    public boolean isSAnchorDateFlag() {
        return sAnchorDateFlag;
    }

    public boolean isOffhookDetectFlag() {
        return offhookDetectFlag;
    }

    public int getBitRate() {
        return bitRate;
    }

    public boolean isIdInPurpose() {
        return idInPurpose;
    }

    public boolean isNoLockoutParm() {
        return noLockoutParm;
    }
}
