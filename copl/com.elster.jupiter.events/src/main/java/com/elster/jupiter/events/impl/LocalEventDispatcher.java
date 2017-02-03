/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final class LocalEventDispatcher extends EventHandler<LocalEvent> {

    private final List<Pair<TopicMatcher, TopicHandler>> subscriptions = new CopyOnWriteArrayList<>();

    public LocalEventDispatcher() {
        super(LocalEvent.class);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        String topic = event.getType().getTopic();
        for (Pair<TopicMatcher, TopicHandler> subscription : subscriptions) {
            if (subscription.getFirst().matches(topic)) {
                subscription.getLast().handle(event);
            }
        }
    }

    void addSubscription(TopicHandler handler) {
        TopicMatcher topicMatcher = matcherForString(handler.getTopicMatcher());
        subscriptions.add(Pair.of(topicMatcher, handler));
    }

    private TopicMatcher matcherForString(String matcherString) {
        if (endsInWildcard(matcherString)) {
            return new StartMatcher(withoutLastChar(matcherString));
        }
        return new LiteralMatcher(matcherString);
    }

    private String withoutLastChar(String matcherString) {
        return matcherString.substring(0, matcherString.length() - 1);
    }

    private boolean endsInWildcard(String matcherString) {
        return matcherString.charAt(matcherString.length() - 1) == '*';
    }

    public void removeSubscription(TopicHandler topicHandler) {
        List<Pair<TopicMatcher, TopicHandler>> toRemove = collectAllToRemove(topicHandler);
        subscriptions.removeAll(toRemove);
    }

    private List<Pair<TopicMatcher, TopicHandler>> collectAllToRemove(TopicHandler topicHandler) {
        List<Pair<TopicMatcher, TopicHandler>> toRemove = new ArrayList<>();
        for (Pair<TopicMatcher, TopicHandler> pair : subscriptions) {
            if (pair.getLast().equals(topicHandler)) {
                toRemove.add(pair);
                break;
            }
        }
        return toRemove;
    }

    private interface TopicMatcher {

        boolean matches(String topic);

    }

    private static final class LiteralMatcher implements TopicMatcher {

        private final String literal;

        private LiteralMatcher(String literal) {
            this.literal = literal;
        }

        @Override
        public boolean matches(String topic) {
            return literal.equals(topic);
        }
    }

    private static final class StartMatcher implements TopicMatcher {

        private final String start;

        private StartMatcher(String start) {
            this.start = start;
        }

        @Override
        public boolean matches(String topic) {
            return topic.startsWith(start);
        }
    }

}
