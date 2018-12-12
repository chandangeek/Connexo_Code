/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class NoSuchTopicException extends LocalizedException
{

	private static final long serialVersionUID = 1L;

	public NoSuchTopicException(Thesaurus thesaurus, String topic) {
        super(thesaurus, MessageSeeds.NO_SUCH_TOPIC, topic);
        set("topic", topic);
    }
}
