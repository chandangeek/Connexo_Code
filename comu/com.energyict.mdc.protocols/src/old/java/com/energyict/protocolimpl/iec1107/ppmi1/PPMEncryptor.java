/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/* Created on Nov 19, 2004 2:05:35 PM */
package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.protocolimpl.base.Encryptor;

/**
 * PPM meters use a combination of a seed and an encrypted form of that seed to
 * protect the individual requests in a communication. The Encryptor interface
 * supports encrypting a string but not getting the next seed data
 * 
 * @author fbo
 */
public interface PPMEncryptor extends Encryptor {

	/**
	 * statefull version of encrypt, the object itself will hold
	 * a copy of the password.
	 */
	String encrypt(String key);

	/**
	 * 
	 * @return the next seed
	 */
	String getNextSeed();

}