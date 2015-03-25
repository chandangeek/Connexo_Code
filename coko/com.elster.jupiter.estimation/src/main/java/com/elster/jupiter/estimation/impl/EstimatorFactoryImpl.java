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

    public static final String VALUE_FILL = "Value fill";
    public static final String LINEAR_INTERPOLATION = "Linear interpolation";
    private volatile EstimationService estimationService;
    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;

    @Override
    public List<String> available() {
        return Arrays.asList(VALUE_FILL, LINEAR_INTERPOLATION);
    }

    @Override
    public Estimator create(String implementation, Map<String, Object> props) {
        if (implementation.equals(VALUE_FILL)) {
            return new ValueFillEstimator(thesaurus, propertySpecService, props);
        } else if (implementation.equals(LINEAR_INTERPOLATION)) {
            return new LinearInterpolation(thesaurus, propertySpecService, props);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Estimator createTemplate(String implementation) {
        if (implementation.equals(VALUE_FILL)) {
            return new ValueFillEstimator(thesaurus, propertySpecService);
        } else if (implementation.equals(LINEAR_INTERPOLATION)) {
            return new LinearInterpolation(thesaurus, propertySpecService);
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
