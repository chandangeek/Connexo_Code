/*
 * ConstantsDataRead.java
 *
 * Created on 2 november 2006, 16:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class TemplateDataRead extends AbstractDataRead {

	/** Creates a new instance of ConstantsDataRead */
	public TemplateDataRead(DataReadFactory dataReadFactory) {
		super(dataReadFactory);
	}

	protected void parse(byte[] data) throws IOException {
		getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
	}

	protected void prepareBuild() throws IOException {

		long[] lids = new long[]{LogicalIDFactory.findLogicalId("CT_MULTIPLIER").getId(),
				LogicalIDFactory.findLogicalId("VT_MULTIPLIER").getId(),
				LogicalIDFactory.findLogicalId("REGISTER_MULTIPLIER").getId(),
				LogicalIDFactory.findLogicalId("CUSTOMER_SERIAL_NUMBER").getId(),
				LogicalIDFactory.findLogicalId("PROGRAM_ID").getId(),
				LogicalIDFactory.findLogicalId("FIRMWARE_VERSION_REVISION").getId(),
				LogicalIDFactory.findLogicalId("DEMAND_INTERVAL_LENGTH").getId(),
				LogicalIDFactory.findLogicalId("ALL_SITESCAN").getId()};

		setDataReadDescriptor(new DataReadDescriptor(0x00, 0x08, lids));

	}

}
