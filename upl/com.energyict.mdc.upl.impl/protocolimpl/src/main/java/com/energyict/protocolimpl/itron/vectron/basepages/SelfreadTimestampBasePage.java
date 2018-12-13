/*
 * SelfreadIndexBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;
import com.energyict.protocolimpl.itron.protocol.Utils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class SelfreadTimestampBasePage extends AbstractBasePage {
    
    private int index;
    private Date selfReadDate;
    
    
    /** Creates a new instance of SelfreadIndexBasePage */
    public SelfreadTimestampBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfreadIndexBasePage:\n");
        strBuff.append("   index="+getIndex()+"\n");
        return strBuff.toString();
    }    
    
    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(RegisterFactory.SELFREADS_BASE_ADDRESS+getIndex()*RegisterFactory.SELFREADS_BLOCK_SIZE,RegisterFactory.SELFREADS_BLOCK_SIZE);
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        
        TimeZone tz = getBasePagesFactory().getProtocolLink().getTimeZone();
        if (!((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().isDstEnabled())
            tz = ProtocolUtils.getWinterTimeZone(tz);

        setSelfReadDate(Utils.buildTOODate(data,offset, tz, ((BasePagesFactory)getBasePagesFactory()).getRealTimeBasePage().getCalendar()));        
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Date getSelfReadDate() {
        return selfReadDate;
    }

    public void setSelfReadDate(Date selfReadDate) {
        this.selfReadDate = selfReadDate;
    }

        
} // public class RealTimeBasePage extends AbstractBasePage
