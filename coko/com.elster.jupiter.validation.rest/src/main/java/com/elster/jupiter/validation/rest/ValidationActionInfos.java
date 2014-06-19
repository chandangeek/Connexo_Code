package com.elster.jupiter.validation.rest;

import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationActionInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ValidationActionInfos {

    public int total;
    public List<ValidationActionInfo> actions = new ArrayList<ValidationActionInfo>();

    ValidationActionInfos() {
    }

    ValidationActionInfos(ValidationAction validationAction) {
        add(validationAction);
    }

    ValidationActionInfos(Iterable<? extends ValidationAction> actions) {
        addAll(actions);
    }

    ValidationActionInfo add(ValidationAction validationAction) {
        ValidationActionInfo result = new ValidationActionInfo(validationAction);
        actions.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends ValidationAction> actions) {
        for (ValidationAction each : actions) {
            add(each);
        }
    }
}
