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

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverResult;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverTools;

import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.eictmodbusrtu.eictveris.RegisterFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
/**
 *
 * @author Koen
 */
public class EictModbusRtu extends Modbus {

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU Modbus";
    }

    @Inject
    public EictModbusRtu(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected void doTheConnect() throws IOException {

    }

    protected void doTheDisConnect() throws IOException {

    }

    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {

    }

    protected List<String> doTheGetOptionalKeys() {
        return Collections.emptyList();
    }

    protected void initRegisterFactory(){
        setRegisterFactory(new RegisterFactory(this));
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        return null;
    }

}