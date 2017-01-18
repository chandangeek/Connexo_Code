package com.energyict.protocolimpl.dlms.prime.messaging;

import com.energyict.dlms.DlmsSession;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import java.io.IOException;

/**
 * Messaging for demand response.
 *
 * @author alex
 */
public final class DemandResponse extends PrimeMessageExecutor {

	/** The root tag of the message that allows setting the dctcp. */
	private static final String ROOT_TAG_DEMAND_CLOSE_TO_CONTRACT_POWER_THRESHOLD = "SetDemandCloseToContractPowerThreshold";

	/** Obis code for the register. */
	private static final ObisCode OBIS_DEMAND_CLOSE_TO_CONTRACT_POWER_THRESHOLD = ObisCode.fromString("0.0.94.34.70.255");

	/** The name of the attribute. */
	private static final String ATTRIBUTE_THRESHOLD_PERCENTAGE = "Threshold (%)";

	/**
	 * Gets the message category for the power quality messages.
	 *
	 * @return	The message category for the power quality messages.
	 */
	public static final MessageCategorySpec getCategorySpec() {
		final MessageCategorySpec spec = new MessageCategorySpec("Demand response");

		spec.addMessageSpec(addBasicMsgWithAttributes("Set demand close to contract power threshold", ROOT_TAG_DEMAND_CLOSE_TO_CONTRACT_POWER_THRESHOLD, true, ATTRIBUTE_THRESHOLD_PERCENTAGE));

		return spec;
	}

	/**
	 * Create a new instance.
	 *
	 * @param 		session		The DLMS session.
	 */
	public DemandResponse(final DlmsSession session) {
		super(session);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean canHandle(final MessageEntry messageEntry) {
		return (isMessageTag(ROOT_TAG_DEMAND_CLOSE_TO_CONTRACT_POWER_THRESHOLD, messageEntry));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final MessageResult execute(final MessageEntry messageEntry) throws IOException {
		if (isMessageTag(ROOT_TAG_DEMAND_CLOSE_TO_CONTRACT_POWER_THRESHOLD, messageEntry)) {
			return this.writeNumericParameterChange(OBIS_DEMAND_CLOSE_TO_CONTRACT_POWER_THRESHOLD, ATTRIBUTE_THRESHOLD_PERCENTAGE, messageEntry, 100, 16, true);
		} else {
			return MessageResult.createFailed(messageEntry);
		}
	}

}
