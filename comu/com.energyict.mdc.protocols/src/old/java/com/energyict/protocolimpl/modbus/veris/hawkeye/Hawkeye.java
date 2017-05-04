/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverResult;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverTools;

import com.energyict.protocolimpl.modbus.core.Modbus;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;
/**
 *
 * @author Koen
 */
public class Hawkeye extends Modbus  {

    @Override
    public String getProtocolDescription() {
        return "Veris Hawkeye h80xx Modbus";
    }

    @Inject
    public Hawkeye(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected void doTheConnect() throws IOException {

    }

    protected void doTheDisConnect() throws IOException {

    }

    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","25").trim()));
    }

    protected List<String> doTheGetOptionalKeys() {
        return Collections.emptyList();
    }

    public String getFirmwareVersion() throws IOException {
        return getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getSlaveId()+", "+getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getAdditionalDataAsString();
    }

    public String getProtocolVersion() {
        return "$Revision: 1.5 $";
    }

    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    public Date getTime() throws IOException {
        //return getRegisterFactory().findRegister("clock").dateValue();
        return new Date();
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();

        try {
            setProperties(discoverTools.getProperties());
            if (getInfoTypeHalfDuplex() != 0) {
                setHalfDuplexController(null);
            }
            init(null, null, TimeZone.getTimeZone("ECT"), Logger.getLogger("name"));
            connect();

            String fwVersion = getFirmwareVersion();

            if (fwVersion.toLowerCase().contains("veris format")) {
                discoverResult.setDiscovered(true);
                discoverResult.setProtocolName("com.energyict.protocolimpl.modbus.eictmodbusrtu.eictveris.EictVeris");
                discoverResult.setAddress(discoverTools.getAddress());
            }
            else if (fwVersion.toLowerCase().contains("veris h8036")) {
                discoverResult.setDiscovered(true);
                discoverResult.setProtocolName("com.energyict.protocolimpl.modbus.veris.hawkeye.Hawkeye");
                discoverResult.setAddress(discoverTools.getAddress());
            }
            else {
                discoverResult.setDiscovered(false);
            }

            discoverResult.setResult(fwVersion);
            return discoverResult;
        }
        catch (Exception e) {
            discoverResult.setDiscovered(false);
            discoverResult.setResult(e.toString());
            return discoverResult;
        }
        finally {
           try {
              disconnect();
           }
           catch(IOException e) {
               // absorb
           }
        }
    }

}
