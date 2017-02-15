/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RemoteConfigurationConfiguration.java
 *
 * Created on 13/02/2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class RemoteConfigurationConfiguration extends AbstractTable {

    /*
    Memory storage EEPROM
    Total table size (bytes) 30
    Read access 1
    Write access 3

    This table defines the configuration for each remote port.
    ST-92 through ST-97 are for remote port 1, the dedicated remote port. MT-92 through MT-97
    are for the remote port that is shared with the optical port.
    After a call attempt, if the Call Complete procedure is not received, a random retry time is
    calculated, such that:
    Minimum retry delay < retry delay < maximum retry delay
    */

    private int opticalPortConfigurationTurnAroundDelay; // 1 byte The minimum time between the last byte of a received packet and the ACK character (0x06) sent by the meter. Resolution is msec. 0 = no turn-around delay imposed other than C12.18 requirement of 175 usec. The maximum allowed turn-around delay is 80 msec; if the field is set to a value greater than 80, the firmware sets the field to 80.
    private PortConfiguration[] portConfigurations;


    /** Creates a new instance of RemoteConfigurationConfiguration */
    public RemoteConfigurationConfiguration(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(90,true));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RemoteConfigurationConfiguration:\n");
        strBuff.append("   opticalPortConfigurationTurnAroundDelay="+getOpticalPortConfigurationTurnAroundDelay()+"\n");
        for (int i=0;i<getPortConfigurations().length;i++)
            strBuff.append("   portConfigurations["+i+"]="+getPortConfigurations()[i]+"\n");
        return strBuff.toString();
    }



    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset = 0;
        setOpticalPortConfigurationTurnAroundDelay(C12ParseUtils.getInt(tableData,offset++));
        setPortConfigurations(new PortConfiguration[2]);
        for (int i=0;i<getPortConfigurations().length;i++)
            getPortConfigurations()[i] = new PortConfiguration(tableData, offset, getTableFactory()); offset+=PortConfiguration.getSize(getTableFactory());
    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

    public int getOpticalPortConfigurationTurnAroundDelay() {
        return opticalPortConfigurationTurnAroundDelay;
    }

    public void setOpticalPortConfigurationTurnAroundDelay(int opticalPortConfigurationTurnAroundDelay) {
        this.opticalPortConfigurationTurnAroundDelay = opticalPortConfigurationTurnAroundDelay;
    }

    public PortConfiguration[] getPortConfigurations() {
        return portConfigurations;
    }

    public void setPortConfigurations(PortConfiguration[] portConfigurations) {
        this.portConfigurations = portConfigurations;
    }


}
