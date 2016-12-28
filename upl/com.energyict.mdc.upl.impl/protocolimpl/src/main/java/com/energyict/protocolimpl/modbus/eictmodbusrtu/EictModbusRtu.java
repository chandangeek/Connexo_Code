/*
 * EictRtuModbus.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.eictmodbusrtu;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.eictmodbusrtu.eictveris.RegisterFactory;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class EictModbusRtu extends Modbus {

    public EictModbusRtu(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected void doTheConnect() throws IOException {
    }

    @Override
    protected void doTheDisConnect() throws IOException {
    }

    @Override
    protected void initRegisterFactory(){
        setRegisterFactory(new RegisterFactory(this));
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public DiscoverResult discover(DiscoverTools discoverTools) {
        return null;
    }

}