package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Reading;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BillingReadingInfo.class, name = "billing"),
        @JsonSubTypes.Type(value = NumericalReadingInfo.class, name = "numerical"),
        @JsonSubTypes.Type(value = TextReadingInfo.class, name = "text"),
        @JsonSubTypes.Type(value = FlagsReadingInfo.class, name = "flags")
})
public abstract class ReadingInfo<R extends Reading, S extends RegisterSpec> {
    @JsonProperty("timeStamp")
    public Long timeStamp;
    @JsonProperty("reportedDateTime")
    public Long reportedDateTime;

    public ReadingInfo() {
    }

    public ReadingInfo(R reading, S registerSpec, ValidationEvaluator evaluator) {
        this.timeStamp = reading.getTimeStamp().getTime();
        this.reportedDateTime = reading.getReportedDateTime().getTime();
    }

}
