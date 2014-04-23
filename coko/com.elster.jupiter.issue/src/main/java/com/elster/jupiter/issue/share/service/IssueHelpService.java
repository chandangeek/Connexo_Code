package com.elster.jupiter.issue.share.service;

//TODO delete this class when events will be sent by MDC
/**
 * This class can be used only in test purpose while MDC hasn't correct implementation
 */
@Deprecated
public interface IssueHelpService {
    void postEvent(String topic, String eventIdentifier);
}
