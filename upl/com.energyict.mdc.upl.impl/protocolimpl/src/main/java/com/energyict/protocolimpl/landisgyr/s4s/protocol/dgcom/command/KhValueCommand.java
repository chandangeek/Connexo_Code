/*
 * TemplateCommand.java
 *
 * Created on 22 mei 2006, 15:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.command;

import com.energyict.mdc.upl.ProtocolException;

import com.energyict.protocolimpl.base.ParseUtils;

import java.math.BigDecimal;


/**
 *
 * @author Koen
 */
public class KhValueCommand extends AbstractCommand {

    private int khValue;
    private BigDecimal bdKhFactor;

    /** Creates a new instance of TemplateCommand */
    public KhValueCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("KhValuecommand:\n");
        strBuff.append("   bdKhFactor="+getBdKhFactor()+"\n");
        strBuff.append("   khValue="+getKhValue()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws ProtocolException {
        return new byte[]{(byte)0x6D,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws ProtocolException {
        int offset=0;
        setKhValue((int)ParseUtils.getBCD2LongLE(data, offset, 3));
        setBdKhFactor(BigDecimal.valueOf(getKhValue()));
        setBdKhFactor(getBdKhFactor().movePointLeft(3)); // divide by 1000
    }

    public int getKhValue() {
        return khValue;
    }

    public void setKhValue(int khValue) {
        this.khValue = khValue;
    }

    public BigDecimal getBdKhFactor() {
        return bdKhFactor;
    }

    public void setBdKhFactor(BigDecimal bdKhFactor) {
        this.bdKhFactor = bdKhFactor;
    }
}
