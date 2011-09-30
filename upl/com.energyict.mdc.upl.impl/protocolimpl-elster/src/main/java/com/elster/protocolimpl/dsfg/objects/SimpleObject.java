package com.elster.protocolimpl.dsfg.objects;

import com.elster.protocolimpl.dsfg.ProtocolLink;


public class SimpleObject extends AbstractObject {

	/** The address of the object */
	private String startAddress;

	@Override
	protected String getStartAddress() {
		return startAddress;
	}

	public SimpleObject(ProtocolLink link, String startAddress) {
		super(link);
		this.startAddress = startAddress;
	}
}
