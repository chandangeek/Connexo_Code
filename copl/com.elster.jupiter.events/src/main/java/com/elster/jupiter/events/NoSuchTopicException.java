package com.elster.jupiter.events;

import com.elster.jupiter.util.exception.BaseException;

public class NoSuchTopicException extends BaseException
{

    public NoSuchTopicException(String topic) {
        super(ExceptionTypes.NO_SUCH_TOPIC, "Topic {0} does not exist.");
        set("topic", topic);
    }
}
