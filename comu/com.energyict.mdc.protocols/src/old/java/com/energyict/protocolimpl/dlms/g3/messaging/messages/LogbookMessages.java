/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

public interface LogbookMessages {

    String RESET_LOGBOOKS_CATEGORY = "Reset logbook";

    @RtuMessageDescription(category = RESET_LOGBOOKS_CATEGORY, description = "Reset main logbook", tag = "ResetMainLogbook")
    interface ResetMainLogbookMessage extends AnnotatedMessage {

    }

    @RtuMessageDescription(category = RESET_LOGBOOKS_CATEGORY, description = "Reset cover logbook", tag = "ResetCoverLogbook")
    interface ResetCoverLogbookMessage extends AnnotatedMessage {

    }

    @RtuMessageDescription(category = RESET_LOGBOOKS_CATEGORY, description = "Reset breaker logbook", tag = "ResetBreakerLogbook")
    interface ResetBreakerLogbookMessage extends AnnotatedMessage {

    }

    @RtuMessageDescription(category = RESET_LOGBOOKS_CATEGORY, description = "Reset communication logbook", tag = "ResetCommunicationLogbook")
    interface ResetCommunicationLogbookMessage extends AnnotatedMessage {

    }

    @RtuMessageDescription(category = RESET_LOGBOOKS_CATEGORY, description = "Reset voltage cut logbook", tag = "ResetVoltageCutLogbook")
    interface ResetVoltageCutLogbookMessage extends AnnotatedMessage {

    }

    @RtuMessageDescription(category = RESET_LOGBOOKS_CATEGORY, description = "Reset LQI logbook", tag = "ResetLQILogbook")
    interface ResetLqiLogbookMessage extends AnnotatedMessage {

    }

}
