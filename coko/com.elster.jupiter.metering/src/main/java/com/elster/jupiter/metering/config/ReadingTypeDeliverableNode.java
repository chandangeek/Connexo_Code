/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import aQute.bnd.annotation.ProviderType;

/**
 * Created by igh on 17/03/2016.
 */
@ProviderType
public interface ReadingTypeDeliverableNode extends ExpressionNode {
    ReadingTypeDeliverable getReadingTypeDeliverable();
}