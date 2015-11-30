/*
 * OptionBoardDefinition.java
 *
 * Created on 13/02/2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.TableFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Koen
 */
public class OptionBoardDefinition {

    private String optionBoardType; // 2 bytes ASCII per the above list. If an XMB is present, the meter sets these fields using information read from the XMB.
    private int optionBoardVersionSSPEC; // 3 bytes SSPEC, Group, and Revision Number of Option Board 1. For example, the information for the XMB is: 0x000224 00 02
    private int optionBoardVersionGroup; // 1 byte
    private int optionBoardVersionRevisionNr; // 1 byte

    static Map<String, String> map = new HashMap<>();
    static {
        map.put("0A","External modem boar");
        map.put("0B","20mA current loop board");
        map.put("0D","Internal modem");
        map.put("0E","RS232");
        map.put("0F","RS485");
        map.put("0G","Outage modem w/ battery, SSPEC = 000225 00");
        map.put("0H","WAN board");
        map.put("0J","Cellnet");
        map.put("0K","2-modem board");
        map.put("11","No Comm board with relays");
        map.put("12","XMB, SSPEC = 000224 00 02");
        map.put("13","Modem interface adapter board");
        map.put("14","Pulse input board");
    }


    /** Creates a new instance of SourceDefinitionEntry */
    public OptionBoardDefinition(byte[] tableData,int offset,TableFactory tableFactory) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int temp = C12ParseUtils.getInt(tableData,offset,2,dataOrder);
        setOptionBoardType(temp==0?null:new String(ProtocolUtils.getSubArray2(tableData,offset,2))); offset+=2;
        setOptionBoardVersionSSPEC(C12ParseUtils.getInt(tableData,offset,3,dataOrder)); offset+=3;
        setOptionBoardVersionGroup(C12ParseUtils.getInt(tableData,16)); offset++;
        setOptionBoardVersionRevisionNr(C12ParseUtils.getInt(tableData,17)); offset++;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("OptionBoardDefinition:\n");
        if (getOptionBoardType()==null) {
            strBuff.append("   Option board not present");
        }
        else {
            String type = map.get(getOptionBoardType());

            strBuff.append("   optionBoardType=").append(type).append("\n");
            strBuff.append("   optionBoardVersionGroup=").append(getOptionBoardVersionGroup()).append("\n");
            strBuff.append("   optionBoardVersionRevisionNr=").append(getOptionBoardVersionRevisionNr()).append("\n");
            strBuff.append("   optionBoardVersionSSPEC=").append(getOptionBoardVersionSSPEC()).append("\n");
        }
        return strBuff.toString();
    }

    public static int getSize(TableFactory tableFactory) {
        return 7;
    }


    public String getOptionBoardType() {
        return optionBoardType;
    }

    public void setOptionBoardType(String optionBoardType) {
        this.optionBoardType = optionBoardType;
    }

    public int getOptionBoardVersionSSPEC() {
        return optionBoardVersionSSPEC;
    }

    public void setOptionBoardVersionSSPEC(int optionBoardVersionSSPEC) {
        this.optionBoardVersionSSPEC = optionBoardVersionSSPEC;
    }

    public int getOptionBoardVersionGroup() {
        return optionBoardVersionGroup;
    }

    public void setOptionBoardVersionGroup(int optionBoardVersionGroup) {
        this.optionBoardVersionGroup = optionBoardVersionGroup;
    }

    public int getOptionBoardVersionRevisionNr() {
        return optionBoardVersionRevisionNr;
    }

    public void setOptionBoardVersionRevisionNr(int optionBoardVersionRevisionNr) {
        this.optionBoardVersionRevisionNr = optionBoardVersionRevisionNr;
    }

}
