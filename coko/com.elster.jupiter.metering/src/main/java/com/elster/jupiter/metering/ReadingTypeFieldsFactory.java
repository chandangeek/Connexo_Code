package com.elster.jupiter.metering;

import java.util.Map;

public interface ReadingTypeFieldsFactory {
    Map<Integer, String> getCodeFields(String field);
}
