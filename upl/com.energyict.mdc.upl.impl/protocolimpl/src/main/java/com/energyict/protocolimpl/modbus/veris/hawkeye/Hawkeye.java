/*
 * Hawkeye.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.veris.hawkeye;

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.modbus.core.Modbus;

import java.io.IOException;
import java.util.Date;
/**
 *
 * @author Koen
 */
public class Hawkeye extends Modbus  {

    public Hawkeye(PropertySpecService propertySpecService) {
        super(propertySpecService);
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
        return "$Date: 2015-04-09 09:40:57 +0200 (Thu, 09 Apr 2015) $";
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    @Override
    public Date getTime() {
        return new Date();
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (obisCode.equals(ObisCode.fromString("0.0.96.1.0.255"))) {
            String slaveId = getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getAdditionalDataAsString();
            return new RegisterValue(obisCode, slaveId);
        } else {
            return super.readRegister(obisCode);
        }
    }

}