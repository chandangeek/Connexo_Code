/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SourceUnits.java
 *
 * Created on 16 november 2005, 11:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv.tables;

import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.ansi.c12.C12ProtocolLink;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SourceInfo {

    C12ProtocolLink c12ProtocolLink;

    static private final Unit[] sourceUnits=new Unit[]{Unit.get("kW"),Unit.get("kvar"),Unit.get("kvar"),Unit.get("kVA"),Unit.get("kWh"),Unit.get("kvarh"),Unit.get("kvarh"),Unit.get("kVAh"),};
    static private final int[] cFieldc={1,3,128,9,1,3,128,9};

    /** Creates a new instance of SourceUnits */
    public SourceInfo(C12ProtocolLink c12ProtocolLink) {
        this.c12ProtocolLink=c12ProtocolLink;
    }

    public Unit getSummationUnit(int index) throws IOException {
        int dataControlEntryIndex = c12ProtocolLink.getStandardTableFactory().getDataSelectionTable().getSummationSelects()[index];
        return getChannelUnit(dataControlEntryIndex);
    }
    public Unit getDemandUnit(int index) throws IOException {
        int dataControlEntryIndex = c12ProtocolLink.getStandardTableFactory().getDataSelectionTable().getDemandSelects()[index];
        return getChannelUnit(dataControlEntryIndex);
    }
    public Unit getCoincidentUnit(int index) throws IOException {
        int dataControlEntryIndex = c12ProtocolLink.getStandardTableFactory().getDataSelectionTable().getCoincidentSelects()[index];
        return getChannelUnit(dataControlEntryIndex);
    }
    public Unit getCoinDemandAssocUnit(int index) throws IOException {
        int dataControlEntryIndex = c12ProtocolLink.getStandardTableFactory().getDataSelectionTable().getCoinDemandAssocs()[index];
        return getChannelUnit(dataControlEntryIndex);
    }

    public Unit getChannelUnit(int dataControlEntryIndex) throws IOException {
        byte[][] sourceId=c12ProtocolLink.getStandardTableFactory().getDataControlTable().getSourceId();
        if (dataControlEntryIndex==255)
            return Unit.get(""); // C = unitless
        return sourceUnits[sourceId[dataControlEntryIndex][0]];
    }

    public int getSummationCField(int index) throws IOException {
        int dataControlEntryIndex = c12ProtocolLink.getStandardTableFactory().getDataSelectionTable().getSummationSelects()[index];
        return getChannelCField(dataControlEntryIndex);
    }
    public int getDemandCField(int index) throws IOException {
        int dataControlEntryIndex = c12ProtocolLink.getStandardTableFactory().getDataSelectionTable().getDemandSelects()[index];
        return getChannelCField(dataControlEntryIndex);
    }
    public int getCoincidentCField(int index) throws IOException {
        int dataControlEntryIndex = c12ProtocolLink.getStandardTableFactory().getDataSelectionTable().getCoincidentSelects()[index];
        return getChannelCField(dataControlEntryIndex);
    }
    public int getCoinDemandAssocCField(int index) throws IOException {
        int dataControlEntryIndex = c12ProtocolLink.getStandardTableFactory().getDataSelectionTable().getCoinDemandAssocs()[index];
        return getChannelCField(dataControlEntryIndex);
    }

    public int getChannelCField(int dataControlEntryIndex) throws IOException {
        byte[][] sourceId=c12ProtocolLink.getStandardTableFactory().getDataControlTable().getSourceId();

        if (dataControlEntryIndex==255)
            return 82; // C = unitless

        return cFieldc[sourceId[dataControlEntryIndex][0]];
    }

}
