package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.protocolimpl.base.ClockController;
import com.energyict.protocolimpl.base.ContactorController;
import com.energyict.protocolimpl.dlms.as220.AS220;

public class EMeter {

	private final AS220 as220;
	private final ClockController clockController;
	private final ContactorController contactorController;

	public EMeter(AS220 as220) {
		this.as220 = as220;
		this.clockController = new AS220ClockController(as220);
		this.contactorController = new AS220ContactorController(as220);
	}

	public ClockController getClockController() {
		return clockController;
	}

	public ContactorController getContactorController() {
		return contactorController;
	}

}
