/*
 * HtmlEnabledBusinessException.java
 *
 * Created on 5 oktober 2005, 9:02
 */

package com.energyict.mdc.engine.offline.core.exception;

/**
 * BusinessException of which the message can be get as a HTML marked up String
 *
 * @author pasquien
 */
public abstract class HtmlEnabledBusinessException extends BusinessException {

    public HtmlEnabledBusinessException() {
        super();
    }

    public HtmlEnabledBusinessException(String messageId, String defaultPattern) {
        super(messageId, defaultPattern);
    }

    /**
     * Get the exception's message as a HTML marked up String
     *
     * @return The exception's message marked up as an HTML String
     */
    public abstract String getHtmlMessage();
}
