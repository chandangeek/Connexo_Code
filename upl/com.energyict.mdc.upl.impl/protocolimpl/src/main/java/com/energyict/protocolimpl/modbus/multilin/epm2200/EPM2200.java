package com.energyict.protocolimpl.modbus.multilin.epm2200;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ModbusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 7/09/12
 * Time: 10:21
 */
public class EPM2200 extends Modbus {

    public EPM2200() {
    }

    protected void doTheConnect() throws IOException {
    }

    protected void doTheDisConnect() throws IOException {
    }

    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout", "50").trim()));
    }

    protected List doTheGetOptionalKeys() {
        List result = new ArrayList();
        return result;
    }

    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    /**
     * The protocol version date
     */
    public String getProtocolVersion() {
        return "$Date$";
    }

    public String getFirmwareVersion() throws IOException {
        Object firmwareVersion = getRegisterFactory().findRegister("FirmwareVersion").value();
        return (String) firmwareVersion;
    }

    public Date getTime() throws IOException {
        return new Date();
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            Object value = getRegisterFactory().findRegister(obisCode).value();
            if (value instanceof BigDecimal) {
                return new RegisterValue(obisCode, new Quantity((BigDecimal) value, getRegisterFactory().findRegister(obisCode).getUnit()));
            } else if (value instanceof String) {
                return new RegisterValue(obisCode, (String) value);
            }
            throw new NoSuchRegisterException();
        } catch (ModbusException e) {
            getLogger().warning("Failed to read register " + obisCode.toString() + " - " + e.getMessage());
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();

        try {
            setProperties(discoverTools.getProperties());
            if (getInfoTypeHalfDuplex() != 0) {
                setHalfDuplexController(discoverTools.getDialer().getHalfDuplexController());
            }
            init(discoverTools.getDialer().getInputStream(), discoverTools.getDialer().getOutputStream(), TimeZone.getTimeZone("ECT"), Logger.getLogger(EPM2200.class.toString()));
            connect();

            String meterName = (String) getRegisterFactory().findRegister("MeterName").value();
            String firmwareVersion = (String) getRegisterFactory().findRegister("FirmwareVersion").value();

            if ((meterName.toLowerCase().indexOf("shark 50") >= 0) && (firmwareVersion.indexOf("0051") >= 0)) {
                discoverResult.setDiscovered(true);
                discoverResult.setProtocolName(this.getClass().getName());
                discoverResult.setAddress(discoverTools.getAddress());
            } else {
                discoverResult.setDiscovered(false);
            }

            discoverResult.setResult(meterName);
            return discoverResult;
        } catch (Exception e) {
            discoverResult.setDiscovered(false);
            discoverResult.setResult(e.toString());
            return discoverResult;
        } finally {
            try {
                disconnect();
            } catch (IOException e) {
                // absorb
            }
        }
    }
}