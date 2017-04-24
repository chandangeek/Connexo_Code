package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;

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
