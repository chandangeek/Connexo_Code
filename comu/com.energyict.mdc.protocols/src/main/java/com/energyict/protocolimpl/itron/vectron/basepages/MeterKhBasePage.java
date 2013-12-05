/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;
import java.math.BigDecimal;
/**
 *
 * @author Koen
 */
public class MeterKhBasePage extends AbstractBasePage {

    private BigDecimal kh;

    /** Creates a new instance of RealTimeBasePage */
    public MeterKhBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MeterKhBasePage:\n");
        strBuff.append("   kh="+getKh()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x21C1,3);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setKh(ParseUtils.convertBCDFixedPoint(data,offset,3,8));
    }

    public BigDecimal getKh() {
        return kh;
    }

    public void setKh(BigDecimal kh) {
        this.kh = kh;
    }


} // public class RealTimeBasePage extends AbstractBasePage
