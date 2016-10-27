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

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;
import com.energyict.protocolimpl.modbus.core.functioncode.FunctionCodeFactory;
import com.energyict.protocolimpl.modbus.eictmodbusrtu.eictveris.RegisterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
/**
 *
 * @author Koen
 */
public class EictModbusRtu extends Modbus {

    ModbusConnection modbusConnection;
    FunctionCodeFactory functionCodeFactory;

    /** Creates a new instance of EictRtuModbus */
    public EictModbusRtu() {
    }

    protected void doTheConnect() throws IOException {

    }

    protected void doTheDisConnect() throws IOException {

    }

    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {

    }


    protected List doTheGetOptionalKeys() {
        List result = new ArrayList();
        return result;
    }

    protected void initRegisterFactory(){
        setRegisterFactory(new RegisterFactory(this));
    }

    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        return null;
    }

}