/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime.messaging;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.MessageAttributeSpec;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Integer16;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer64;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.Register;
import com.energyict.protocolimpl.dlms.prime.PrimeProperties;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PrimeMessageExecutor {

    private final DlmsSession session;
    private PrimeProperties properties;

    protected PrimeMessageExecutor(final DlmsSession session) {
        this.session = session;
    }

    protected PrimeMessageExecutor(final DlmsSession session, PrimeProperties properties) {
        this.session = session;
        this.properties = properties;
    }

    protected PrimeProperties getProperties() {
        return properties;
    }

    protected DlmsSession getSession() {
        return session;
    }

    protected Logger getLogger() {
        return getSession().getLogger();
    }

    public abstract boolean canHandle(final MessageEntry messageEntry);

    public abstract MessageResult execute(final MessageEntry messageEntry) throws IOException;

    protected static boolean isMessageTag(final String tag, final MessageEntry messageEntry) {
        return (messageEntry.getContent().contains("<" + tag));
    }

    protected static MessageSpec addBasicMsg(String displayName, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(displayName, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected static MessageSpec addBasicMsgWithAttributes(final String keyId, final String tagName, final boolean advanced, String... attr) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, true));
        }
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Gets the value of a particular attribute from the message.
     *
     * @param 	attributeName		The name of the attribute.
     * @param 	messageContent		The message content.
     *
     * @return	The value of the attribute, <code>null</code> if no such attribute could be found.
     */
    protected static String getAttributeValue(final String attributeName, final String messageContent) {
    	final StringBuilder patternBuilder = new StringBuilder(attributeName.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)"));
    	patternBuilder.append("=\"(.*?)\"");

    	String patternString = MessageFormat.format(patternBuilder.toString(), new Object[]{attributeName});

    	final Pattern pattern = Pattern.compile(patternString);
    	final Matcher matcher = pattern.matcher(messageContent);

    	if (matcher.find()) {
    		return matcher.group(1);
    	}

    	return null;
    }

	/**
	 * Reads the given attribute from the message, and writes the integer value to the specified register. This assumes the register
	 * holds long-unsigned values (Unsigned16).
	 *
	 * @param 	obisCode			The OBIS code of the register to write.
	 * @param 	attributeName		The name of the attribute to fetch out of the message.
	 * @param 	entry				The message itself.
	 * @param	dataTypeWidth       The data type.
	 * @param	parameterMultiplier	The multiplier for the parameter (should the meter use another scale).
	 *
	 * @return	The {@link MessageResult}.
	 *
	 * @throws java.io.IOException            If an IO error occurs while applying the change.
	 */
	protected final MessageResult writeNumericParameterChange(final ObisCode obisCode, final String attributeName, final MessageEntry entry, final int parameterMultiplier, final int dataTypeWidth, final boolean unsigned) throws IOException {
		final String attributeValue = getAttributeValue(attributeName, entry.getContent());

		if (this.getLogger().isLoggable(Level.INFO)) {
			this.getLogger().info("Trying to set attribute [" + attributeName + "], OBIS code [" + obisCode + "] to value [" + attributeValue + "], multiplier [" + parameterMultiplier + "]");
		}

		if (attributeValue != null) {
			try {
				final int numericValue = Integer.parseInt(attributeValue) * parameterMultiplier;

				final Register register = this.getSession().getCosemObjectFactory().getRegister(obisCode);
				AbstractDataType dataToWrite = null;

				switch (dataTypeWidth) {
					case 8: {
						dataToWrite = (unsigned? new Unsigned8(numericValue) : new Integer8(numericValue));
						break;
					}

					case 16: {
						dataToWrite = (unsigned ? new Unsigned16(numericValue) : new Integer16(numericValue));
						break;
					}

					case 32: {
						dataToWrite = (unsigned ? new Unsigned32(numericValue) : new Integer32(numericValue));
						break;
					}

					case 64: {
						dataToWrite = (unsigned ? new Integer64(numericValue) : new Integer64(numericValue));
						break;
					}

					default: {
						throw new IllegalArgumentException("Illegal data width : [" + dataTypeWidth + "]");
					}
				}

				register.setValueAttr(dataToWrite);

				if (this.getLogger().isLoggable(Level.INFO)) {
					this.getLogger().info("Parameter change applied successfully.");
				}

				return MessageResult.createSuccess(entry);
			} catch (NumberFormatException e) {
				this.getLogger().warning("Could not parse attribute [" + attributeName + "] in message [" + entry.getContent() + "] to a valid integer !");

				return MessageResult.createFailed(entry);
			}
		} else {
			this.getLogger().warning("Message [" + entry.getContent() + "] did not contain attribute [" + attributeName + "]");

			return MessageResult.createFailed(entry);
		}
	}
}