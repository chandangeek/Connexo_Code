/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AbstractClass.java
 *
 * Created on 11 juli 2005, 16:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.protocolimpl.elster.alpha.core.connection.ResponseFrame;

import java.io.IOException;


/**
 *
 * @author Koen
 */
abstract public class AbstractClass {

	abstract protected void parse(byte[] data) throws IOException;
	abstract protected ClassIdentification getClassIdentification();

	//byte[] data;
	private ClassFactory classFactory;

	/** Creates a new instance of AbstractClass */
	public AbstractClass(ClassFactory classFactory) {
		this.classFactory=classFactory;
	}

	protected void prepareBuild() throws IOException {
		// override to provide extra functionality...
	}

	public void build() throws IOException {
		prepareBuild();
		ResponseFrame responseFrame = this.classFactory.getCommandFactory().getClassReadCommand().readClass(getClassIdentification().getId(),getClassIdentification().getLength(),getClassIdentification().isMultipleClass());
		if (getClassIdentification().isVerify()) {
			verifyChecksum(responseFrame.getData());
		}
		parse(responseFrame.getData());
	}

	protected void verifyChecksum(byte[] data) throws IOException {
		int checksum = 0;
		for (int i=0;i<data.length-1;i++) {
			checksum += data[i];
		}
		checksum= ((checksum&0xFF)^0xFF);

		// KV_DEBUG temporary removed!!!
		if (checksum != (data[data.length-1]&0xFF)) {
			throw new IOException("AbstractClass, verifyChecksum(), Application layer class checksum wrong! (0x"+Integer.toHexString(checksum)+"!=0x"+Integer.toHexString(data[data.length-1])+")");
		}
	}

	protected ClassFactory getClassFactory() {
		return this.classFactory;
	}

}
