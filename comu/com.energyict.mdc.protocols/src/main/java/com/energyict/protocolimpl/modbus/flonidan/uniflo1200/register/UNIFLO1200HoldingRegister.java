/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * UNIFLO1200HoldingRegister.java
 *
 * Created on 15-dec-2008, 11:40:55 by jme
 *
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadHoldingRegistersRequest;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadInputRegistersRequest;
import com.energyict.protocolimpl.modbus.core.functioncode.WriteMultipleRegisters;
import com.energyict.protocolimpl.modbus.core.functioncode.WriteSingleRegister;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers.UNIFLO1200Parsers;

import java.io.IOException;
import java.util.Date;

/**
 * @author jme
 *
 */
public class UNIFLO1200HoldingRegister extends HoldingRegister {

	private static final int DEBUG 				= 0;
	private int slaveID 						= 0;
	private int baseSlaveID 					= 0;
	private ModbusConnection modbusConnection 	= null;
	private boolean oddAddress;

	public UNIFLO1200HoldingRegister(int reg, int range, ObisCode obisCode,	Unit unit, String name, int slaveID, ModbusConnection modbusConnection) {
		super(reg, range, obisCode, unit, name);
		this.slaveID = slaveID;
		this.modbusConnection = modbusConnection;
		if (getModbusConnection() != null) this.baseSlaveID = getModbusConnection().getAddress();
	}

	public UNIFLO1200HoldingRegister(int registerAddress, int numberOfWords, String registerName, int slaveID, ModbusConnection modbusConnection) {
		super(registerAddress, numberOfWords, registerName);
		this.slaveID = slaveID;
		this.modbusConnection = modbusConnection;
    	if (getModbusConnection() != null) this.baseSlaveID = getModbusConnection().getAddress();
	}

	public UNIFLO1200HoldingRegister(int registerAddress, int numberOfWords, String registerName, ModbusConnection modbusConnection) {
		super(registerAddress, numberOfWords, registerName);
		this.modbusConnection = modbusConnection;
    	if (getModbusConnection() != null) this.baseSlaveID = getModbusConnection().getAddress();
    	genSlaveID();
	}

	public int getSlaveID() {
		return slaveID;
	}

	public void genSlaveID() {
		this.slaveID = (getReg() & 0x000F0000) >> 16;
		setReg(getReg() - (getReg() & 0x000F0000));
		setReg(getReg()/2);
		return;
	}

    private int getBaseSlaveID() {
    	return baseSlaveID;
	}

	private ModbusConnection getModbusConnection() {
		return modbusConnection;
	}

	private void defaultSlaveID() {
		getModbusConnection().setAddress(getBaseSlaveID());
	}

	private void activateSlaveID() {
		getModbusConnection().setAddress(getBaseSlaveID() + getSlaveID());
	}

	public void fixParser() {
		if (isOddAddress()) {
			if (getParser().equalsIgnoreCase(UNIFLO1200Parsers.PARSER_UINT8)) {
				setParser(UNIFLO1200Parsers.PARSER_UINT8_SWP);
				return;
			}
			if (getParser().equalsIgnoreCase(UNIFLO1200Parsers.PARSER_STR1)) {
				setParser(UNIFLO1200Parsers.PARSER_STR1_SWP);
				return;
			}
		}
	}

	public boolean isOddAddress(){
		return oddAddress;
	}

	public void setOddAddress(boolean isOddAddress) {
		this.oddAddress = isOddAddress;
	}

	public Date dateValue() throws IOException {
		activateSlaveID();
		Date returnValue = super.dateValue();
		defaultSlaveID();
		return returnValue;
	}

	public ReadHoldingRegistersRequest getReadHoldingRegistersRequest()	throws IOException {
		activateSlaveID();
		ReadHoldingRegistersRequest returnValue = super.getReadHoldingRegistersRequest();
		defaultSlaveID();
		return returnValue;
	}

	public ReadInputRegistersRequest getReadInputRegistersRequest()	throws IOException {
		activateSlaveID();
		ReadInputRegistersRequest returnValue = super.getReadInputRegistersRequest();
		defaultSlaveID();
		return returnValue;
	}

	public WriteMultipleRegisters getWriteMultipleRegisters(byte[] registerValues) throws IOException {
		activateSlaveID();
		WriteMultipleRegisters returnValue = super.getWriteMultipleRegisters(registerValues);
		defaultSlaveID();
		return returnValue;
	}

	public WriteSingleRegister getWriteSingleRegister(int writeRegisterValue) throws IOException {
		activateSlaveID();
		WriteSingleRegister returnValue = super.getWriteSingleRegister(writeRegisterValue);
		defaultSlaveID();
		return returnValue;
	}

	public Object objectValueWithParser(String key) throws IOException {
		activateSlaveID();
		Object returnValue = super.objectValueWithParser(key);
		defaultSlaveID();
		return returnValue;
	}

	public Quantity quantityValue() throws IOException {
		activateSlaveID();
		Quantity returnValue = super.quantityValue();
		defaultSlaveID();
		return returnValue;
	}

	public Quantity quantityValueWithParser(String key) throws IOException {
		activateSlaveID();
		Quantity returnValue = super.quantityValueWithParser(key);
		defaultSlaveID();
		return returnValue;
	}

	public RegisterValue registerValue(String key) throws IOException {
		activateSlaveID();
		RegisterValue returnValue = super.registerValue(key);
		defaultSlaveID();
		return returnValue;
	}

	public String toString() {
		return super.toString() + ", slaveIDOffset="+getSlaveID() + ", parser=" + getParser();
	}

	public Object value() throws IOException {
		activateSlaveID();
		Object returnValue = super.value();
		defaultSlaveID();
		return returnValue;
	}

	public int[] values() throws IOException {
		activateSlaveID();
		int[] returnValue = super.values();
		defaultSlaveID();
		return returnValue;
	}

}
