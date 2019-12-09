package com.energyict.mdc.engine.offline.core.exception;

public class MultipleErrorMessagesException extends HtmlEnabledBusinessException {

    public MultipleErrorMessagesException() {
        super("multipleErrorsOccurred", "Multiple errors occurred");
    }

    public MultipleErrorMessagesException(String msgId, String defaultPattern) {
        super(msgId, defaultPattern);
    }

    public String getHtmlMessage() {
        StringBuilder stringBuilder = new StringBuilder(super.getLocalizedMessage()).append(":<br>");
        if (getSuppressed().length>0) {
            stringBuilder.append("<ul>");
        }
        for (Throwable each : getSuppressed()) {
            stringBuilder.append("<li><b>");
            if (each instanceof CompositeCommandBusinessException) {
                stringBuilder.append(((CompositeCommandBusinessException)each).getCompositeCommandName()+" - ");
            }
            if (each instanceof BusinessException) {
                stringBuilder.append(((BusinessException)each).getErrorCode()+": ");
            } else {
                stringBuilder.append( each.getClass().getSimpleName() + ": ");
            }
            stringBuilder.append("</b>").append(each.getLocalizedMessage())
            .append("</li>");
        }
        if (getSuppressed().length>0) {
            stringBuilder.append("</ul>");
        }
        return stringBuilder.toString();
    }
}


