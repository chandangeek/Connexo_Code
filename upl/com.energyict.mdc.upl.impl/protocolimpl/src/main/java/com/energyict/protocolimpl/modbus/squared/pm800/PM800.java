/*
 * PM800.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.squared.pm800;

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.functioncode.MandatoryDeviceIdentification;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 *
 * @author Koen
 */
public class PM800 extends Modbus  {

    private MultiplierFactory multiplierFactory = null;

    public PM800(PropertySpecService propertySpecService) {
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
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getTypedProperty(PK_INTERFRAME_TIMEOUT, "50").trim()));
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return getRegisterFactory().getFunctionCodeFactory().getMandatoryReadDeviceIdentification().toString();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-04-09 09:51:37 +0200 (Thu, 09 Apr 2015) $";
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    @Override
    public Date getTime() throws IOException {
        return getRegisterFactory().findRegister(3034).dateValue();
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

            MandatoryDeviceIdentification mdi = getRegisterFactory().getFunctionCodeFactory().getMandatoryReadDeviceIdentification();

            if ((mdi.getVendorName().toLowerCase().contains("square d")) && (mdi.getProductCode().contains("15210"))) {
                discoverResult.setDiscovered(true);
                discoverResult.setProtocolName(this.getClass().getName());
                discoverResult.setAddress(discoverTools.getAddress());
            }
            else {
                discoverResult.setDiscovered(false);
            }

            discoverResult.setResult(mdi.toString());
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

}