/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.edf.trimarancje.core;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 * @author gna
 *
 */
public class ContractsTable extends AbstractTable{

	public ContractsTable(DataFactory dataFactory) {
		super(dataFactory);
	}

	protected int getCode() {
		return 12;
	}

	protected void parse(byte[] data) throws IOException {
		int offset = 0;

		while(true){
			if (offset == data.length){
				break;
			}

			int temp = ProtocolUtils.getIntLE(data,offset, 2); offset+=2;
			System.out.println(temp);
		}

	}

}