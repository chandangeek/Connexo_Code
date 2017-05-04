/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.instromet.v444.tables;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.instromet.connection.Command;
import com.energyict.protocolimpl.instromet.connection.Response;
import com.energyict.protocolimpl.instromet.v444.CommandFactory;

import java.io.IOException;

public abstract class AbstractTable {

	private TableFactory tableFactory;
	private int tableLength;

	public AbstractTable(TableFactory tableFactory) {
		this.tableFactory = tableFactory;
	}

	protected int getTableLength() {
		return tableLength;
	}

	public int getTableType() {
    	throw new RuntimeException("no implementation provided");
	}

	protected void readHeaders() throws IOException {
		//System.out.println("read headers");
		CommandFactory commandFactory =
			getTableFactory().getCommandFactory();
		Response response =
			commandFactory.readHeadersCommand().invoke();
		parseStatus(response);
		parseHeaders(response);
	}

	abstract protected void parse(byte[] data) throws IOException;


    protected TableFactory getTableFactory() {
        return tableFactory;
    }


    protected void build() throws IOException {
    	prepareBuild();
        doBuild();
    }

    protected void prepareBuild() throws IOException {

    }

    protected void doBuild() throws IOException {}

    protected void parseStatus(Response response) throws IOException {
    	tableFactory.getInstromet444().parseStatus(response);
    }

    protected boolean initParseWrite(Response response) throws IOException {
    	byte[] data = response.getData();
    	if (data.length < 1)
    		return false;
    	char function = (char) data[0];
    	Command command = new Command(function);
    	if (command.isWriteCommand()) {
    		if (data.length < 4)
    			throw new IOException("Invalid data write from corrector");
    		return true;
    	}
    	return false;
    }

    protected void parseWrite(Response response) throws IOException {
    	boolean isWrite = initParseWrite(response);
    	if (!isWrite)
    		return;
    	byte[] data = response.getData();
    	//function + 4 bytes start address + 2 bytes length
    	parse(ProtocolUtils.getSubArray2(data, 7, data.length-7));
    }

    protected void parseHeaders(Response response) throws IOException {
    	boolean isWrite = initParseWrite(response);
    	if (!isWrite)
    		return;
    	byte[] data = response.getData();
    	byte[] realData = ProtocolUtils.getSubArray2(data, 7, 5);
    	//System.out.println("realData " + ProtocolUtils.outputHexString(realData));
    	int tableType = (int) realData[0];
    	checkTableType(tableType);
    	tableLength = ProtocolUtils.getIntLE(realData, 1, 4);
    	//System.out.println("tableLength =  " + tableLength);
    }

    protected void checkTableType(int type) throws IOException {
    	int tableType = getTableTypeReturned();
    	if (type != tableType)
    		throw new IOException(
    				"Unexpected table type: " + type
    				+ ", should be " + tableType);
    	//System.out.println("tableType ok after table switch");
    }

    protected int getTableTypeReturned() {
    	return this.getTableType();
    }



}
