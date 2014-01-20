package com.elster.jupiter.events;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;

public class NoSuchTopicException extends BaseException
{

    public NoSuchTopicException(Thesaurus thesaurus, String topic) {
        super(ExceptionTypes.NO_SUCH_TOPIC, buildMessage(thesaurus, topic));
        set("topic", topic);
    }

    private static String buildMessage(Thesaurus thesaurus, String topic) {
        return thesaurus.getFormat(MessageSeeds.NO_SUCH_TOPIC).format(topic);
    }
}
