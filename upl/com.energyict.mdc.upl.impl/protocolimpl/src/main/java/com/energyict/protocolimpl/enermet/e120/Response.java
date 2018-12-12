package com.energyict.protocolimpl.enermet.e120;

public interface Response extends MessageBody {

	boolean isOk();

	NackCode getNackCode();

	Object getValue();

}
