/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TemplateCommand.java
 *
 * Created on 26 juli 2006, 17:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class RevisionLevelCommand extends AbstractCommand {

    private int rev;
    private int dialOut;
    private int type;


    /** Creates a new instance of ForceStatusCommand */
    public RevisionLevelCommand(CommandFactory cm) {
        super(cm);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RevisionLevelCommand:\n");
        strBuff.append("   rev="+getRev()+"\n");
        strBuff.append("   dialOut=0x"+Integer.toHexString(getDialOut())+"\n");
        strBuff.append("   type=0x"+Integer.toHexString(getType())+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setRev(ProtocolUtils.BCD2hex(data[offset++]));
        setDialOut(ProtocolUtils.getInt(data,offset++,1));
        setType(ProtocolUtils.getInt(data,offset,2));
        offset+=2;


    }

    protected CommandDescriptor getCommandDescriptor() {
        return new CommandDescriptor('v');
    }

    public int getRev() {
        return rev;
    }

    public void setRev(int rev) {
        this.rev = rev;
    }

    public int getDialOut() {
        return dialOut;
    }

    public void setDialOut(int dialOut) {
        this.dialOut = dialOut;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


}
