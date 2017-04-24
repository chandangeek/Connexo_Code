/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * GlobalParametersTablesForRemotePorts.java
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
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class GlobalParametersTablesForRemotePorts extends AbstractTable {

    /*
    Memory storage: EEPROM
    Total table size: (bytes) 41, Fixed
    Read access: 1
    Write access: 3
    */
    private int psemIdentity; // 1 byte The meters multi-drop id 1-254. 255 is used for 'who called me'. All meters respond to packets with an id = 0. An id of 0 should not be used in a multidrop configuration. The meter with an id = 1 is responsible for initializing the modem. The identity field is in the C12.21 packet header (layer 2). Since there are control tables per port, the identity could be set uniquely for each port. However, it is expected that the identities will be synchronized by programming software.
    private long bitRate; // 4 bytes Default bit rate for the remote port. UINT32
    private String setupString; // 36 bytes Modem init string

    /** Creates a new instance of GlobalParametersTablesForRemotePorts */
    public GlobalParametersTablesForRemotePorts(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(92,true));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("GlobalParametersTablesForRemotePorts:\n");
        strBuff.append("   bitRate="+getBitRate()+"\n");
        strBuff.append("   psemIdentity="+getPsemIdentity()+"\n");
        strBuff.append("   setupString="+getSetupString()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset = 0;
        setPsemIdentity(C12ParseUtils.getInt(tableData,offset++));
        setBitRate(C12ParseUtils.getLong(tableData,offset,4, dataOrder)); offset+=4;
        setSetupString(new String(ProtocolUtils.getSubArray2(tableData, offset, 36))); offset+=36;
    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

    public int getPsemIdentity() {
        return psemIdentity;
    }

    public void setPsemIdentity(int psemIdentity) {
        this.psemIdentity = psemIdentity;
    }

    public long getBitRate() {
        return bitRate;
    }

    public void setBitRate(long bitRate) {
        this.bitRate = bitRate;
    }

    public String getSetupString() {
        return setupString;
    }

    public void setSetupString(String setupString) {
        this.setupString = setupString;
    }

}
