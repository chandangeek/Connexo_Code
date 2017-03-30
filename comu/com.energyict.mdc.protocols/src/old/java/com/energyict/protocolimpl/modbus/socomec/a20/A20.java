/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * A20.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.socomec.a20;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverResult;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverTools;

import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
/**
 *
 * @author Koen
 */
public class A20 extends Modbus {

    @Override
    public String getProtocolDescription() {
        return "Socomec Diris A20 Modbus";
    }

    ModbusConnection modbusConnection;
    private RegisterFactory registerFactory;
    private MultiplierFactory multiplierFactory=null;
    private String socomecType;

    @Inject
    public A20(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected void doTheConnect() throws IOException {

    }

    protected void doTheDisConnect() throws IOException {

    }

    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","50").trim()));
        setSocomecType(properties.getProperty("SocomecType"));
    }

    public String getFirmwareVersion() throws IOException {
        return "unknown";
    }

    protected List<String> doTheGetOptionalKeys() {
        return Collections.singletonList("SocomecType");
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    public Date getTime() throws IOException {
        return new Date();
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        // discovery is implemented in the GenericModbusDiscover protocol
        return null;
    }


    public BigDecimal getRegisterMultiplier(int address) throws IOException {
        return getMultiplierFactory().getMultiplier(address);
    }

    public MultiplierFactory getMultiplierFactory() {
        if (multiplierFactory == null) {
            multiplierFactory = new MultiplierFactory(this);
        }
        return multiplierFactory;
    }

    public String getSocomecType() {
        return socomecType;
    }

    private void setSocomecType(String socomecType) {
        this.socomecType = socomecType;
    }

}