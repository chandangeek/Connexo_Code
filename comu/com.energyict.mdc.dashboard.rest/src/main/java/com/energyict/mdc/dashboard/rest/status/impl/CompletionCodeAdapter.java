package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

/**
 * Maps TaskStatus to related REST message seed
 * Created by bvn on 7/30/14.
 */
public class CompletionCodeAdapter extends MapBasedXmlAdapter<CompletionCode> {

    public CompletionCodeAdapter() {
        register(MessageSeeds.CONNECTION_ERROR.getKey(),CompletionCode.ConnectionError);
        register(MessageSeeds.CONFIGURATION_ERROR.getKey(),CompletionCode.ConfigurationError);
        register(MessageSeeds.CONFIGURATION_WARNING.getKey(),CompletionCode.ConfigurationWarning);
        register(MessageSeeds.IO_ERROR.getKey(),CompletionCode.IOError);
        register(MessageSeeds.PROTOCOL_ERROR.getKey(),CompletionCode.ProtocolError);
        register(MessageSeeds.OK.getKey(),CompletionCode.Ok);
        register(MessageSeeds.RESCHEDULED.getKey(),CompletionCode.Rescheduled);
        register(MessageSeeds.TIME_ERROR.getKey(),CompletionCode.TimeError);
        register(MessageSeeds.UNEXPECTED_ERROR.getKey(),CompletionCode.UnexpectedError);
    }
}
