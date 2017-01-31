/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * 
 */
package com.energyict.protocolimpl.base;

import java.io.IOException;

/**
 * Interface for commission/decommission related functionality
 * 
 * @author gna
 * @since 16-feb-2010
 *
 */
public interface MbusInstallController {
	
	/**
	 * Command to install an MBus meter <i>(Commission)</i>
	 * @throws IOException if something goes wrong during installation
	 */
	void install() throws IOException;
	
	/**
	 * Command to deinstall an MBus meter <i>(DeCommission)</i>
	 * @throws IOException if something goes wrong during installation
	 */
	void deinstall() throws IOException;
	
	/**
	 * Command to send an OPEN key to the meter.
	 * The OPEN key is used, together with the default key, to generate a TRANSFER key
	 * 
	 * @param encryptionKey 
	 * 					- the OPEN encryption key to send to the meter
	 * 
	 * @throws IOException if setting the key didn't succeed
	 */
	void setEncryptionKey(byte[] encryptionKey) throws IOException;
	
	/**
	 * Command to send a TRANSFER key to the meter.
	 * The TRANSFER key is generated by encrypting the OPEN key with the DEFAULT key
	 * 
	 * @param transferKey 
	 * 					- the TRANSFER key to send to the meter
	 * 
	 * @throws IOException if setting the key didn't succeed
	 */
	void setTransferKey(byte[] transferKey) throws IOException;

}
