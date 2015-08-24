package com.elster.jupiter.estimation;

import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public interface Estimator extends HasDynamicProperties {

    default void init(Logger logger) {
        // empty by default
    }

    EstimationResult estimate(List<EstimationBlock> estimationBlock);

    String getDisplayName();

    String getDisplayName(String property);

    String getDefaultFormat();

    default void validateProperties(Map<String, Object> properties) {

    }

    NlsKey getNlsKey();

    NlsKey getPropertyNlsKey(String property);

    String getPropertyDefaultFormat(String property);

    List<Pair<? extends NlsKey, String>> getExtraTranslations();

    List<String> getRequiredProperties();
}
