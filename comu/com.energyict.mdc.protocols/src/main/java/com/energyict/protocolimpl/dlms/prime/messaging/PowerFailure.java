/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime.messaging;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;

import com.energyict.dlms.DlmsSession;

import java.io.IOException;

/**
 * Messages concerning power failures.
 *
 * @author alex
 */
public final class PowerFailure extends PrimeMessageExecutor {

	/** The root tag used for a set long power failure time threshold. */
	private static final String ROOT_TAG_SET_LONG_POWER_FAILURE_TIME_THRESHOLD = "SetLongPowerFailureTimeThreshold";

	/** The root tag used for a set long power failure threshold. */
	private static final String ROOT_TAG_SET_LONG_POWER_FAILURE_THRESHOLD = "SetLongPowerFailureThreshold";

	/** The OBIS code that points to the register holding the long power failure time threshold. */
	private static final ObisCode OBIS_LONG_POWER_FAILURE_TIME_THRESHOLD = ObisCode.fromString("0.0.96.7.20.255");

	/** The OBIS code that points to the register holding the long power failure threshold. */
	private static final ObisCode OBIS_LONG_POWER_FAILURE_THRESHOLD = ObisCode.fromString("0.0.94.34.60.255");

	/** The attribute for a time threshold. */
	private static final String ATTRIBUTE_TIME_THRESHOLD = "Time threshold (seconds)";

    /** The name of the attribute. */
    private static final String ATTRIBUTE_THRESHOLD_PERCENTAGE = "Threshold (%)";

    /**
	 * Gets the message category for the power quality messages.
	 *
	 * @return	The message category for the power quality messages.
	 */
	public static final MessageCategorySpec getCategorySpec() {
		final MessageCategorySpec spec = new MessageCategorySpec("Power failure");

		spec.addMessageSpec(addBasicMsgWithAttributes("Long power failure time threshold", ROOT_TAG_SET_LONG_POWER_FAILURE_TIME_THRESHOLD, true, ATTRIBUTE_TIME_THRESHOLD));
		spec.addMessageSpec(addBasicMsgWithAttributes("Long power failure threshold", ROOT_TAG_SET_LONG_POWER_FAILURE_THRESHOLD, true, ATTRIBUTE_THRESHOLD_PERCENTAGE));

		return spec;
	}

	/**
	 * Create a new instance.
	 *
	 * @param 		session		The DLMS session.
	 */
	public PowerFailure(final DlmsSession session) {
		super(session);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean canHandle(final MessageEntry messageEntry) {
		boolean canHandle = false;
        canHandle |= isMessageTag(ROOT_TAG_SET_LONG_POWER_FAILURE_TIME_THRESHOLD, messageEntry);
        canHandle |= isMessageTag(ROOT_TAG_SET_LONG_POWER_FAILURE_THRESHOLD, messageEntry);
        return  canHandle;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final MessageResult execute(final MessageEntry messageEntry) throws IOException {
		if (isMessageTag(ROOT_TAG_SET_LONG_POWER_FAILURE_TIME_THRESHOLD, messageEntry)) {
			return this.writeNumericParameterChange(OBIS_LONG_POWER_FAILURE_TIME_THRESHOLD, ATTRIBUTE_TIME_THRESHOLD, messageEntry, 1, 16, true);
        }

        if (isMessageTag(ROOT_TAG_SET_LONG_POWER_FAILURE_THRESHOLD, messageEntry)) {
			return this.writeNumericParameterChange(OBIS_LONG_POWER_FAILURE_THRESHOLD, ATTRIBUTE_THRESHOLD_PERCENTAGE, messageEntry, 100, 16, true);
		}

		return MessageResult.createFailed(messageEntry);
	}

}
