package com.energyict.protocolimpl.base;

import java.util.List;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.messaging.MessageSpec;

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
