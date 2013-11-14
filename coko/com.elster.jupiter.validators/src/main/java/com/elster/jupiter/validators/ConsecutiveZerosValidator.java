package com.elster.jupiter.validators;

import com.elster.jupiter.validation.Validator;

import java.util.ArrayList;
import java.util.List;

public class ConsecutiveZerosValidator implements Validator {

    @Override
    public List<String> getrequiredKeys() {
        return new ArrayList();
    }

    @Override
    public List<String> getOptionalKeys() {
        return new ArrayList();
    }

    @Override
    public String getReadingQualityTypeCode() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
