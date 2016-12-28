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
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.Modbus;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;
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
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        super.setProperties(properties);
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

    @Override
    public DiscoverResult discover(DiscoverTools discoverTools) {
        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();

        try {
            setProperties(com.energyict.cpo.TypedProperties.copyOf(discoverTools.getProperties()));
            if (getInfoTypeHalfDuplex() != 0) {
                setHalfDuplexController(discoverTools.getDialer().getHalfDuplexController());
            }
            init(discoverTools.getDialer().getInputStream(),discoverTools.getDialer().getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
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
        } finally {
           try {
              disconnect();
           }
           catch(IOException e) {
               // absorb
           }
        }
    }

}