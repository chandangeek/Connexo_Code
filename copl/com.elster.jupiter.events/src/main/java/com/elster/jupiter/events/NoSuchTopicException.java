package com.elster.jupiter.events;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class NoSuchTopicException extends LocalizedException
{

    public NoSuchTopicException(Thesaurus thesaurus, String topic) {
        super(thesaurus, MessageSeeds.NO_SUCH_TOPIC, topic);
        set("topic", topic);
    }
}
