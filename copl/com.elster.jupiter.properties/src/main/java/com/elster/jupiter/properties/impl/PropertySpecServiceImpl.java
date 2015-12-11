package com.elster.jupiter.properties.impl;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.LongFactory;
import com.elster.jupiter.properties.PropertySpecBuilderWizard;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.TimeZoneFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.TimeZone;

/**
 * Provides an implementation for the {@link PropertySpecService} interface
 * and registers as an OSGi component to be used by other dependent modules.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:59)
 */
@Component(name = "com.elster.jupiter.properties.propertyspecservice", service = PropertySpecService.class)
public class PropertySpecServiceImpl implements PropertySpecService {

    private volatile TimeService timeService;
    private volatile OrmService ormService;

    // For OSGi purposes
    public PropertySpecServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public PropertySpecServiceImpl(TimeService timeService, OrmService ormService) {
        this();
        this.setTimeService(timeService);
        this.setOrmService(ormService);
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }


    @Override
    public <T> PropertySpecBuilderWizard.NlsOptions<T> specForValuesOf(ValueFactory<T> valueFactory) {
        return new PropertySpecBuilderNlsOptions<>(valueFactory);
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> stringSpec() {
        return this.specForValuesOf(new StringFactory());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Boolean> booleanSpec() {
        return this.specForValuesOf(new BooleanFactory());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Long> longSpec() {
        return this.specForValuesOf(new LongFactory());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> bigDecimalSpec() {
        return this.specForValuesOf(new BigDecimalFactory());
    }

    @Override
    public <T> PropertySpecBuilderWizard.NlsOptions<TimeZone> timezoneSpec() {
        return this.specForValuesOf(new TimeZoneFactory());
    }

    @Override
    public <T> PropertySpecBuilderWizard.NlsOptions<RelativePeriod> relativePeriodSpec() {
        return this.specForValuesOf(new RelativePeriodFactory(this.timeService));
    }

    @Override
    public <T> PropertySpecBuilderWizard.NlsOptions<T> referenceSpec(Class<T> apiClass) {
        ReferenceValueFactory<T> valueFactory = new ReferenceValueFactory<T>(this.ormService).init(apiClass);
        return this.specForValuesOf(valueFactory);
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> boundedBigDecimalSpec(BigDecimal lowerLimit, BigDecimal upperLimit) {
        return new PartiallyInitializedPropertySpecBuilderNlsOptions<>(
                new BigDecimalFactory(),
                new BoundedBigDecimalPropertySpecImpl(lowerLimit, upperLimit));
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> positiveBigDecimalSpec() {
        return this.boundedBigDecimalSpec(BigDecimal.ZERO, null);
    }

}