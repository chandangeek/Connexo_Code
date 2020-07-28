/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.Name;
import ch.iec.tc57._2011.schema.message.ObjectType;
import ch.iec.tc57._2011.schema.message.ReplyType;

import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.Arrays;

public class ReplyTypeFactory {
    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final Thesaurus thesaurus;

    @Inject
    public ReplyTypeFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ReplyType failureReplyType(String meterName, MessageSeeds messageSeed, Object... args) {
        return failureReplyType(meterName, messageSeed, ReplyType.Result.FAILED, args);
    }

    public ReplyType failureReplyType(MessageSeeds messageSeed, Object... args) {
        return failureReplyType(null, messageSeed, args);
    }

    public ReplyType partialFailureReplyType(MessageSeeds messageSeed, Object... args) {
        return failureReplyType(messageSeed, ReplyType.Result.PARTIAL, args);
    }

    public ReplyType failureReplyType(String message, String errorCode) {
        return failureReplyType(message, errorCode, ErrorType.Level.FATAL, ReplyType.Result.FAILED);
    }

    public ReplyType failureReplyType(String meterName, String message, String errorCode) {
        return failureReplyType(meterName, message, errorCode, ErrorType.Level.FATAL, ReplyType.Result.FAILED);
    }

    public ReplyType okReplyType() {
        return failureReplyType(ReplyType.Result.OK);
    }

    private ReplyType failureReplyType(MessageSeeds messageSeed, ReplyType.Result result, Object... args) {
        return failureReplyType(result, errorType(messageSeed, null, args));
    }

    private ReplyType failureReplyType(String meterName, MessageSeeds messageSeed, ReplyType.Result result, Object... args) {
        return failureReplyType(result, errorType(messageSeed, meterName, args));
    }


    private ReplyType failureReplyType(String meterName, String translatedMessage, String errorCode, ErrorType.Level level, ReplyType.Result result) {
        return failureReplyType(result, errorType(translatedMessage, errorCode, meterName, level));
    }

    private ReplyType failureReplyType(String translatedMessage, String errorCode, ErrorType.Level level, ReplyType.Result result) {
        return failureReplyType(result, errorType(translatedMessage, errorCode, null, level));
    }

    public ReplyType failureReplyType(ReplyType.Result result, ErrorType... errorType) {
        ReplyType replyType = cimMessageObjectFactory.createReplyType();
        replyType.setResult(result);
        replyType.getError().addAll(Arrays.asList(errorType));
        return replyType;
    }

    public ErrorType errorType(MessageSeeds messageSeed, String meterName, Object... args) {
        return errorType(messageSeed.translate(thesaurus, args),
                messageSeed.getErrorCode(),
                meterName,
                messageSeed.getErrorTypeLevel());
    }

    public ErrorType errorType(String deviceName, String deviceMrid, String translatedMessage, String errorCode, ErrorType.Level level) {
        ErrorType errorType = errorType(translatedMessage, errorCode, deviceName, level);
        if (deviceMrid != null) {
            ObjectType objectType = errorType.getObject();
            if (objectType == null) {
                objectType = new ObjectType();
                objectType.setObjectType("EndDevice");
                errorType.setObject(objectType);
            }
            objectType.setMRID(deviceMrid);
        }
        return errorType;
    }

    private ErrorType errorType(String translatedMessage, String errorCode, String meterName, ErrorType.Level level) {
        ErrorType errorType = cimMessageObjectFactory.createErrorType();
        errorType.setCode(errorCode);
        errorType.setDetails(translatedMessage);
        errorType.setLevel(level);
        if (meterName != null) {
            ObjectType objectType = new ObjectType();
            objectType.setObjectType("EndDevice");
            Name name = new Name();
            name.setName(meterName);
            objectType.getName().add(name);
            errorType.setObject(objectType);
        }
        return errorType;
    }
}