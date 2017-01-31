/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.modbus.core.functioncode.ReadDeviceIdentification;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadHoldingRegistersRequest;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadInputRegistersRequest;
import com.energyict.protocolimpl.modbus.core.functioncode.ReportSlaveId;
import com.energyict.protocolimpl.modbus.core.functioncode.WriteMultipleRegisters;
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
       this(reg,range,obisCode,Unit.get(""),obisCode.getDescription());
    }

    public AbstractRegister(int reg,int range,ObisCode obisCode,String description) {
       this(reg,range,obisCode,Unit.get(""),description);
    }

    public AbstractRegister(int reg,int range,ObisCode obisCode,Unit unit) {
       this(reg,range,obisCode,unit,obisCode.getDescription());
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
        return getRegisterFactory().getFunctionCodeFactory().getReadHoldingRegistersRequest(getReg()-(getRegisterFactory().isZeroBased()?1:0),getRange());
    }

    public ReadInputRegistersRequest getReadInputRegistersRequest() throws IOException {
        return getRegisterFactory().getFunctionCodeFactory().getReadInputRegistersRequest(getReg()-(getRegisterFactory().isZeroBased()?1:0),getRange());
    }

    public WriteSingleRegister getWriteSingleRegister(int writeRegisterValue) throws IOException {
        return getRegisterFactory().getFunctionCodeFactory().getWriteSingleRegister(getReg()-(getRegisterFactory().isZeroBased()?1:0), writeRegisterValue);
    }

    public WriteMultipleRegisters getWriteMultipleRegisters(byte[] registerValues) throws IOException {
        return getRegisterFactory().getFunctionCodeFactory().getWriteMultipleRegisters(getReg()-(getRegisterFactory().isZeroBased()?1:0), getRange(), registerValues);
    }

    public ReportSlaveId getReportSlaveId() throws IOException {
        return getRegisterFactory().getFunctionCodeFactory().getReportSlaveId();
    }

    public ReadDeviceIdentification getReadDeviceIdentification(int readDeviceIdCode, int objectID) throws IOException {
        return getRegisterFactory().getFunctionCodeFactory().getReadDeviceIdentification(readDeviceIdCode, objectID);
    }




    public Date dateValue() throws IOException {
        if (this instanceof HoldingRegister)
            return (Date)getRegisterFactory().getParserFactory().get(getParser()==null?"Date":getParser()).val(getReadHoldingRegistersRequest().getRegisters(), this);
        else if (this instanceof InputRegister)
            return (Date)getRegisterFactory().getParserFactory().get(getParser()==null?"Date":getParser()).val(getReadInputRegistersRequest().getRegisters(), this);
        throw new IOException ("AbstractRegister, dateValue(), invalid registertype "+this.getClass().getName());
    }
    public Object value() throws IOException {
        if (this instanceof HoldingRegister)
            return getRegisterFactory().getParserFactory().get(getParser()==null?"BigDecimal":getParser()).val(getReadHoldingRegistersRequest().getRegisters(), this);
        else if (this instanceof InputRegister)
            return getRegisterFactory().getParserFactory().get(getParser()==null?"BigDecimal":getParser()).val(getReadInputRegistersRequest().getRegisters(), this);
        throw new IOException ("AbstractRegister, quantityValue(), invalid registertype "+this.getClass().getName());
    }
    public Quantity quantityValue() throws IOException {
        if (this instanceof HoldingRegister)
            return new Quantity((BigDecimal)getRegisterFactory().getParserFactory().get(getParser()==null?"BigDecimal":getParser()).val(getReadHoldingRegistersRequest().getRegisters(), this),getUnit());
        else if (this instanceof InputRegister)
            return new Quantity((BigDecimal)getRegisterFactory().getParserFactory().get(getParser()==null?"BigDecimal":getParser()).val(getReadInputRegistersRequest().getRegisters(), this),getUnit());
        throw new IOException ("AbstractRegister, quantityValue(), invalid registertype "+this.getClass().getName());
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

    public RegisterValue registerValue(String key)
        throws IOException {

        if (this instanceof HoldingRegister) {

            Parser p = getRegisterFactory().getParserFactory().get(parser);
            int [] value = getReadHoldingRegistersRequest().getRegisters();
            return (RegisterValue)p.val(value, this);

        }

        String msg = "AbstractRegister, registerValue(), invalid registertype "
                     + this.getClass().getName();
        throw new IOException (msg);

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
