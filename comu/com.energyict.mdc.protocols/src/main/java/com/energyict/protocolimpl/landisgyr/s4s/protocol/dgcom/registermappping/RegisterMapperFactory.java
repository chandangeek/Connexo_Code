/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterMapperFactory.java
 *
 * Created on 12 juni 2006, 15:55
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.registermappping;

import com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.S4s;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class RegisterMapperFactory {

    S4s s4s;
    RegisterMapper registerMapper=null;

    /** Creates a new instance of RegisterMapperFactory */
    public RegisterMapperFactory(S4s s4s) {
        this.s4s=s4s;
    }


    public RegisterMapper getRegisterMapper() throws IOException {
        if (registerMapper == null) {
            if (s4s.getCommandFactory().getFirmwareVersionCommand().isRX()) {
                registerMapper = new RegisterMapperRX(s4s);
            }
            else if (s4s.getCommandFactory().getFirmwareVersionCommand().isDX()) {
                registerMapper =new RegisterMapperDX(s4s);
            }
        }
        return registerMapper;
    }

}
