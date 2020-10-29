package com.energyict.protocolimplv2.eict.webcatch;

import com.energyict.protocolimplv2.eict.webcatch.model.ModelApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;

public class ResponseWriter {

    private final HttpServletResponse response;

    public ResponseWriter(HttpServletResponse response) {
        this.response = response;
    }

    public void success() {
        writeResult(Status.CREATED, Optional.empty());
    }

    public void clientSideValidationException(String message) {
        writeResult(Status.BAD_REQUEST, Optional.of(message));
    }

    public void serverSideValidationException(String message) {
        writeResult(Status.NOT_ACCEPTABLE, Optional.of(message));
    }

    public void failure() {
        writeResult(Status.INTERNAL_SERVER_ERROR, Optional.empty());
    }

    private void writeResult(Status status, Optional<String> message) {
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(status.statusCode);
            new ObjectMapper().writeValue(response.getOutputStream(), buildModelApiResponseForStatus(status, message));
        }
        catch(IOException ignored) {
        }
    }

    private ModelApiResponse buildModelApiResponseForStatus(Status status, Optional<String> message) {
        ModelApiResponse modelApiResponse = new ModelApiResponse();
        modelApiResponse.setCode(status.statusCode);
        modelApiResponse.setType(status.reasonPhrase);
        modelApiResponse.setMessage(message.orElse(status.defaultMessage));
        return modelApiResponse;
    }

    enum Status {
        CREATED(SC_CREATED, "Created", "JSON object is persisted successfully!!!"),
        BAD_REQUEST(SC_BAD_REQUEST, "Bad Request", "Bad Request"),
        NOT_ACCEPTABLE(SC_NOT_ACCEPTABLE, "Not Acceptable", "Not Acceptable"),
        INTERNAL_SERVER_ERROR(SC_INTERNAL_SERVER_ERROR, "Internal Server Error","Internal Server error:No Explicit reason");

        private final int statusCode;
        private final String reasonPhrase;
        private final String defaultMessage;

        Status(int statusCode, String reasonPhrase, String defaultMessage) {
            this.statusCode = statusCode;
            this.reasonPhrase = reasonPhrase;
            this.defaultMessage = defaultMessage;
        }
    }

}