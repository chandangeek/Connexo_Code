package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;

import java.io.IOException;

/**
 * @author gna
 *
 */

public class GenericInvoke extends AbstractCosemObject {

	private int method;
	
	public GenericInvoke(ProtocolLink protocolLink,ObjectReference objectReference, int method) {
		super(protocolLink, objectReference);
		this.method = method;
	}

	protected int getClassId() {
		return getObjectReference().getClassId();
	}
	
	public void invoke(byte[] data) throws IOException {
		invoke(method, data);
	}

    public int getMethod() {
        return method;
    }

    public void invoke() throws IOException {
		invoke(method);
	}

}
