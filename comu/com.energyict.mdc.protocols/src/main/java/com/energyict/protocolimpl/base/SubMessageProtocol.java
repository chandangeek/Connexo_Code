package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.device.data.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.messaging.MessageSpec;

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
