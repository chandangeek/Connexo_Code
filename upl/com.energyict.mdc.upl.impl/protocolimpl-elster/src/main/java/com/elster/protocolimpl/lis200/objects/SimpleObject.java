package com.elster.protocolimpl.lis200.objects;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

public class SimpleObject extends AbstractObject {

	/** The address of the object */
	protected String startAddress;

	/** The instance of the object */
	protected int instance;

	@Override
	protected String getInitialAddress() {
		return startAddress;
	}

	@Override
	protected int getObjectInstance() {
		return instance;
	}

	public SimpleObject(ProtocolLink link, int instance, String startAddress) {
		super(link);
		this.startAddress = startAddress;
		this.instance = instance;
	}

	public SimpleObject(ProtocolLink link, String address) {
		super(link);
		String[] addressPart = address.split(":");
		if (addressPart.length > 1) {
			instance = Integer.parseInt(addressPart[0]);
     		startAddress = addressPart[1];
		}
		else {
			instance = 1;
     		startAddress = addressPart[0];
		}	
	}
}
