/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecBuilderWizard;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.ean.Ean13;
import com.energyict.mdc.common.ean.Ean18;
import com.energyict.mdc.dynamic.Ean13Factory;
import com.energyict.mdc.dynamic.Ean18Factory;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

/**
 * Provides an implementation for the {@link PropertySpecService} interface
 * and registers as an OSGi component to be used by other dependent modules.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:59)
 */
@Component(name = "com.energyict.mdc.dynamic.propertyspecservice", service = {PropertySpecService.class, MessageSeedProvider.class})
public class PropertySpecServiceImpl implements PropertySpecService, MessageSeedProvider {

    public static final String COMPONENT = "PSP";

    private volatile DataModel dataModel;
    private volatile DataVaultService dataVaultService;
    private volatile com.elster.jupiter.properties.PropertySpecService basicPropertySpecService;

    // For OSGi purposes
    public PropertySpecServiceImpl() {
    }

    // For testing purposes
    @Inject
    public PropertySpecServiceImpl(com.elster.jupiter.properties.PropertySpecService basicPropertySpec, DataVaultService dataVaultService, OrmService ormService) {
        this();
        this.setBasicPropertySpecService(basicPropertySpec);
        this.setOrmService(ormService);
        this.setDataVaultService(dataVaultService);
        this.activate();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("DYN", "MDC Dynamic Services");
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }


    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(DataVaultService.class).toInstance(dataVaultService);
            }
        };
    }

    @Reference
    public void setBasicPropertySpecService(com.elster.jupiter.properties.PropertySpecService propertySpecService) {
        this.basicPropertySpecService = propertySpecService;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<ObisCode> obisCodeSpec() {
        return this.specForValuesOf(new ObisCodeValueFactory());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Password> passwordSpec() {
        return this.specForValuesOf(new PasswordFactory(this.dataVaultService));
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> encryptedStringSpec() {
        return this.specForValuesOf(new EncryptedStringFactory(this.dataVaultService));
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<HexString> encryptedHexStringSpec() {
        return this.specForValuesOf(new EncryptedHexStringFactory(this.dataVaultService).addValidator(new HexStringLengthValidator().withLength(32) ));
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<TimeDuration> timeDurationSpec() {
        return this.specForValuesOf(new TimeDurationValueFactory());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<HexString> hexStringSpec() {
        return this.specForValuesOf(new HexStringFactory());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Ean13> ean13Spec() {
        return this.specForValuesOf(new Ean13Factory());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Ean18> ean18Spec() {
        return this.specForValuesOf(new Ean18Factory());
    }

    @Override
    public <T> PropertySpecBuilderWizard.NlsOptions<T> specForValuesOf(ValueFactory<T> factory) {
        return basicPropertySpecService.specForValuesOf(factory);
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> stringSpec() {
        return basicPropertySpecService.stringSpec();
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Boolean> booleanSpec() {
        return basicPropertySpecService.booleanSpec();
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Long> longSpec() {
        return basicPropertySpecService.longSpec();
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> bigDecimalSpec() {
        return basicPropertySpecService.bigDecimalSpec();
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> boundedBigDecimalSpec(BigDecimal lowerLimit, BigDecimal upperLimit) {
        return basicPropertySpecService.boundedBigDecimalSpec(lowerLimit, upperLimit);
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> positiveBigDecimalSpec() {
        return basicPropertySpecService.positiveBigDecimalSpec();
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<TimeZone> timezoneSpec() {
        return basicPropertySpecService.timezoneSpec();
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<RelativePeriod> relativePeriodSpec() {
        return basicPropertySpecService.relativePeriodSpec();
    }

    @Override
    public <T> PropertySpecBuilderWizard.NlsOptions<T> referenceSpec(Class<T> apiClass) {
        return basicPropertySpecService.referenceSpec(apiClass);
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }
}