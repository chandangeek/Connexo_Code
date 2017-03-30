/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json;

import com.google.common.collect.ImmutableList;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * ActionBasedJsonParser crawls all paths in the Json String and passes each path that has a value to the configured ActionForPaths, who may record, the value at that path, or perform some other action.
 * ActionBasedJsonParser is actually a convenience class over an event based parsing approach.
 */
public final class ActionBasedJsonParser {

    private static final String OBJECT_MARKER = "{";
    private static final String ARRAY_MARKER = "[";
    private final List<ActionForPath> actionForPaths;
    private final JsonFactory jsonFactory = new JsonFactory();

    /**
     * @param actionForPaths the ActionForPath instances.
     */
    public ActionBasedJsonParser(List<ActionForPath> actionForPaths) {
        this.actionForPaths = ImmutableList.copyOf(actionForPaths);
    }

    /**
     * @param actionForPaths the ActionForPath instances.
     */
    public ActionBasedJsonParser(ActionForPath... actionForPaths) {
        this.actionForPaths = ImmutableList.copyOf(actionForPaths);
    }

    public void parse(Reader reader) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(reader)) {
            doParse(parser);
        }
    }

    private void doParse(JsonParser parser) throws IOException {
        List<String> tokens = new ArrayList<>();
        for (JsonToken currentToken = parser.nextToken(); currentToken != null; currentToken = parser.nextToken()) {
            handleToken(parser, tokens, currentToken);
        }
    }

    private void handleToken(JsonParser parser, List<String> tokens, JsonToken currentToken) throws IOException {
        switch (currentToken) {
            case START_OBJECT:
                tokens.add(OBJECT_MARKER);
                break;
            case END_OBJECT:
                String objectMarker = OBJECT_MARKER;
                stripUntil(tokens, objectMarker);
                break;
            case START_ARRAY:
                tokens.add(ARRAY_MARKER);
                break;
            case END_ARRAY:
                stripUntil(tokens, ARRAY_MARKER);
                break;
            case FIELD_NAME:
                if (isFieldName(previous(tokens))) {
                    tokens.remove(tokens.size() - 1);
                }
                tokens.add(parser.getText());
                break;
            case VALUE_EMBEDDED_OBJECT:
            case VALUE_FALSE:
            case VALUE_TRUE:
            case VALUE_NULL:
            case VALUE_NUMBER_FLOAT:
            case VALUE_NUMBER_INT:
            case VALUE_STRING:
                List<String> path = ImmutableList.copyOf(tokens);
                for (ActionForPath actionForPath : actionForPaths) {
                    actionForPath.action(path, parser.getText());
                }
            default:

        }
    }

    private String previous(List<String> tokens) {
        return tokens.get(tokens.size() - 1);
    }

    private boolean isFieldName(String previous) {
        return !ARRAY_MARKER.equals(previous) && !OBJECT_MARKER.equals(previous);
    }

    private void stripUntil(List<String> tokens, String objectMarker) {
        String remove;
        do {
            remove = tokens.remove(tokens.size() - 1);
        }
        while (!objectMarker.equals(remove));
    }
}
