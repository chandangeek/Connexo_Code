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

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverResult;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverTools;

import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.functioncode.MandatoryDeviceIdentification;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
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
public class PM800 extends Modbus  {

    @Override
    public String getProtocolDescription() {
        return "Schneider Electric SquareD PM800 Modbus";
    }

    private MultiplierFactory multiplierFactory=null;

    @Inject
    public PM800(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected void doTheConnect() throws IOException {

    }

    protected void doTheDisConnect() throws IOException {

    }

    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","50").trim()));
    }

    public String getFirmwareVersion() throws IOException {
        //return getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getSlaveId()+", "+getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getAdditionalDataAsString();
        return getRegisterFactory().getFunctionCodeFactory().getMandatoryReadDeviceIdentification().toString();
    }

    protected List<String> doTheGetOptionalKeys() {
        return Collections.emptyList();
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    public Date getTime() throws IOException {
        return getRegisterFactory().findRegister(3034).dateValue();
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
        catch(Exception e) {
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


    public BigDecimal getRegisterMultiplier(int address) throws IOException {
        return getMultiplierFactory().getMultiplier(address);
    }

    public MultiplierFactory getMultiplierFactory() {
        if (multiplierFactory == null) {
            multiplierFactory = new MultiplierFactory(this);
        }
        return multiplierFactory;
    }

}