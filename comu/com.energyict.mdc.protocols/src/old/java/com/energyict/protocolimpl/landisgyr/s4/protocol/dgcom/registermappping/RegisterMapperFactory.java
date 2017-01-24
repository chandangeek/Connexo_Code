/*
 * RegisterMapperFactory.java
 *
 * Created on 12 juni 2006, 15:55
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.registermappping;

import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.S4;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class RegisterMapperFactory {

    S4 s4;
    RegisterMapper registerMapper=null;

    /** Creates a new instance of RegisterMapperFactory */
    public RegisterMapperFactory(S4 s4) {
        this.s4=s4;
    }


    public RegisterMapper getRegisterMapper() throws IOException {
        if (registerMapper == null) {
            if (s4.getCommandFactory().getFirmwareVersionCommand().isRX()) {
                registerMapper = new RegisterMapperRX(s4);
            }
            else if (s4.getCommandFactory().getFirmwareVersionCommand().isDX()) {
                registerMapper =new RegisterMapperDX(s4);
            }
        }
        return registerMapper;
    }

}
