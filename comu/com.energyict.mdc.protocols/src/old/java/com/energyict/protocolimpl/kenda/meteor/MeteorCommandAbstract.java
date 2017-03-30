/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.meteor;

public interface MeteorCommandAbstract {

	String toString();

	void printData();

	byte[] parseToByteArray();

}
