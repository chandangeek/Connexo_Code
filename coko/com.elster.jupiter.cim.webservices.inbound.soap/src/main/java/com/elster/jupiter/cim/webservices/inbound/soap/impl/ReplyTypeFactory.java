/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.nls.Thesaurus;

import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.ReplyType;

import javax.inject.Inject;
import java.util.Arrays;

public class ReplyTypeFactory {
    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final Thesaurus thesaurus;

    @Inject
    public ReplyTypeFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ReplyType failureReplyType(MessageSeeds messageSeed, Object... args) {
        return failureReplyType(messageSeed, ReplyType.Result.FAILED, args);
    }

    public ReplyType partialFailureReplyType(MessageSeeds messageSeed, Object... args) {
        return failureReplyType(messageSeed, ReplyType.Result.PARTIAL, args);
    }

    public ReplyType failureReplyType(String message, String errorCode) {
        return failureReplyType(message, errorCode, ErrorType.Level.FATAL, ReplyType.Result.FAILED);
    }

    public ReplyType okReplyType() {
        return failureReplyType(ReplyType.Result.OK);
    }

    private ReplyType failureReplyType(MessageSeeds messageSeed, ReplyType.Result result, Object... args) {
        return failureReplyType(result, errorType(messageSeed, args));
    }

    private ReplyType failureReplyType(String translatedMessage, String errorCode, ErrorType.Level level, ReplyType.Result result) {
        return failureReplyType(result, errorType(translatedMessage, errorCode, level));
    }

    public ReplyType failureReplyType(ReplyType.Result result, ErrorType... errorType) {
        ReplyType replyType = cimMessageObjectFactory.createReplyType();
        replyType.setResult(result);
        replyType.getError().addAll(Arrays.asList(errorType));
        return replyType;
    }

    public ErrorType errorType(MessageSeeds messageSeed, Object... args) {
        return errorType(messageSeed.translate(thesaurus, args),
                messageSeed.getErrorCode(),
                messageSeed.getErrorTypeLevel());
    }

    private ErrorType errorType(String translatedMessage, String errorCode, ErrorType.Level level) {
        ErrorType errorType = cimMessageObjectFactory.createErrorType();
        errorType.setCode(errorCode);
        errorType.setDetails(translatedMessage);
        errorType.setLevel(level);
        return errorType;
    }
}
