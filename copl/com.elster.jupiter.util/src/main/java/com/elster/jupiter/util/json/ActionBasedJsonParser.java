package com.elster.jupiter.util.json;

import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public final class ActionBasedJsonParser {

    private static final String OBJECT_MARKER = "{";
    private static final String ARRAY_MARKER = "[";
    private final List<ActionForPath> actionForPaths;
    private final JsonFactory jsonFactory = new JsonFactory();

    public ActionBasedJsonParser(List<ActionForPath> actionForPaths) {
        this.actionForPaths = ImmutableList.copyOf(actionForPaths);
    }

    public ActionBasedJsonParser(ActionForPath... actionForPaths) {
        this.actionForPaths = ImmutableList.copyOf(actionForPaths);
    }

    public void parse(Reader reader) throws IOException {
        try (JsonParser parser = jsonFactory.createJsonParser(reader)) {
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
            case END_ARRAY :
                stripUntil(tokens, ARRAY_MARKER);
                break;
            case FIELD_NAME :
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
                for (ActionForPath actionForPath : actionForPaths) {
                    actionForPath.action(tokens, parser.getText());
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
        while (!objectMarker.equals(tokens.remove(tokens.size() - 1)));
    }
}
