/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events;

public interface TopicHandler {

    void handle(LocalEvent localEvent);

    String getTopicMatcher();

}
