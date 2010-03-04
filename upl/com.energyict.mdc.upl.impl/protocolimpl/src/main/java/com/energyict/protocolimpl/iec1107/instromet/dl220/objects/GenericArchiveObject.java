/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.objects;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * Implementation of a generic archive (LoadProfile)
 * 
 * TODO TOCOMPLETE!
 * 
 * @author gna
 * @since 4-mrt-2010
 *
 */
public class GenericArchiveObject extends AbstractObject {

	/** The startAddress of this object */
	private static String startAddress = null;
	
	/** The instance of the object */
	private int instance = 0;
	
	
	/**
	 * @param link
	 */
	public GenericArchiveObject(ProtocolLink link) {
		super(link);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getInitialAddress() {
		if(this.startAddress == null){
			throw new IllegalArgumentException("The initial address of the GenericArchive can't be NULL");
		}
		return this.startAddress;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getObjectInstance() {
		return instance;
	}

}
