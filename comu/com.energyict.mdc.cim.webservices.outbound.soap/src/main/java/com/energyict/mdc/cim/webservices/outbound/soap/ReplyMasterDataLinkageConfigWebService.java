/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.outbound.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import aQute.bnd.annotation.ConsumerType;

import java.math.BigDecimal;
import java.util.List;

@ConsumerType
public interface ReplyMasterDataLinkageConfigWebService {

	String NAME = "CIM ReplyMasterDataLinkageConfig";

	/**
	 * Invoked by the service call when the async inbound WS is completed
	 *
	 * @param endPointConfiguration - the outbound end point
	 * @param operation             - the operation that has been performed ("CREATE" or "CLOSE")
	 * @param successfulLinkages    - the list of successfully created linkages between usagePoints and meters
	 * @param failedLinkages        - the list of failed linkage creation attempts (between usagePoints and meters) with error message code and description
	 * @param expectedNumberOfCalls - the expected number of child calls
	 * @param correlationId - correlationId received in inbound request
	 */
	void call(EndPointConfiguration endPointConfiguration, String operation,
			List<LinkageOperation> successfulLinkages, List<FailedLinkageOperation> failedLinkages,
			BigDecimal expectedNumberOfCalls, String correlationId);
}