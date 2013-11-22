package com.energyict.mdc.common;

import com.energyict.mdc.common.BusinessException;

public class MessageException extends BusinessException {

    public MessageException(String messageId, String defaultPattern) {
        super(messageId, defaultPattern);
    }

    public MessageException(String messageId, String defaultPattern, Throwable ex) {
        super(messageId, defaultPattern, null, ex);
    }
}
