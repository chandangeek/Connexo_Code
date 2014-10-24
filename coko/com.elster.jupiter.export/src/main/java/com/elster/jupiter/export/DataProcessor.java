package com.elster.jupiter.export;

import com.elster.jupiter.properties.PropertySpec;

import java.util.List;

public interface DataProcessor {

    List<PropertySpec<?>> getPropertySpecs();
}
