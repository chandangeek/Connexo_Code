/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.siemenss4s.SiemensS4sRegisterDefinition;
import com.energyict.protocolimpl.iec1107.siemenss4s.SiemensS4sRegisterMapper;

import java.io.IOException;

public class S4sObjectFactory {

	private FlagIEC1107Connection 		iec1107Connection;
	private SiemensS4sRegisterMapper 	siemensS4sRegisterMapper;
	private S4sProfilePointer 			siemensProfilePointer;
	private S4sIntegrationPeriod 		siemensIntegrationPeriod;

	public S4sObjectFactory(FlagIEC1107Connection flagIEC1107Connection){
		this.iec1107Connection = flagIEC1107Connection;
	}

	/**
	 * Analyze the response for errors.
	 * @param response from the meter
	 * @return the same response back if OK
	 * @throws IOException when response contains an error
	 */
	private byte[] analyzeResponse(byte[] response) throws IOException{
		if(ProtocolUtils.stripBrackets(new String(response)).equalsIgnoreCase("ERR8")){
			throw new IOException("Read denied");
		} else if(ProtocolUtils.stripBrackets(new String(response)).equalsIgnoreCase("ERR8")){
			throw new IOException("Write denied");
		} else {
			return response;
		}
	}
	/**
	 * @return the RegisterMapper
	 */
	private SiemensS4sRegisterMapper getSiemensS4sRegisterMapper(){
		if(this.siemensS4sRegisterMapper == null){
			this.siemensS4sRegisterMapper = new SiemensS4sRegisterMapper();
		}
		return this.siemensS4sRegisterMapper;
	}

	/**
	 * @return the current connection
	 */
	private FlagIEC1107Connection getConnection(){
		return this.iec1107Connection;
	}

	/**
	 * Read the raw byteArray from a given register
	 * @param register the name of the register
	 * @return the byteArray containing the requested data
	 * @throws FlagIEC1107ConnectionException when sending or receiving failed
	 * @throws ConnectionException when sending or receiving failed
	 * @throws IOException if received contains an error
	 */
	private byte[] readRawRegister(String register) throws FlagIEC1107ConnectionException, ConnectionException, IOException{
		SiemensS4sRegisterDefinition ss4r = getSiemensS4sRegisterMapper().find(register);
		byte[] readCommand = ss4r.prepareRead();
		getConnection().sendRawCommandFrame(FlagIEC1107Connection.READ1, readCommand);
		byte[] readResponse = getConnection().receiveData();
		return analyzeResponse(readResponse);
	}

	/**
	 * Read a specific memoryBlock
	 * The preparedReadCommand should contain the memory Address, followed by an bracket NibbleLength
	 * ex. 2B40(40)
	 * @param preparedReadCommand - a self constructed byteArray
	 * @return the raw byteArray return from the device without brackets and checked for errors
	 * @throws FlagIEC1107ConnectionException
	 * @throws ConnectionException
	 * @throws IOException
	 */
	public byte[] readRawMemoryBlock(byte[] preparedReadCommand) throws FlagIEC1107ConnectionException, ConnectionException, IOException{
		getConnection().sendRawCommandFrame(FlagIEC1107Connection.READ1, preparedReadCommand);
		byte[] response = getConnection().receiveData();
		return analyzeResponse(response);
	}

	/**
	 * @return the meter his serialNumber object
	 * @throws FlagIEC1107ConnectionException
	 * @throws ConnectionException
	 * @throws IOException
	 */
	public S4sSerialNumber getSerialNumberObject() throws FlagIEC1107ConnectionException, ConnectionException, IOException {
		byte[] rrr = readRawRegister(SiemensS4sRegisterMapper.METER_SERIAL_NUMBER);
		return new S4sSerialNumber(rrr);
	}

	/**
	 * @return the meters current Date and Time
	 *
	 * @throws FlagIEC1107ConnectionException
	 * @throws ConnectionException
	 * @throws IOException
	 */
	public S4sDateTime getDateTimeObject() throws FlagIEC1107ConnectionException, ConnectionException, IOException {
    	byte[] date = readRawRegister(SiemensS4sRegisterMapper.DATE);
    	byte[] time = readRawRegister(SiemensS4sRegisterMapper.TIME);
		return new S4sDateTime(date, time);
	}

	/**
	 * @return the profileInterval object
	 * @throws FlagIEC1107ConnectionException
	 * @throws ConnectionException
	 * @throws IOException
	 */
	public S4sIntegrationPeriod getIntegrationPeriodObject() throws FlagIEC1107ConnectionException, ConnectionException, IOException{
		if(this.siemensIntegrationPeriod == null){
			byte[] period = readRawRegister(SiemensS4sRegisterMapper.PROFILE_INTERVAL);
			this.siemensIntegrationPeriod = new S4sIntegrationPeriod(period);
		}
		return this.siemensIntegrationPeriod;
	}

	/**
	 * @return the profile memory Pointer
	 * @throws FlagIEC1107ConnectionException
	 * @throws ConnectionException
	 * @throws IOException
	 */
	public S4sProfilePointer getProfilePointerObject() throws FlagIEC1107ConnectionException, ConnectionException, IOException {
		if(this.siemensProfilePointer == null){
			byte[] pointer = readRawRegister(SiemensS4sRegisterMapper.PROFILE_POINTER);
			this.siemensProfilePointer = new S4sProfilePointer(pointer);
		}
		return this.siemensProfilePointer;
	}

	/**
	 * @return the 4 channelInfo registers
	 * @throws FlagIEC1107ConnectionException
	 * @throws ConnectionException
	 * @throws IOException
	 */
	public byte[] getAllChannelInfosRawData() throws FlagIEC1107ConnectionException, ConnectionException, IOException {
		return readRawRegister(SiemensS4sRegisterMapper.TOTAL_CHANNEL_CONFIGURATION);
	}

	public S4sRegister getRegister(String name) throws FlagIEC1107ConnectionException, ConnectionException, IOException{
		byte[] rawData = readRawRegister(name);
		byte[] rawRegisterConfig = readRawRegister(getSiemensS4sRegisterMapper().convertRegisterNameToUnitName(name));
		S4sRegisterConfig config = new S4sRegisterConfig(rawRegisterConfig);
		S4sRegister register = new S4sRegister(rawData, config);
		return register;
	}

}
