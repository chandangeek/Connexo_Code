/*
 * MeterMultiplierDataRead.java
 *
 * Created on 10 november 2006, 14:40
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class MeterMultiplierDataRead extends AbstractDataRead {

	/** Creates a new instance of ConstantsDataRead */
	public MeterMultiplierDataRead(DataReadFactory dataReadFactory) {
		super(dataReadFactory);
	}

	protected void parse(byte[] data) throws IOException {
		getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
		System.out.println(ProtocolUtils.outputHexString(data));
	}

	protected void prepareBuild() throws IOException {
		long[] lids = new long[] { LogicalIDFactory.findLogicalId("METER_MULTIPLIER").getId(), LogicalIDFactory.findLogicalId("TRANSFORMER_MULTIPLIER").getId() };
		setDataReadDescriptor(new DataReadDescriptor(0x00, 0x02, lids));
	}
}
