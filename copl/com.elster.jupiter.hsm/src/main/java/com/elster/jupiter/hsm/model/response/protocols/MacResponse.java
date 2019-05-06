package com.elster.jupiter.hsm.model.response.protocols;

public interface MacResponse {
    byte[] getData();
    byte[] getInitVector();
}
