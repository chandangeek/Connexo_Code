/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class QuantityIdentificationDataRead extends AbstractDataRead {

	private long[] energyLids;
	private long[] demandLids;
	private long[] cumDemandLids;
	private long[] demandTOOLids;

	/** Creates a new instance of ConstantsDataRead */
	public QuantityIdentificationDataRead(DataReadFactory dataReadFactory) {
		super(dataReadFactory);
	}

	public String toString() {
		// Generated code by ToStringBuilder
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("QuantityIdentificationDataRead:\n");
		for (int i = 0; i < getEnergyLids().length; i++) {
			try {
				strBuff.append("       energyLids[" + i + "]=0x" + Long.toHexString(getEnergyLids()[i]) + ", "
						+ LogicalIDFactory.findLogicalId(getEnergyLids()[i]) + "\n");
			} catch (IOException e) {
				e.toString();
			}

		}
		for (int i = 0; i < getDemandLids().length; i++) {
			try {
				strBuff.append("       demandLids[" + i + "]=0x" + Long.toHexString(getDemandLids()[i]) + ", "
						+ LogicalIDFactory.findLogicalId(getDemandLids()[i]) + "\n");
			} catch (IOException e) {
				e.toString();
			}

		}
		for (int i = 0; i < getCumDemandLids().length; i++) {
			try {
				strBuff.append("       cumDemandLids[" + i + "]=0x" + Long.toHexString(getCumDemandLids()[i]) + ", "
						+ LogicalIDFactory.findLogicalId(getCumDemandLids()[i]) + "\n");
			} catch (IOException e) {
				e.toString();
			}
		}
		if (this.demandTOOLids != null) {
			for (int i = 0; i < getDemandTOOLids().length; i++) {
				try {
					strBuff.append("       demandTOOLids[" + i + "]=0x" + Long.toHexString(getDemandTOOLids()[i]) + ", "
							+ LogicalIDFactory.findLogicalId(getDemandTOOLids()[i]) + "\n");
				} catch (IOException e) {
					e.toString();
				}

			}
		}
		return strBuff.toString();
	}

	protected void parse(byte[] data) throws IOException {

		int offset = 0;
		int dataOrder = getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable()
		.getDataOrder();

		setEnergyLids(new long[getDataReadFactory().getCapabilitiesDataRead().getNumberOfEnergies()]);
		for (int i = 0; i < getDataReadFactory().getCapabilitiesDataRead().getNumberOfEnergies(); i++) {
			getEnergyLids()[i] = C12ParseUtils.getInt(data, offset, 4, dataOrder);
			offset += 4;
		}

		setDemandLids(new long[getDataReadFactory().getCapabilitiesDataRead().getNumberOfDemands()
		                       + getDataReadFactory().getCapabilitiesDataRead().getPFAvgBillingPeriodAvailable()]);
		for (int i = 0; i < (getDataReadFactory().getCapabilitiesDataRead().getNumberOfDemands() + getDataReadFactory().getCapabilitiesDataRead()
				.getPFAvgBillingPeriodAvailable()); i++) {
			getDemandLids()[i] = C12ParseUtils.getInt(data, offset, 4, dataOrder);
			offset += 4;
		}

		setCumDemandLids(new long[getDataReadFactory().getCapabilitiesDataRead().getNumberOfCumulativeDemands()]);
		for (int i = 0; i < getDataReadFactory().getCapabilitiesDataRead().getNumberOfCumulativeDemands(); i++) {
			getCumDemandLids()[i] = C12ParseUtils.getInt(data, offset, 4, dataOrder);
			offset += 4;
		}

		if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
			setDemandTOOLids(new long[getDataReadFactory().getCapabilitiesDataRead().getNumberOfDemands()]);
			for (int i = 0; i < (getDataReadFactory().getCapabilitiesDataRead().getNumberOfDemands()); i++) {
				getDemandTOOLids()[i] = C12ParseUtils.getInt(data, offset, 4, dataOrder);
				offset += 4;
			}
		}

	}

	protected void prepareBuild() throws IOException {
		long[] lids = null;

		if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
			lids = new long[] { LogicalIDFactory.findLogicalId("ALL_SEC_ENERGIES_TOTAL").getId(),
					LogicalIDFactory.findLogicalId("ALL_SEC_DEMANDS_TOTAL").getId(), LogicalIDFactory.findLogicalId("ALL_SEC_CUMS_TOTAL").getId(),
					LogicalIDFactory.findLogicalId("ALL_DEMAND_TOO_TOTAL").getId() };
		} else {
			lids = new long[] { LogicalIDFactory.findLogicalId("ALL_SEC_ENERGIES_TOTAL").getId(),
					LogicalIDFactory.findLogicalId("ALL_SEC_DEMANDS_TOTAL").getId(), LogicalIDFactory.findLogicalId("ALL_SEC_CUMS_TOTAL").getId() };
		}

		if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
			setDataReadDescriptor(new DataReadDescriptor(0x03, 0x04, lids));
		} else {
			setDataReadDescriptor(new DataReadDescriptor(0x03, 0x03, lids));
		}

	} // protected void prepareBuild() throws IOException

	public long[] getEnergyLids() {
		return this.energyLids;
	}

	public void setEnergyLids(long[] energyLids) {
		this.energyLids = energyLids;
	}

	public long[] getDemandLids() {
		return this.demandLids;
	}

	public void setDemandLids(long[] demandLids) {
		this.demandLids = demandLids;
	}

	public long[] getCumDemandLids() {
		return this.cumDemandLids;
	}

	public void setCumDemandLids(long[] cumDemandLids) {
		this.cumDemandLids = cumDemandLids;
	}

	public long[] getDemandTOOLids() {
		return this.demandTOOLids;
	}

	public void setDemandTOOLids(long[] demandTOOLids) {
		this.demandTOOLids = demandTOOLids;
	}

} // public class ConstantsDataRead extends AbstractDataRead
