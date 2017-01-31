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
 * Messages that apply to the power quality objects.
 *
 * @author alex
 */
public final class PowerQuality extends PrimeMessageExecutor {

	/** Root tag for the message for setting the reference voltage. */
	private static final String ROOT_TAG_SET_REFERENCE_VOLTAGE = "SetReferenceVoltage";

	/** The attribute for the reference voltage. */
	private static final String ATTRIBUTE_REFERENCE_VOLTAGE = "Reference voltage (V)";

	/** The attribute for a time threshold. */
	private static final String ATTRIBUTE_TIME_THRESHOLD = "Time threshold (seconds)";

	/** The attribute for a percentage threshold. */
	private static final String ATTRIBUTE_PERCENTAGE_THRESHOLD = "Threshold (%)";

	/** Root tag for setting the voltage sag threshold (%). */
	private static final String ROOT_TAG_THRESHOLD_FOR_VOLTAGE_SAG = "SetVoltageSagThreshold";

	/** Root tag for setting the voltage swell threshold (%). */
	private static final String ROOT_TAG_THRESHOLD_FOR_VOLTAGE_SWELL = "SetVoltageSwellThreshold";

	/** Root tag for setting the voltage sag time threshold (seconds). */
	private static final String ROOT_TAG_TIME_THRESHOLD_FOR_VOLTAGE_SAG = "SetVoltageSagTimeThreshold";

	/** Root tag for setting the voltage swell time threshold (seconds). */
	private static final String ROOT_TAG_TIME_THRESHOLD_FOR_VOLTAGE_SWELL = "SetVoltageSwellTimeThreshold";

	/** Obis code for the register containing the reference voltage. */
	private static final ObisCode OBIS_REFERENCE_VOLTAGE = ObisCode.fromString("1.0.0.6.4.255");

	/** Obis code for the register containing the voltage sag time threshold. */
	private static final ObisCode OBIS_VOLTAGE_SAG_TIME_THRESHOLD = ObisCode.fromString("1.0.12.43.0.255");

	/** Obis code for the register containing the voltage sag threshold. */
	private static final ObisCode OBIS_VOLTAGE_SAG_THRESHOLD = ObisCode.fromString("1.0.12.31.0.255");

	/** Obis code for the register containing the voltage swell time threshold. */
	private static final ObisCode OBIS_VOLTAGE_SWELL_TIME_THRESHOLD = ObisCode.fromString("1.0.12.44.0.255");

	/** Obis code for the register containing the voltage swell threshold. */
	private static final ObisCode OBIS_VOLTAGE_SWELL_THRESHOLD = ObisCode.fromString("1.0.12.35.0.255");

	/**
	 * Create a new instance.
	 *
	 * @param 	session			The current DLMS session.
	 */
	public PowerQuality(final DlmsSession session) {
		super(session);
	}

	/**
	 * Gets the message category for the power quality messages.
	 *
	 * @return	The message category for the power quality messages.
	 */
	public static final MessageCategorySpec getCategorySpec() {
		final MessageCategorySpec spec = new MessageCategorySpec("Power quality");

		spec.addMessageSpec(addBasicMsgWithAttributes("Set reference voltage", ROOT_TAG_SET_REFERENCE_VOLTAGE, true, ATTRIBUTE_REFERENCE_VOLTAGE));
		spec.addMessageSpec(addBasicMsgWithAttributes("Set voltage sag time threshold", ROOT_TAG_TIME_THRESHOLD_FOR_VOLTAGE_SAG, true, ATTRIBUTE_TIME_THRESHOLD));
		spec.addMessageSpec(addBasicMsgWithAttributes("Set voltage swell time threshold", ROOT_TAG_TIME_THRESHOLD_FOR_VOLTAGE_SWELL, true, ATTRIBUTE_TIME_THRESHOLD));
		spec.addMessageSpec(addBasicMsgWithAttributes("Set voltage sag threshold", ROOT_TAG_THRESHOLD_FOR_VOLTAGE_SAG, true, ATTRIBUTE_PERCENTAGE_THRESHOLD));
		spec.addMessageSpec(addBasicMsgWithAttributes("Set voltage swell threshold", ROOT_TAG_THRESHOLD_FOR_VOLTAGE_SWELL, true, ATTRIBUTE_PERCENTAGE_THRESHOLD));

		return spec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean canHandle(final MessageEntry messageEntry) {
		return (isMessageTag(ROOT_TAG_SET_REFERENCE_VOLTAGE, messageEntry)
				 || isMessageTag(ROOT_TAG_THRESHOLD_FOR_VOLTAGE_SAG, messageEntry)
				 || isMessageTag(ROOT_TAG_THRESHOLD_FOR_VOLTAGE_SWELL, messageEntry)
				 || isMessageTag(ROOT_TAG_TIME_THRESHOLD_FOR_VOLTAGE_SAG, messageEntry)
				 || isMessageTag(ROOT_TAG_TIME_THRESHOLD_FOR_VOLTAGE_SWELL, messageEntry));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final MessageResult execute(final MessageEntry messageEntry) throws IOException {
		if (isMessageTag(ROOT_TAG_SET_REFERENCE_VOLTAGE, messageEntry)) {
			return this.writeNumericParameterChange(OBIS_REFERENCE_VOLTAGE, ATTRIBUTE_REFERENCE_VOLTAGE, messageEntry, 1, 16, true);
		} else if (isMessageTag(ROOT_TAG_THRESHOLD_FOR_VOLTAGE_SAG, messageEntry)) {
			return this.writeNumericParameterChange(OBIS_VOLTAGE_SAG_THRESHOLD, ATTRIBUTE_PERCENTAGE_THRESHOLD, messageEntry, 100, 16, true);
		} else if (isMessageTag(ROOT_TAG_THRESHOLD_FOR_VOLTAGE_SWELL, messageEntry)) {
			return this.writeNumericParameterChange(OBIS_VOLTAGE_SWELL_THRESHOLD, ATTRIBUTE_PERCENTAGE_THRESHOLD, messageEntry, 100, 16, true);
		} else if (isMessageTag(ROOT_TAG_TIME_THRESHOLD_FOR_VOLTAGE_SAG, messageEntry)) {
			return this.writeNumericParameterChange(OBIS_VOLTAGE_SAG_TIME_THRESHOLD, ATTRIBUTE_TIME_THRESHOLD, messageEntry, 1, 16, true);
		} else if (isMessageTag(ROOT_TAG_TIME_THRESHOLD_FOR_VOLTAGE_SWELL, messageEntry)) {
			return this.writeNumericParameterChange(OBIS_VOLTAGE_SWELL_TIME_THRESHOLD, ATTRIBUTE_TIME_THRESHOLD, messageEntry, 1, 16, true);
		} else {
			this.getLogger().warning("Message [" + messageEntry.getContent() + "] could not be handled by this class.");

			return MessageResult.createFailed(messageEntry);
		}
	}


}
