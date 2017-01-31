/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ClassFactory.java
 *
 * Created on 12 juli 2005, 10:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.protocolimpl.elster.alpha.core.Alpha;
import com.energyict.protocolimpl.elster.alpha.core.connection.CommandFactory;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ClassFactory {

	private Alpha alpha;

	// uncached
	private Class9Status1 class9Status1=null; // current time/date is in this class
	private Class17LoadProfileData class17LoadProfileData=null;
	private Class16LoadProfileHistory class16LoadProfileHistory=null;

	// cached lazy initialized classes
	private Class0ComputationalConfiguration class0ComputationalConfiguration=null;
	private Class2IdentificationAndDemandData class2IdentificationAndDemandData=null;
	private Class6MeteringFunctionBlock class6MeteringFunctionBlock=null;
	private Class7MeteringFunctionBlock class7MeteringFunctionBlock=null;
	private Class8FirmwareConfiguration class8FirmwareConfiguration=null;
	private Class33ModemConfigurationInfo class33ModemConfigurationInfo=null;
	private Class11BillingData class11BillingData=null;
	private Class12PreviousMonthBillingData class12PreviousMonthBillingData=null;
	private Class13PreviousSeasonBillingData class13PreviousSeasonBillingData=null;
	private Class14LoadProfileConfiguration class14LoadProfileConfiguration=null;
	private Class10Status2 class10Status2=null;
	private Class15TimeAdjustHistory class15TimeAdjustHistory=null;


	/** Creates a new instance of ClassFactory */
	public ClassFactory(Alpha alpha) {
		this.alpha=alpha;
	}

	public CommandFactory getCommandFactory() {
		return this.alpha.getCommandFactory();
	}

	public Alpha getAlpha() {
		return this.alpha;
	}

	// **************************************************************************************************
	// uncached classes
	public Class9Status1 getClass9Status1() throws IOException {
		Class9Status1 class9Status1 = new Class9Status1(this);
		class9Status1.build();
		return class9Status1;
	}

	public Class17LoadProfileData getClass17LoadProfileData(int nrOfDays) throws IOException {
		//getAlphaPlus().getCommandFactory().getFunctionWithDataCommand().PacketSize(4);
		Class17LoadProfileData class17LoadProfileData = new Class17LoadProfileData(this);
		class17LoadProfileData.setNrOfDays(nrOfDays);
		class17LoadProfileData.build();
		return class17LoadProfileData;
	}

	public Class16LoadProfileHistory getClass16LoadProfileHistory() throws IOException {
		Class16LoadProfileHistory class16LoadProfileHistory = new Class16LoadProfileHistory(this);
		class16LoadProfileHistory.build();
		return class16LoadProfileHistory;
	}

	// **************************************************************************************************
	// cached lazy initialized classes
	public Class0ComputationalConfiguration getClass0ComputationalConfiguration() throws IOException {
		if (this.class0ComputationalConfiguration == null) {
			this.class0ComputationalConfiguration = new Class0ComputationalConfiguration(this);
			this.class0ComputationalConfiguration.build();
		}
		return this.class0ComputationalConfiguration;
	}

	public Class2IdentificationAndDemandData getClass2IdentificationAndDemandData() throws IOException {
		if (this.class2IdentificationAndDemandData == null) {
			this.class2IdentificationAndDemandData = new Class2IdentificationAndDemandData(this);
			this.class2IdentificationAndDemandData.build();
		}
		return this.class2IdentificationAndDemandData;
	}

	public Class6MeteringFunctionBlock getClass6MeteringFunctionBlock() throws IOException {
		if (this.class6MeteringFunctionBlock == null) {
			this.class6MeteringFunctionBlock = new Class6MeteringFunctionBlock(this);
			this.class6MeteringFunctionBlock.build();
		}
		return this.class6MeteringFunctionBlock;
	}

	public long getSerialNumber() throws IOException {
		this.class7MeteringFunctionBlock = new Class7MeteringFunctionBlock(this);
		this.class7MeteringFunctionBlock.discoverSerialNumber();
		this.class7MeteringFunctionBlock.build();
		return this.class7MeteringFunctionBlock.getXMTRSN();
	}

	public Class7MeteringFunctionBlock getClass7MeteringFunctionBlock() throws IOException {
		if (this.class7MeteringFunctionBlock == null) {
			this.class7MeteringFunctionBlock = new Class7MeteringFunctionBlock(this);
			this.class7MeteringFunctionBlock.build();
		}
		return this.class7MeteringFunctionBlock;
	}

	public Class8FirmwareConfiguration getClass8FirmwareConfiguration() throws IOException {
		if (this.class8FirmwareConfiguration == null) {
			this.class8FirmwareConfiguration = new Class8FirmwareConfiguration(this);
			this.class8FirmwareConfiguration.build();
		}
		return this.class8FirmwareConfiguration;
	}

	public Class33ModemConfigurationInfo getClass33ModemConfigurationInfo() throws IOException {
		if (this.class33ModemConfigurationInfo == null) {
			this.class33ModemConfigurationInfo = new Class33ModemConfigurationInfo(this);
			this.class33ModemConfigurationInfo.build();
		}
		return this.class33ModemConfigurationInfo;
	}
	public Class11BillingData getClass11BillingData() throws IOException {
		if (this.class11BillingData == null) {
			this.class11BillingData = new Class11BillingData(this);
			this.class11BillingData.build();
		}
		return this.class11BillingData;
	}
	public Class12PreviousMonthBillingData getClass12PreviousMonthBillingData() throws IOException {
		if (this.class12PreviousMonthBillingData == null) {
			this.class12PreviousMonthBillingData = new Class12PreviousMonthBillingData(this);
			this.class12PreviousMonthBillingData.build();
		}
		return this.class12PreviousMonthBillingData;
	}
	public Class13PreviousSeasonBillingData getClass13PreviousSeasonBillingData() throws IOException {
		if (this.class13PreviousSeasonBillingData == null) {
			this.class13PreviousSeasonBillingData = new Class13PreviousSeasonBillingData(this);
			this.class13PreviousSeasonBillingData.build();
		}
		return this.class13PreviousSeasonBillingData;
	}
	public Class14LoadProfileConfiguration getClass14LoadProfileConfiguration() throws IOException {
		if (this.class14LoadProfileConfiguration == null) {
			this.class14LoadProfileConfiguration = new Class14LoadProfileConfiguration(this);
			this.class14LoadProfileConfiguration.build();
		}
		return this.class14LoadProfileConfiguration;
	}
	public Class10Status2 getClass10Status2() throws IOException {
		if (this.class10Status2 == null) {
			this.class10Status2 = new Class10Status2(this);
			this.class10Status2.build();
		}
		return this.class10Status2;
	}

	public Class15TimeAdjustHistory getClass15TimeAdjustHistory() throws IOException {
		if (this.class15TimeAdjustHistory == null) {
			this.class15TimeAdjustHistory = new Class15TimeAdjustHistory(this);
			this.class15TimeAdjustHistory.build();
		}
		return this.class15TimeAdjustHistory;
	}
}
