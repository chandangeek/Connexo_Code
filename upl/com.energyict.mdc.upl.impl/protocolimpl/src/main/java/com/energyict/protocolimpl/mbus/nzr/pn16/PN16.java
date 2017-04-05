/*
 * PN16.java
 *
 * Created on 2 oktober 2007, 10:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.nzr.pn16;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.mbus.core.MBus;

import java.io.IOException;

/**
 *
 * @author kvds
 */
public class PN16 extends MBus {

    public PN16(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    protected void doTheConnect() throws IOException {
        getMBusConnection().sendSND_NKE();
    }

    @Override
    protected void doTheDisConnect() throws IOException {

    }

    @Override
    public String getProtocolDescription() {
        return "NZR PN16 Mbus";
    }

    @Override
    public String getFirmwareVersion() {
        return "NOT YET IMPLEMENTED";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-06-26 10:11:31 +0200 (Fri, 26 Jun 2015) $";
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

}