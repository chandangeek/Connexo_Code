package com.energyict.protocolimpl.modbus.enerdis.cdt;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * RecDigit Cct meter is a pulse counter.
 */

public abstract class RecDigitCdt extends Modbus {

    private BigDecimal ku;
    private BigDecimal ki;
    private BigDecimal kp;
    private BigDecimal ctRatio;
    private BigDecimal ptRatio;

    public RecDigitCdt(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected void doTheConnect() throws IOException { }
    protected void doTheDisConnect() throws IOException {}
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {

    	setInfoTypePhysicalLayer(Integer.parseInt(properties.getProperty("PhysicalLayer","1").trim()));
    	setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","100").trim()));

    }

    public String getFirmwareVersion() {
        return "unknown";
    }

    protected List<String> doTheGetOptionalKeys() {
        return Collections.emptyList();
    }

    /**
     * @param address   offset
     * @param length    nr of words
     * @return          int[] 2 bytes per int
     */
    int[] readRawValue(int address, int length)  throws IOException {

        HoldingRegister r = new HoldingRegister(address, length);
        r.setRegisterFactory(getRegisterFactory());
        return r.getReadHoldingRegistersRequest().getRegisters();

    }

    BigDecimal readValue(int address, Type type) throws IOException {

        int [] values = readRawValue( address, type.wordSize() );
        return getRecFactory().toBigDecimal(type, values);

    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return getRecFactory().toString();
    }

    public int getNumberOfChannels() throws IOException {
        return 2;
    }

    public RegisterFactoryCdtPr getRecFactory( ) {
        return (RegisterFactoryCdtPr)getRegisterFactory();
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        AbstractRegister r  = getRegisterFactory().findRegister(obisCode);
        return new RegisterInfo( r.getName() );
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        AbstractRegister r  = getRegisterFactory().findRegister(obisCode);
        String key          = r.getName();

        return r.registerValue(key);
    }

    /** Transformation coefficient V */
    BigDecimal getKU( ) throws IOException {
        if (ku == null) {
            ku = readValue(0x0006, Type.REAL_NUMBER);
        }
        return ku;
    }

    /** Transformation coefficient I */
    BigDecimal getKI( ) throws IOException {
        if (ki == null) {
            ki = readValue(0x000a, Type.REAL_NUMBER);
        }
        return ki;
    }

    /** Transformation coefficient P */
    BigDecimal getKP( ) throws IOException {
        if (kp == null) {
            kp = readValue(0x000e, Type.REAL_NUMBER);
        }
        return kp;
    }

    BigDecimal getCtRatio( ) throws IOException {
        if( ctRatio == null ) {
            ctRatio = readValue(0x19fa, Type.REAL_NUMBER);
        }
        return ctRatio;
    }

    BigDecimal getPtRatio( ) throws IOException {
        if( ptRatio == null ) {
            ptRatio = readValue(0x19fe, Type.REAL_NUMBER);
        }
        return ptRatio;
    }


}
