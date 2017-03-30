/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.enermet.e120;

public interface Response extends MessageBody {

	boolean isOk();

	NackCode getNackCode();

	Object getValue();

}
