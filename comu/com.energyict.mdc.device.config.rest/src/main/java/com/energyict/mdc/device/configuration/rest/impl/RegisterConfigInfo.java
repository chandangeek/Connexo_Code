package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.RegisterSpec;
import org.codehaus.jackson.annotate.JsonProperty;

public class RegisterConfigInfo {

    @JsonProperty("id")
    public long id;

    public RegisterConfigInfo(RegisterSpec registerSpec) {
        this.id = registerSpec.getId();
    }
}
