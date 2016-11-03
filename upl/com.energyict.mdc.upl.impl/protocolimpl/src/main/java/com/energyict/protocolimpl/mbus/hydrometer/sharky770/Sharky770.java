/*
 * Sharky770.java
 *
 * Created on 2 oktober 2007, 10:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.hydrometer.sharky770;

import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.mbus.core.MBus;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author kvds
 */
public class Sharky770 extends MBus {

    @Override
    public DiscoverResult discover(DiscoverTools discoverTools) {
        // discovery is implemented in the GenericModbusDiscover protocol
        return null;
    }

    @Override
    public Date getTime() throws IOException {
        return getRegisterFactory().getTime();
    }

    @Override
    protected void doTheConnect() throws IOException {
        getMBusConnection().sendSND_NKE();
    }

    @Override
    protected void doTheDisConnect() throws IOException {
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