package com.elster.jupiter.hsm.model.response;

import com.elster.jupiter.hsm.model.Message;

public class DecryptResponse extends Message {

    public DecryptResponse(byte[] data) {
        super(data);
    }

}
