package com.energyict.protocolimpl.modbus.enerdis.cdt;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ModbusException;

import java.io.IOException;
import java.math.BigDecimal;

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

    @Override
    protected void doTheConnect() throws IOException { }

    @Override
    protected void doTheDisConnect() throws IOException {}

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
    	setInfoTypePhysicalLayer(Integer.parseInt(properties.getTypedProperty(PK_PHYSICAL_LAYER, "1").trim()));
    	setInfoTypeInterframeTimeout(Integer.parseInt(properties.getTypedProperty(PK_INTERFRAME_TIMEOUT, "100").trim()));
    }

    @Override
    public String getFirmwareVersion() {
        return "unknown";
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

    @Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return getRecFactory().toString();
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return 2;
    }

    public RegisterFactoryCdtPr getRecFactory( ) {
        return (RegisterFactoryCdtPr)getRegisterFactory();
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        AbstractRegister r  = getRegisterFactory().findRegister(obisCode);
        return new RegisterInfo( r.getName() );
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        AbstractRegister r  = getRegisterFactory().findRegister(obisCode);
        String key          = r.getName();

        try {
            return r.registerValue(key);
        } catch (ModbusException e) {
            getLogger().warning("Failed to read register " + obisCode.toString() + " - " + e.getMessage());
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
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