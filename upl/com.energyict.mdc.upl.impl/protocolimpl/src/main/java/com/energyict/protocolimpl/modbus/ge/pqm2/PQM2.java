/*
 * PQM2.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.ge.pqm2;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.modbus.core.Modbus;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class PQM2 extends Modbus implements SerialNumberSupport {

    public PQM2(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected void doTheConnect() throws IOException {
    }

    @Override
    protected void doTheDisConnect() throws IOException {
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return (String)getRegisterFactory().findRegister("firmware version").objectValueWithParser("firmware version");
    }

    @Override
    public String getSerialNumber() {
        try {
            return (String)getRegisterFactory().findRegister("SerialNumber").value();
        } catch (IOException e){
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:42 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    @Override
    public Date getTime() throws IOException {
        return getRegisterFactory().findRegister("clock").dateValue();
        //return new Date();
    }

}