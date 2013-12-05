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

import java.io.IOException;


/**
 *
 * @author Koen
 */
public class ScaleFactorCommand extends AbstractCommand {

    private int scaleFactor;

    /** Creates a new instance of TemplateCommand */
    public ScaleFactorCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ScaleFactorCommand:\n");
        strBuff.append("   scaleFactor="+getScaleFactor()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        return new byte[]{(byte)0x8A,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setScaleFactor(((int)data[0]&0xFF)+1);

    }

    public int getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(int scaleFactor) {
        this.scaleFactor = scaleFactor;
    }
}
