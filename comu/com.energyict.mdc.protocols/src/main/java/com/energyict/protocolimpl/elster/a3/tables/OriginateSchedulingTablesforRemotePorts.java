/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * OriginateSchedulingTablesforRemotePorts.java
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
import com.energyict.protocolimpl.ansi.c12.tables.RDate;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class OriginateSchedulingTablesforRemotePorts extends AbstractTable { 
	
    /*
    Memory storage: EEPROM
    Total table size: (bytes) 48
    Read access: 1
    Write access: 3

    ST-94 and MT-94 control call origination for the remote ports. ST/MT-94 supports 2 event
    fields that can be used for event calls to two different phone numbers. These event fields can
    be used to trigger calls for different types of events. Elster Electricity metering software uses
    the first event field for alarm calls and the second event field solely for power restoration
    calls.
    */

    private Date sAnchorDate; // 2 bytes The anchor date to use for period/offset based recurring dates. DATE format

    // ****************************************************************************************************
    // The meter supports 1 recurring date for each port. Each recurring date requires 6 bytes.
    private RDate originateDateRecurring; // 2 bytes The day or recurring period to place a call. RDATE format.
    private Date startTimeRecurring; // 3 bytes The time to place the call. TIME format. Byte 1 = binary hours (0-23), byte 2 = binary minutes (0-59), byte 3 = binary seconds (0-59). 
    private int originateControlRecurring; // 1 byte b0-2: Primary_phone_number Select the ST-93 or MT-93 phone to use for the call. (0-2)
                                                             // b3: unused = 0
                                                             // b4-6: Secondary_phone_number The meter ignores this field and only uses the Primary phone number.
                                                             // b7: USE_WINDOWS If set to  1  the meter will check the originate windows (ST-93/MT-93). If outside a window, the meter waits until the start of the next window. If set to  0 , the meter will ignore call windows and attempt to call immediately.

    // ****************************************************************************************************
    // The meter supports 1 non-recurring date for each port. Each non-recurring date requires 6 bytes.							 
    private Date originateDateNonRecurring; // 2 bytes The date to place a call. DATE format.
    private Date startTimeNonRecurring; // 3 bytes The time to place the call. TIME format. Byte 1 = binary hours (0-23), byte 2 = binary minutes (0-59), byte 3 = binary seconds (0-59).
    private int originateControlNonRecurring; // 1 byte b0-2: Primary_phone_number Select the ST-93 or MT-93 phone to use for the call. (0-2)
                                                             // b3: unused = 0
                                                             // b4-6: Secondary_phone_number The meter ignores this field and only uses the Primary phone number.
                                                             // b7: USE_WINDOWS If set to  1  the meter will check the originate windows (ST- 93/MT-93). If outside a window, the meter waits until the start of the next window. If set to  0 , the meter will ignore call windows and attempt to call immediately.
    /*
     Identify the events that can trigger a call (alarm call). There is a control flag for
     each status bit (standard and manufacturer s) in ST-3. If the flag is set, an alarm
     call will be made when the corresponding status flag is set in ST-3. The meter
     supports 2 event records in ST-94 so that different phone numbers can be
     used for restoration and alarm events.
    */    
    private Event[] events; // 2 events
    
    /** Creates a new instance of OriginateSchedulingTablesforRemotePorts */
    public OriginateSchedulingTablesforRemotePorts(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(94,true));
    }
 
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("OriginateSchedulingTablesforRemotePorts:\n");
        strBuff.append("   SAnchorDate="+getSAnchorDate()+"\n");
        strBuff.append("   events="+getEvents()+"\n");
        strBuff.append("   originateControlNonRecurring="+getOriginateControlNonRecurring()+"\n");
        strBuff.append("   originateControlRecurring="+getOriginateControlRecurring()+"\n");
        strBuff.append("   originateDateNonRecurring="+getOriginateDateNonRecurring()+"\n");
        strBuff.append("   originateDateRecurring="+getOriginateDateRecurring()+"\n");
        strBuff.append("   startTimeNonRecurring="+getStartTimeNonRecurring()+"\n");
        strBuff.append("   startTimeRecurring="+getStartTimeRecurring()+"\n");
        return strBuff.toString();
    }
    
    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int timeFormat = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat();
        int offset = 0;
        
        setSAnchorDate(C12ParseUtils.getDateFromDate(tableData, offset, getTableFactory().getC12ProtocolLink().getTimeZone(), dataOrder)); 
        offset+=C12ParseUtils.getDateSize();
        setOriginateDateRecurring(new RDate(tableData, offset, getTableFactory()));
        offset+=RDate.getSize(getTableFactory());
        setStartTimeRecurring(C12ParseUtils.getDateFromTime(tableData, offset, timeFormat, getTableFactory().getC12ProtocolLink().getTimeZone(), dataOrder));
        offset+=C12ParseUtils.getTimeSize(timeFormat);
        setOriginateControlRecurring(C12ParseUtils.getInt(tableData,offset++));
        Date originateDateNonRecurring = C12ParseUtils.getDateFromDate(tableData, offset, getTableFactory().getC12ProtocolLink().getTimeZone(), dataOrder); 
        offset+=C12ParseUtils.getDateSize();
        Date startTimeNonRecurring = C12ParseUtils.getDateFromTime(tableData, offset, timeFormat, getTableFactory().getC12ProtocolLink().getTimeZone(), dataOrder);
        offset+=C12ParseUtils.getTimeSize(timeFormat);
        int originateControlNonRecurring = C12ParseUtils.getInt(tableData,offset++);
        setEvents(new Event[2]);
        for (int i=0;i<getEvents().length;i++) {
            getEvents()[i] = new Event(tableData,offset,getTableFactory());
            offset+=Event.getSize(getTableFactory());
        }
    } 
    
    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

    public Date getSAnchorDate() {
        return sAnchorDate;
    }

    public void setSAnchorDate(Date sAnchorDate) {
        this.sAnchorDate = sAnchorDate;
    }

    public RDate getOriginateDateRecurring() {
        return originateDateRecurring;
    }

    public void setOriginateDateRecurring(RDate originateDateRecurring) {
        this.originateDateRecurring = originateDateRecurring;
    }

    public Date getStartTimeRecurring() {
        return startTimeRecurring;
    }

    public void setStartTimeRecurring(Date startTimeRecurring) {
        this.startTimeRecurring = startTimeRecurring;
    }

    public int getOriginateControlRecurring() {
        return originateControlRecurring;
    }

    public void setOriginateControlRecurring(int originateControlRecurring) {
        this.originateControlRecurring = originateControlRecurring;
    }

    public Date getOriginateDateNonRecurring() {
        return originateDateNonRecurring;
    }

    public void setOriginateDateNonRecurring(Date originateDateNonRecurring) {
        this.originateDateNonRecurring = originateDateNonRecurring;
    }

    public Date getStartTimeNonRecurring() {
        return startTimeNonRecurring;
    }

    public void setStartTimeNonRecurring(Date startTimeNonRecurring) {
        this.startTimeNonRecurring = startTimeNonRecurring;
    }

    public int getOriginateControlNonRecurring() {
        return originateControlNonRecurring;
    }

    public void setOriginateControlNonRecurring(int originateControlNonRecurring) {
        this.originateControlNonRecurring = originateControlNonRecurring;
    }

    public Event[] getEvents() {
        return events;
    }

    public void setEvents(Event[] events) {
        this.events = events;
    }

}
