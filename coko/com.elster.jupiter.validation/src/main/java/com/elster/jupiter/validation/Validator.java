package com.elster.jupiter.validation;

import java.util.List;

public interface Validator {

    List<String> getrequiredKeys();

    List<String> getOptionalKeys();

    String getReadingQualityTypeCode();


}
