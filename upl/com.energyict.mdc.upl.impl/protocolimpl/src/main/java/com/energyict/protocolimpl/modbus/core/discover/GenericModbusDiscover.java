/*
 * GenericModbusDiscover.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.discover;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimpl.modbus.core.Modbus;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
@Deprecated //Never released, technical class
public class GenericModbusDiscover extends Modbus {

    final int DEBUG=0;

    public GenericModbusDiscover(PropertySpecService propertySpecService, NlsService nlsService) {
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
        return "THIS PROTOCOL IS ONLY FOR DISCOVERY";
    }

    @Override
    public String getProtocolVersion() {
        return "$Revision: 1.4 $";
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    @Override
    public Date getTime() throws IOException {
        return new Date();
    }

}