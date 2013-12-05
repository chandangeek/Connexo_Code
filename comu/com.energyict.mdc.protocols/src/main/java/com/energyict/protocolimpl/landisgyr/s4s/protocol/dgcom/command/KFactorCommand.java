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

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.math.BigDecimal;


/**
 *
 * @author Koen
 */
public class KFactorCommand extends AbstractCommand {

    private int kFactor;
    private BigDecimal bdKFactor;

    /** Creates a new instance of TemplateCommand */
    public KFactorCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("KFactorCommand:\n");
        strBuff.append("   KFactor="+getKFactor()+"\n");
        strBuff.append("   bdKFactor="+getBdKFactor()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        return new byte[]{(byte)0x1F,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setKFactor((int)ParseUtils.getBCD2LongLE(data, offset, 3));
        setBdKFactor(BigDecimal.valueOf(getKFactor()));
        setBdKFactor(getBdKFactor().movePointLeft(3)); // divide by 1000
        setBdKFactor(getBdKFactor().divide(BigDecimal.valueOf(getCommandFactory().getScaleFactorCommand().getScaleFactor()),BigDecimal.ROUND_HALF_UP)); // divide by scalefactor
    }

    public int getKFactor() {
        return kFactor;
    }

    private void setKFactor(int kFactor) {
        this.kFactor = kFactor;
    }

    public BigDecimal getBdKFactor() {
        return bdKFactor;
    }

    private void setBdKFactor(BigDecimal bdKFactor) {
        this.bdKFactor = bdKFactor;
    }
}
