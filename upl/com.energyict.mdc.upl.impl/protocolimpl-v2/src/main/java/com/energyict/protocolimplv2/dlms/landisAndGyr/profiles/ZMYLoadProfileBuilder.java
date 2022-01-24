package com.energyict.protocolimplv2.dlms.landisAndGyr.profiles;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;

public class ZMYLoadProfileBuilder extends LoadProfileBuilder {

	public ZMYLoadProfileBuilder(AbstractDlmsProtocol meterProtocol) {
		super(meterProtocol, meterProtocol.getCollectedDataFactory(), meterProtocol.getIssueFactory());
	}
}
