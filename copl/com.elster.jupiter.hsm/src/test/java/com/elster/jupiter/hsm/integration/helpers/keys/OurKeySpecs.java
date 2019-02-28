package com.elster.jupiter.hsm.integration.helpers.keys;

import com.elster.jupiter.hsm.model.Message;

public class OurKeySpecs {

    public static final Message wrapperKey = new Message("PasswordPasswordPasswordPassword");
    public static final Message initVector = new Message("0123456789ABCDEF");
    public static final Message deviceKey = new Message("PasswordPassword");
}
