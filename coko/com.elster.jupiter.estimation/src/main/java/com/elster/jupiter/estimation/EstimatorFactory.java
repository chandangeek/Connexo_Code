/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import java.util.List;
import java.util.Map;

public interface EstimatorFactory {

    List<String> available();

    Estimator create(String implementation, Map<String, Object> props);

    Estimator createTemplate(String implementation);

}
