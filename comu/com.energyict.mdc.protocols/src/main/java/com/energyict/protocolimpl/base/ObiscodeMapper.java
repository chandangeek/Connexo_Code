/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.base;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import java.io.IOException;

/**
 * @author jme
 *
 */
public interface ObiscodeMapper {

	RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException;

	RegisterValue getRegisterValue(ObisCode obisCode) throws IOException;

}
