package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

/**
 * Copyrights EnergyICT
 * Date: 7/13/12
 * Time: 10:59 AM
 */
public interface LoadProfileMessages {

    String RESET_LOGBOOKS_CATEGORY = "Reset load profile";

    @RtuMessageDescription(category = RESET_LOGBOOKS_CATEGORY, description = "Reset A+ load profile", tag = "ResetActiveImportLP")
    interface ResetActiveImportLPMessage extends AnnotatedMessage {

    }

    @RtuMessageDescription(category = RESET_LOGBOOKS_CATEGORY, description = "Reset A- load profile", tag = "ResetActiveExportLP")
    interface ResetActiveExportLPMessage extends AnnotatedMessage {

    }

    @RtuMessageDescription(category = RESET_LOGBOOKS_CATEGORY, description = "Reset daily EOB profile", tag = "ResetDailyProfile")
    interface ResetDailyProfileMessage extends AnnotatedMessage {

    }

    @RtuMessageDescription(category = RESET_LOGBOOKS_CATEGORY, description = "Reset monthly EOB profile", tag = "ResetMonthlyProfile")
    interface ResetMonthlyProfileMessage extends AnnotatedMessage {

    }
}