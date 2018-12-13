/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.gogo;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;

public class ListenToTopicHandler implements TopicHandler {

    private final String topic;

    public ListenToTopicHandler(String topic) {
            this.topic = topic;
        }

        @Override
        public void handle(LocalEvent localEvent) {
            System.out.println(topic + ": " + localEvent.getSource());
        }

        @Override
        public String getTopicMatcher() {
            return topic;
        }
}
