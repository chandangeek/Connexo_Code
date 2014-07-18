package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.configuration.rest.RegisterConfigInfo;
import com.energyict.mdc.device.data.Register;
import com.google.common.base.Optional;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EventRegisterInfo.class, name = "EVENT"),
        @JsonSubTypes.Type(value = NumericalRegisterInfo.class, name = "NUMERICAL")
})
public abstract class RegisterInfo<R extends Register> extends RegisterConfigInfo {
    @JsonProperty("lastReading")
    public Long lastReading;
    @JsonProperty("validationStatus")
    public Boolean validationStatus;


    public RegisterInfo() {}

    public RegisterInfo(R register) {
        super(register.getRegisterSpec());
        Optional<Date> lastReading = register.getLastReadingDate();
        if(lastReading.isPresent()) {
            this.lastReading = lastReading.get().getTime();
        }
        this.validationStatus = Boolean.TRUE;
        // TODO Uncomment when it was done in device.data bundle
        // this.validationStatus = register.isValidated();
    }
}
