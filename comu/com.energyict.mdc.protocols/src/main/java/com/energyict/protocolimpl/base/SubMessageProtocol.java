/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;

import java.util.List;

/**
 * @author jme
 *
 */
public interface SubMessageProtocol extends MessageProtocol {

	List<String> getSupportedMessageTags();

	boolean canHandleMessage(MessageEntry messageEntry);

	boolean canHandleMessage(MessageSpec messageSpec);

	boolean canHandleMessage(String messageTag);

}
