package com.energyict.protocolimpl.base;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;

import com.energyict.protocol.MessageProtocol;

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
