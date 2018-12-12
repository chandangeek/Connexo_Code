/**
 * 
 */
package com.elster.protocolimpl.lis200.objects;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

/**
 * @author gna
 * @since 10-feb-2010
 * 
 */
public class SoftwareVersionObject extends AbstractObject {

	/** The startAddress of this object */
	private final String startAddress = "0190.0";

	/** The instance of the object */
	private final int instance = 2;

    /** buffered value, because software version doesn't change during multiple reads in device ;-) */
    private String val = null;
	/**
	 * @param link
	 */
	public SoftwareVersionObject(ProtocolLink link) {
		super(link);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getInitialAddress() {
		return startAddress;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getObjectInstance() {
		return instance;
	}

    /**
     * getValue of software version number
     *
     * only read once...
     */
    @Override
    public String getValue() throws IOException {
        if (val == null) {
            val = super.getValue();
        }
        return val;
    }

}
