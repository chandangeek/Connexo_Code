/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.*;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;

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
    private volatile BeanService beanService;

    // For OSGi purposes
    public PropertySpecServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public PropertySpecServiceImpl(TimeService timeService, OrmService ormService, BeanService beanService) {
        this();
        this.setTimeService(timeService);
        this.setOrmService(ormService);
        this.setBeanService(beanService);
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setBeanService(BeanService beanService) {
        this.beanService = beanService;
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
    public PropertySpecBuilderWizard.NlsOptions<String> base64StringSpec() {
        return this.specForValuesOf(new Base64StringFactory());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> textareaStringSpec() {
        return this.specForValuesOf(new TextareaStringFactory());
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
    public PropertySpecBuilderWizard.NlsOptions<TimeZone> timezoneSpec() {
        return new PartiallyInitializedPropertySpecBuilderNlsOptions<>(
                new TimeZoneFactory(),
                new TimeZonePropertySpecImpl());
    }

    public PropertySpecBuilderWizard.NlsOptions<TimeDuration> timeDurationSpec() {
        return this.specForValuesOf(new TimeDurationValueFactory());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<TimeDuration> temporalAmountSpec() {
        return this.specForValuesOf(new TemporalAmountValueFactory());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<TimeDuration> durationSpec() {
        return this.specForValuesOf(new DurationValueFactory());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<RelativePeriod> relativePeriodSpec() {
        return this.specForValuesOf(new RelativePeriodFactory(this.timeService));
    }

    @Override
    public <T> PropertySpecBuilderWizard.NlsOptions<T> referenceSpec(Class<T> apiClass) {
        return this.specForValuesOf(referenceValueFactory(apiClass));
    }

    @Override
    public <T> ValueFactory<T> referenceValueFactory(Class<T> apiClass) {
        return new ReferenceValueFactory<T>(this.ormService, beanService).init(apiClass);
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
