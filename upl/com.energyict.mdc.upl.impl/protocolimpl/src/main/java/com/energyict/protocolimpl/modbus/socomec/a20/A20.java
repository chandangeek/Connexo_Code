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

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;

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

    public A20(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
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
        propertySpecs.add(this.stringSpec("SocomecType", PropertyTranslationKeys.MODBUS_SOCOMECTYPE, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setInfoTypeInterframeTimeout(properties.getTypedProperty(PK_INTERFRAME_TIMEOUT, 50));
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
    public String getProtocolDescription() {
        return "Socomec Diris A20 Modbus";
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