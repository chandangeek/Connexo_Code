/*
 * EictVeris.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.eictmodbusrtu.eictveris;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimpl.modbus.core.Modbus;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class EictVeris extends Modbus {

    private MultiplierFactory multiplierFactory = null;

    public EictVeris(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    protected void doTheConnect() throws IOException {
    }

    @Override
    protected void doTheDisConnect() throws IOException {
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getTypedProperty(PK_INTERFRAME_TIMEOUT, "25").trim()));
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getSlaveId()+", "+getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getAdditionalDataAsString();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-07-17 08:54:15 +0200 (Thu, 17 Jul 2014) $";
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    @Override
    public Date getTime() throws IOException {
        //return getRegisterFactory().findRegister("clock").dateValue();
        return new Date();
    }

    private MultiplierFactory getMultiplierFactory() throws IOException {
        if (multiplierFactory==null) {
            multiplierFactory = new MultiplierFactory(getFirmwareVersion());
            multiplierFactory.init();
        }
        return multiplierFactory;
    }

    @Override
    public BigDecimal getRegisterMultiplier(int address) throws IOException {
        return getMultiplierFactory().findMultiplier(address);
    }

}