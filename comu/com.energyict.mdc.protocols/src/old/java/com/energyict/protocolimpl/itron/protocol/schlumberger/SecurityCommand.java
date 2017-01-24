/*
 * SecurityCommand.java
 *
 * Created on 8 september 2006, 9:27
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol.schlumberger;

import com.energyict.protocolimpl.itron.protocol.SchlumbergerProtocol;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SecurityCommand extends AbstractCommand{

    private String securityCode=new String(new byte[]{0,0,0,0,0,0,0,0}); // 8 bytes security code default all 0's


    /** Creates a new instance of SecurityCommand */
    public SecurityCommand(SchlumbergerProtocol schlumbergerProtocol) {
        super(schlumbergerProtocol);
    }

    protected Command preparebuild() throws IOException {
        Command command = new Command('S');
        // ,unitType,unitId,
        byte[] data = new byte[8];
        if (getSecurityCode() != null)
            System.arraycopy(getSecurityCode().getBytes(), 0, data, 0, getSecurityCode().length());
        command.setData(data);
        return command;
    }

    protected void parse(byte[] data) throws IOException {
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }



}
