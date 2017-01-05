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

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.Modbus;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Koen
 */
public class A20 extends Modbus {

    private MultiplierFactory multiplierFactory=null;
    private String socomecType;

    public A20(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected void doTheConnect() throws IOException {
    }

    @Override
    protected void doTheDisConnect() throws IOException {
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(this.stringSpec("SocomecType", false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getTypedProperty(PK_INTERFRAME_TIMEOUT, "50").trim()));
        setSocomecType(properties.getTypedProperty("SocomecType"));
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "unknown";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-04-09 09:16:13 +0200 (Thu, 09 Apr 2015) $";
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    @Override
    public Date getTime() throws IOException {
        return new Date();
    }

    @Override
    public DiscoverResult discover(DiscoverTools discoverTools) {
        // discovery is implemented in the GenericModbusDiscover protocol
        return null;
    }

    @Override
    public BigDecimal getRegisterMultiplier(int address) throws IOException {
        return getMultiplierFactory().getMultiplier(address);
    }

    private MultiplierFactory getMultiplierFactory() {
        if (multiplierFactory == null) {
            multiplierFactory = new MultiplierFactory(this);
        }
        return multiplierFactory;
    }

    String getSocomecType() {
        return socomecType;
    }

    private void setSocomecType(String socomecType) {
        this.socomecType = socomecType;
    }

}