/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import java.util.List;

/**
 * Checks if enforcing a VirtualReadingType onto all expressions will work
 * and returns <code>true</code> if that is the case.
 * As an example, the VirtualReadingType 15min cannot be forced into a
 * {@link VirtualReadingTypeRequirement} if none of the backing channels
 * are capable of providing that interval.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-03 (11:00)
 */
interface CheckEnforceReadingType extends ServerExpressionNode.Visitor<Boolean> {

    Boolean forAll(List<ServerExpressionNode> expressions);

    VirtualReadingType getReadingType();

}