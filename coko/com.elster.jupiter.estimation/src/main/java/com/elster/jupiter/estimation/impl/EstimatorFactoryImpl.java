package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component(name = "com.elster.jupiter.estimation.factory", service = {EstimatorFactory.class}, immediate = true)
public class EstimatorFactoryImpl implements EstimatorFactory {

    public static final String ZERO_FILL = "Zero fill";
    public static final String CUMULATIVE_VALUES_INTERPOLATION = "Cumulative values interpolation";
    private volatile EstimationService estimationService;
    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;

    @Override
    public List<String> available() {
        return Arrays.asList(ZERO_FILL, CUMULATIVE_VALUES_INTERPOLATION);
    }

    @Override
    public Estimator create(String implementation, Map<String, Object> props) {
        if (implementation.equals(ZERO_FILL)) {
            return new ZeroFillEstimator(thesaurus, propertySpecService, props);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Estimator createTemplate(String implementation) {
        if (implementation.equals(ZERO_FILL)) {
            return new ZeroFillEstimator(thesaurus, propertySpecService);
        } else if (implementation.equals(CUMULATIVE_VALUES_INTERPOLATION)) {
            return new CumulativeValuesInterpolator(thesaurus, propertySpecService);
        }
        throw new IllegalArgumentException();
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
        this.thesaurus = ((IEstimationService) estimationService).getThesaurus();
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }
}
