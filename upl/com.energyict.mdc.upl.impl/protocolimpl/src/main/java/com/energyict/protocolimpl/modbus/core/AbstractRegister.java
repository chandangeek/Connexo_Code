/*
 * AbstractRegister.java
 *
 * Created on 30 maart 2007, 17:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadDeviceIdentification;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadHoldingRegistersRequest;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadInputRegistersRequest;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadStatuses;
import com.energyict.protocolimpl.modbus.core.functioncode.ReportSlaveId;
import com.energyict.protocolimpl.modbus.core.functioncode.WriteMultipleCoils;
import com.energyict.protocolimpl.modbus.core.functioncode.WriteMultipleRegisters;
import com.energyict.protocolimpl.modbus.core.functioncode.WriteSingleCoil;
import com.energyict.protocolimpl.modbus.core.functioncode.WriteSingleRegister;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class AbstractRegister {


    private AbstractRegisterFactory registerFactory;

    private ObisCode obisCode;
    private int reg,range;
    private String name;
    private Unit unit;
    private String parser=null;

    private int scale;

    /** Creates a new instance of AbstractRegister */
    public AbstractRegister(int reg,int range,ObisCode obisCode) {
       this(reg,range,obisCode,Unit.get(""),obisCode.toString());
    }

    public AbstractRegister(int reg,int range,ObisCode obisCode,String description) {
       this(reg,range,obisCode,Unit.get(""),description);
    }

    public AbstractRegister(int reg,int range,ObisCode obisCode,Unit unit) {
       this(reg,range,obisCode,unit,obisCode.toString());
    }

    public AbstractRegister(int reg,int range,ObisCode obisCode,Unit unit,String name) {
        this.reg=reg;
        this.range=range;
        this.obisCode=obisCode;
        this.unit=unit;
        this.name=name;
    }

    public String toString() {
       return getObisCode()+", reg="+getReg()+", range="+getRange()+", unit="+getUnit()+", name="+getName();
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    public int getReg() {
        return reg;
    }

    public void setReg(int reg) {
        this.reg = reg;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public AbstractRegisterFactory getRegisterFactory() {
        return registerFactory;
    }

    public void setRegisterFactory(AbstractRegisterFactory registerFactory) {
        this.registerFactory = registerFactory;
    }

    public ReadHoldingRegistersRequest getReadHoldingRegistersRequest() throws IOException {
		final int register = getReg()-(getRegisterFactory().isZeroBased()?1:0);
		try {
			final ReadHoldingRegistersRequest request = getRegisterFactory().getFunctionCodeFactory().getReadHoldingRegistersRequest(register, getRange());
			return request;
		} catch (IOException e) {
			throw new NestedIOException(e, "IOException while reading holding register [" + register + "] with range [" + getRange() + "]");
		}
    }

    public ReadInputRegistersRequest getReadInputRegistersRequest() throws IOException {
		final int register = getReg()-(getRegisterFactory().isZeroBased()?1:0);
		try {
			final ReadInputRegistersRequest request = getRegisterFactory().getFunctionCodeFactory().getReadInputRegistersRequest(register, getRange());
			return request;
		} catch (IOException e) {
            throw new NestedIOException(e, "IOException while reading input register [" + register + "] with range [" + getRange() + "]");
		}
	}

    public ReadHoldingRegistersRequest getReadCoilStatusRequest() throws IOException {
		final int register = getReg()-(getRegisterFactory().isZeroBased()?1:0);
		try {
			return getRegisterFactory().getFunctionCodeFactory().getReadHoldingRegistersRequest(register, getRange());
		} catch (IOException e) {
            throw new NestedIOException(e, "IOException while reading coil status [" + register + "] with range [" + getRange() + "]");
		}
	}

    public WriteSingleRegister getWriteSingleRegister(int writeRegisterValue) throws IOException {
        final int register = getReg() - (getRegisterFactory().isZeroBased() ? 1 : 0);
        try {
            return getRegisterFactory().getFunctionCodeFactory().getWriteSingleRegister(register, writeRegisterValue);
        } catch (IOException e) {
            throw new NestedIOException(e, "IOException while writing register [" + register + "]");
        }
    }

    public WriteMultipleRegisters getWriteMultipleRegisters(byte[] registerValues) throws IOException {
        final int register = getReg() - (getRegisterFactory().isZeroBased() ? 1 : 0);
        try {
            return getRegisterFactory().getFunctionCodeFactory().getWriteMultipleRegisters(register, getRange(), registerValues);
        } catch (IOException e) {
            throw new NestedIOException(e, "IOException while writing register [" + register + "] with range [" + getRange() + "]");
        }
    }

    public WriteSingleCoil getWriteSingleCoil(int writeCoilValue) throws IOException {
        final int coil = getReg() - (getRegisterFactory().isZeroBased() ? 1 : 0);
        try {
            return getRegisterFactory().getFunctionCodeFactory().getWriteSingleCoil(coil, writeCoilValue);
        } catch (IOException e) {
            throw new NestedIOException(e, "IOException while writing coil [" + coil + "]");
        }
    }

    public WriteMultipleCoils getWriteMultipleCoils(byte[] coilValues) throws IOException {
        final int coil = getReg() - (getRegisterFactory().isZeroBased() ? 1 : 0);
        try {
            return getRegisterFactory().getFunctionCodeFactory().getWriteMultipleCoils(coil, getRange(), coilValues);
        } catch (IOException e) {
            throw new NestedIOException(e, "IOException while writing Coil [" + coil + "] with range [" + getRange() + "]");
        }
    }

    public ReportSlaveId getReportSlaveId() throws IOException {
        try {
            return getRegisterFactory().getFunctionCodeFactory().getReportSlaveId();
        } catch (IOException e) {
            throw new NestedIOException(e, "IOException while reading slave ID");
        }
    }

    public ReadStatuses getCoilStatuses() throws IOException {
        final int register = getReg() - (getRegisterFactory().isZeroBased() ? 1 : 0);
        try {
            return getRegisterFactory().getFunctionCodeFactory().getReadCoilStatuses(register, getRange());
        } catch (IOException e) {
            throw new NestedIOException(e, "IOException while reading coil statuses [" + register + "] with range [" + getRange() + "]");
        }
    }

    public ReadStatuses getInputStatuses() throws IOException {
        final int register = getReg() - (getRegisterFactory().isZeroBased() ? 1 : 0);
        try {
            return getRegisterFactory().getFunctionCodeFactory().getReadInputStatuses(register, getRange());
        } catch (IOException e) {
            throw new NestedIOException(e, "IOException while reading input statuses [" + register + "] with range [" + getRange() + "]");
        }
    }

    public ReadDeviceIdentification getReadDeviceIdentification(int readDeviceIdCode, int objectID) throws IOException {
        try {
            return getRegisterFactory().getFunctionCodeFactory().getReadDeviceIdentification(readDeviceIdCode, objectID);
        } catch (IOException e) {
            throw new NestedIOException(e, "IOException while reading device identification [" + objectID + "]");
        }
    }

    public Date dateValue() throws IOException {
        if (this instanceof HoldingRegister)
            return (Date)getRegisterFactory().getParserFactory().get(getParser()==null?"Date":getParser()).val(getReadHoldingRegistersRequest().getRegisters(), this);
        else if (this instanceof InputRegister)
            return (Date)getRegisterFactory().getParserFactory().get(getParser()==null?"Date":getParser()).val(getReadInputRegistersRequest().getRegisters(), this);
        throw new IOException ("AbstractRegister, dateValue(), invalid registertype "+this.getClass().getName());
    }

    public Object value() throws IOException {
        if (this instanceof HoldingRegister) {
            return getRegisterFactory().getParserFactory().get(getParser() == null ? "BigDecimal" : getParser()).val(getReadHoldingRegistersRequest().getRegisters(), this);
        } else if (this instanceof InputRegister) {
            return getRegisterFactory().getParserFactory().get(getParser() == null ? "BigDecimal" : getParser()).val(getReadInputRegistersRequest().getRegisters(), this);
        } else if (this instanceof ReportSlaveIDRegister) {
            return getReportSlaveId();
        } else if (this instanceof CoilStatusRegister) {
            return getCoilStatuses();
        } else if (this instanceof  InputStatusRegister) {
            return getInputStatuses();
        }

        throw new IOException("AbstractRegister, quantityValue(), invalid registertype " + this.getClass().getName());
    }
    public Quantity quantityValue() throws IOException {
        if (this instanceof HoldingRegister) {
            return new Quantity((BigDecimal) getRegisterFactory().getParserFactory().get(getParser() == null ? "BigDecimal" : getParser()).val(getReadHoldingRegistersRequest().getRegisters(), this), getUnit());
        } else if (this instanceof InputRegister) {
            return new Quantity((BigDecimal) getRegisterFactory().getParserFactory().get(getParser() == null ? "BigDecimal" : getParser()).val(getReadInputRegistersRequest().getRegisters(), this), getUnit());
        } else if (this instanceof ReportSlaveIDRegister) {
            return new Quantity(new BigDecimal(getReportSlaveId().getSlaveId()), Unit.getUndefined());
        }
        throw new IOException("AbstractRegister, quantityValue(), invalid registertype " + this.getClass().getName());
    }
    public Object objectValueWithParser(String key) throws IOException {
        if (this instanceof HoldingRegister)
            return getRegisterFactory().getParserFactory().get(key).val(getReadHoldingRegistersRequest().getRegisters(), this);
        else if (this instanceof InputRegister)
            return getRegisterFactory().getParserFactory().get(key).val(getReadInputRegistersRequest().getRegisters(), this);
        throw new IOException ("AbstractRegister, objectValueWithParser(), invalid registertype "+this.getClass().getName());

    }

    public int[] values() throws IOException {
        return getReadHoldingRegistersRequest().getRegisters();
    }


    public Quantity quantityValueWithParser(String key) throws IOException {
        if (this instanceof HoldingRegister)
            return new Quantity((BigDecimal)getRegisterFactory().getParserFactory().get(key).val(getReadHoldingRegistersRequest().getRegisters(), this),getUnit());
        else if (this instanceof InputRegister)
            return new Quantity((BigDecimal)getRegisterFactory().getParserFactory().get(key).val(getReadInputRegistersRequest().getRegisters(), this),getUnit());
        throw new IOException ("AbstractRegister, quantityValueWithParser(), invalid registertype "+this.getClass().getName());

    }

    public RegisterValue registerValue(String key) throws IOException {
        if (this instanceof HoldingRegister) {
            Parser p = getRegisterFactory().getParserFactory().get(parser);
            int [] value = getReadHoldingRegistersRequest().getRegisters();
            return (RegisterValue)p.val(value, this);
        }

        String msg = "AbstractRegister, registerValue(), invalid registertype " + this.getClass().getName();
        throw new ModbusException(msg);
    }

    public String getParser() {
        return parser;
    }

    public AbstractRegister setParser(String parser) {
        this.parser = parser;
        return this;
    }

    public int getScale() {
        return scale;
    }

    public AbstractRegister setScale(int scale) {
        this.scale = scale;
        return this;
    }


}
