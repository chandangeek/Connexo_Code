/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;
import com.energyict.protocolimpl.itron.protocol.Utils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class SelfReadAreasBasePage extends AbstractBasePage {

    private int selfReadSet;
    private Date timeStamp;
    private int reason;

    /** Creates a new instance of RealTimeBasePage */
    public SelfReadAreasBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadAreasBasePage:\n");
        strBuff.append("   reason=0x"+Integer.toHexString(getReason())+"\n");
        strBuff.append("   selfReadSet="+getSelfReadSet()+"\n");
        strBuff.append("   timeStamp="+getTimeStamp()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x34AD+getSelfReadSet()*414,6);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;

        TimeZone tz = ((BasePagesFactory)getBasePagesFactory()).getFulcrum().getTimeZone();
        if (!((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().isDstEnabled())
            tz = ProtocolUtils.getWinterTimeZone(tz);

        setTimeStamp(Utils.buildDate(data, offset, tz));
        offset+=Utils.buildDateSize();
        setReason(ProtocolUtils.getInt(data,offset,1));


    }

    public int getSelfReadSet() {
        return selfReadSet;
    }

    public void setSelfReadSet(int selfReadSet) {
        this.selfReadSet = selfReadSet;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

} // public class RealTimeBasePage extends AbstractBasePage
