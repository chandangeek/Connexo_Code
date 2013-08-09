package com.elster.jupiter.events;

public interface TopicHandler {

    void handle(LocalEvent localEvent);

    String getTopicMatcher();

}
