/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/** @author fbo */

public class HistoricalRegister {

    private ProtocolLink protocolLink;

    /* Byte Array: Cumulative Registers */
    private byte[] ba507;
    /* Byte Array: TOU Registers */
    private byte[] ba508;
    /* Byte Array: Tarif Sources */
    private byte[] ba667;
    /* Byte Array: CMD Registers */
    private byte[] ba509;
    /* Byte Array: MD Registers */
    private byte[] ba510;

    private Date billingDate;
    private int billingTrigger;
    private TariffSources tariffSources;

    Map map = new HashMap();

    public HistoricalRegister() {}

    /**
     * Creates a new instance of HistoricalRegister.
     *
     * @param data
     * @param protocolLink
     * @param meterType
     * @throws IOException
     */
    public HistoricalRegister(byte[] data, ProtocolLink protocolLink ) throws IOException {
        this.protocolLink = protocolLink;
        parse(data);
    }

    static public boolean has(String dataId) {
        return (("507".compareTo(dataId) == 0)
        || ("508".compareTo(dataId) == 0)
        || ("516".compareTo(dataId) == 0)
        || ("509".compareTo(dataId) == 0)
        || ("510".compareTo(dataId) == 0));
    }

    private void parse(byte[] data) throws IOException {
        ba507 = ProtocolUtils.getSubArray2(data, 0, 128);
        ba508 = ProtocolUtils.getSubArray2(data, 128, 144);
        ba667 = ProtocolUtils.getSubArray2(data, 256, 16);
        ba509 = ProtocolUtils.getSubArray2(data, 272, 36);
        ba510 = ProtocolUtils.getSubArray2(data, 308, 144);

        billingTrigger = ProtocolUtils.getIntLE(data,452,1);
        long shift = (long)ProtocolUtils.getIntLE(data,453,4)&0xFFFFFFFFL;
        TimeZone tz = protocolLink.getTimeZone();
        if(shift != 0)
            billingDate = ProtocolUtils.getCalendar(tz,shift).getTime();

        tariffSources = new TariffSources(ba667);

        map.put("507", ba507);
        map.put("508", ba508);
        map.put("509", ba509);
        map.put("510", ba510);

    }

    protected byte[] getData(String dataId) {
        byte[] dataRaw = (byte[]) map.get(dataId);
        return dataRaw;
    }

    public Date getBillingDate() {
        return billingDate;
    }

    public int getBillingTrigger() {
        return billingTrigger;
    }

    public TariffSources getTariffSources(){
        return tariffSources;
    }

    public String toString() {
        return new StringBuffer( )
        .append( "HistoricalRegister[\n" )
        .append( " end date:" + billingDate + "\n" )
        .append( " billing trigger: " + billingTrigger + "\n" )
        .append( tariffSources.toString() + "\n" )
        .append( "]" )
        .toString();
    }

}
