/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.umi;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.protocolimplv2.umi.UmiwanStdStatusCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UmiwanStdStatusCustomPropertySet implements CustomPropertySet<Device, UmiwanStdStatusCustomPropertySet.UmiwanStdStatusDomainExtension> {

    public static final String PREFIX = "UW1";
    public static final String TABLE = PREFIX + "_UMIWAN_STD_STATUS";

    private enum Fields {
        DOMAIN("device"),
        NEXT_CALL("nextCall"),
        LAST_TRY("lastTry"),
        LAST_CALL("lastCall"),
        LAST_DURATION("lastDuration"),
        SHORT_RETRY_CTR("shortRetryCtr"),
        LONG_RETRY_CTR("longRetryCtr"),
        ALL_FAILURE_CTR("allFailureCtr"),
        ALL_RETRY_CTR("allRetryCtr"),
        ALL_SUCCESS_CTR("allSuccessCtr"),
        RETRY_CTR_1("retryCtr1"),
        RETRY_CTR_2("retryCtr2"),
        RETRY_CTR_3("retryCtr3"),
        RETRY_CTR_4("retryCtr4"),
        ERROR_CODE("errorCode"),
        RETRY_TYPE("retryType"),
        STATUS_FLAG("statusFlag"),
        UMI_EMC_READ_FAIL_CTR("umiEmcReadFailCtr"),
        UMI_EMC_READ_PASS_CTR("umiEmcReadPassCtr");

        Fields(String javaName) {
            this.javaName = javaName;
        }

        private final String javaName;

        public String javaName() {
            return javaName;
        }
    }

    public static class UmiwanStdStatusDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<Device> {
        private Instant nextCall;
        private Instant lastTry;
        private Instant lastCall;
        private int lastDuration;
        private int shortRetryCtr;
        private int longRetryCtr;
        private int allFailureCtr;
        private int allRetryCtr;
        private int allSuccessCtr;
        private int retryCtr1;
        private int retryCtr2;
        private int retryCtr3;
        private int retryCtr4;
        private int errorCode;
        private int retryType;
        private Long statusFlag;
        private Long umiEmcReadFailCtr;
        private Long umiEmcReadPassCtr;

        private Reference<Device> device = ValueReference.absent();

        @Override
        public void copyFrom(Device domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
            this.device.set(domainInstance);
            this.nextCall = (Instant) propertyValues.getProperty(Fields.NEXT_CALL.javaName());
            this.lastTry = (Instant) propertyValues.getProperty(Fields.LAST_TRY.javaName());
            this.lastCall = (Instant) propertyValues.getProperty(Fields.LAST_CALL.javaName());
            this.lastDuration = ((Number) propertyValues.getProperty(Fields.LAST_DURATION.javaName())).intValue();
            this.shortRetryCtr = ((Number) propertyValues.getProperty(Fields.SHORT_RETRY_CTR.javaName())).intValue();
            this.longRetryCtr = ((Number) propertyValues.getProperty(Fields.LONG_RETRY_CTR.javaName())).intValue();
            this.allFailureCtr = ((Number) propertyValues.getProperty(Fields.ALL_FAILURE_CTR.javaName())).intValue();
            this.allRetryCtr = ((Number) propertyValues.getProperty(Fields.ALL_RETRY_CTR.javaName())).intValue();
            this.allSuccessCtr = ((Number) propertyValues.getProperty(Fields.ALL_SUCCESS_CTR.javaName())).intValue();
            this.retryCtr1 = ((Number) propertyValues.getProperty(Fields.RETRY_CTR_1.javaName())).intValue();
            this.retryCtr2 = ((Number) propertyValues.getProperty(Fields.RETRY_CTR_2.javaName())).intValue();
            this.retryCtr3 = ((Number) propertyValues.getProperty(Fields.RETRY_CTR_3.javaName())).intValue();
            this.retryCtr4 = ((Number) propertyValues.getProperty(Fields.RETRY_CTR_4.javaName())).intValue();
            this.errorCode = ((Number) propertyValues.getProperty(Fields.ERROR_CODE.javaName())).intValue();
            this.retryType = ((Number) propertyValues.getProperty(Fields.RETRY_TYPE.javaName())).intValue();
            this.statusFlag = (Long) propertyValues.getProperty(Fields.STATUS_FLAG.javaName());
            this.umiEmcReadFailCtr = (Long) propertyValues.getProperty(Fields.UMI_EMC_READ_FAIL_CTR.javaName());
            this.umiEmcReadPassCtr = (Long) propertyValues.getProperty(Fields.UMI_EMC_READ_PASS_CTR.javaName());
        }

        @Override
        public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
            propertySetValues.setProperty(Fields.NEXT_CALL.javaName(), this.nextCall);
            propertySetValues.setProperty(Fields.LAST_TRY.javaName(), this.lastTry);
            propertySetValues.setProperty(Fields.LAST_CALL.javaName(), this.lastCall);
            propertySetValues.setProperty(Fields.LAST_DURATION.javaName(), this.lastDuration);
            propertySetValues.setProperty(Fields.SHORT_RETRY_CTR.javaName(), this.shortRetryCtr);
            propertySetValues.setProperty(Fields.LONG_RETRY_CTR.javaName(), this.longRetryCtr);
            propertySetValues.setProperty(Fields.ALL_FAILURE_CTR.javaName(), this.allFailureCtr);
            propertySetValues.setProperty(Fields.ALL_RETRY_CTR.javaName(), this.allRetryCtr);
            propertySetValues.setProperty(Fields.ALL_SUCCESS_CTR.javaName(), this.allSuccessCtr);
            propertySetValues.setProperty(Fields.RETRY_CTR_1.javaName(), this.retryCtr1);
            propertySetValues.setProperty(Fields.RETRY_CTR_2.javaName(), this.retryCtr2);
            propertySetValues.setProperty(Fields.RETRY_CTR_3.javaName(), this.retryCtr3);
            propertySetValues.setProperty(Fields.RETRY_CTR_4.javaName(), this.retryCtr4);
            propertySetValues.setProperty(Fields.ERROR_CODE.javaName(), this.errorCode);
            propertySetValues.setProperty(Fields.RETRY_TYPE.javaName(), this.retryType);
            propertySetValues.setProperty(Fields.STATUS_FLAG.javaName(), this.statusFlag);
            propertySetValues.setProperty(Fields.UMI_EMC_READ_FAIL_CTR.javaName(), this.umiEmcReadFailCtr);
            propertySetValues.setProperty(Fields.UMI_EMC_READ_PASS_CTR.javaName(), this.umiEmcReadPassCtr);
        }

        @Override
        public void validateDelete() {
            // allow delete
        }
    }

    public static class UmiwanStdStatusPersistentSupport implements PersistenceSupport<Device, UmiwanStdStatusDomainExtension> {
        private final Thesaurus thesaurus;

        public UmiwanStdStatusPersistentSupport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public String componentName() {
            return PREFIX;
        }

        @Activate
        public void activate() {
            System.out.println(TABLE);
        }

        @Override
        public String tableName() {
            return TABLE;
        }

        @Override
        public String domainFieldName() {
            return Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return "FK" + TABLE + "_DEVICE";
        }

        @Override
        public Class<UmiwanStdStatusDomainExtension> persistenceClass() {
            return UmiwanStdStatusDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.of(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Thesaurus.class).toInstance(thesaurus);
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                }
            });
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table.column(Fields.NEXT_CALL.name()).map(Fields.NEXT_CALL.javaName()).number().conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column(Fields.LAST_TRY.name()).map(Fields.LAST_TRY.javaName()).number().conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column(Fields.LAST_CALL.name()).map(Fields.LAST_CALL.javaName()).number().conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column(Fields.LAST_DURATION.name()).map(Fields.LAST_DURATION.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.SHORT_RETRY_CTR.name()).map(Fields.SHORT_RETRY_CTR.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.LONG_RETRY_CTR.name()).map(Fields.LONG_RETRY_CTR.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.ALL_FAILURE_CTR.name()).map(Fields.ALL_FAILURE_CTR.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.ALL_RETRY_CTR.name()).map(Fields.ALL_RETRY_CTR.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.ALL_SUCCESS_CTR.name()).map(Fields.ALL_SUCCESS_CTR.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.RETRY_CTR_1.name()).map(Fields.RETRY_CTR_1.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.RETRY_CTR_2.name()).map(Fields.RETRY_CTR_2.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.RETRY_CTR_3.name()).map(Fields.RETRY_CTR_3.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.RETRY_CTR_4.name()).map(Fields.RETRY_CTR_4.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.ERROR_CODE.name()).map(Fields.ERROR_CODE.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.RETRY_TYPE.name()).map(Fields.RETRY_TYPE.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.STATUS_FLAG.name()).map(Fields.STATUS_FLAG.javaName()).number().conversion(ColumnConversion.NUMBER2LONG).add();
            table.column(Fields.UMI_EMC_READ_FAIL_CTR.name()).map(Fields.UMI_EMC_READ_FAIL_CTR.javaName()).number().conversion(ColumnConversion.NUMBER2LONG).add();
            table.column(Fields.UMI_EMC_READ_PASS_CTR.name()).map(Fields.UMI_EMC_READ_PASS_CTR.javaName()).number().conversion(ColumnConversion.NUMBER2LONG).add();
        }

        @Override
        public String application() {
            return "MDC";
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(Fields.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(Fields::name)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }
    }

    private Thesaurus thesaurus;
    private PropertySpecService propertySpecService;
    private DeviceService deviceService; // table ordering

    @SuppressWarnings("unused") // OSGI
    public UmiwanStdStatusCustomPropertySet() {
    }

    @SuppressWarnings("unused") // TESTS
    @Inject
    public UmiwanStdStatusCustomPropertySet(NlsService nlsService, PropertySpecService propertySpecService, DeviceService deviceService) {
        setNlsService(nlsService);
        setPropertySpecService(propertySpecService);
        setDeviceService(deviceService);
    }

    @SuppressWarnings("unused") // OSGI
    @org.osgi.service.component.annotations.Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceProtocolService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @SuppressWarnings("unused") // OSGI
    @org.osgi.service.component.annotations.Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @SuppressWarnings("unused") // OSGI, table ordering
    @org.osgi.service.component.annotations.Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @org.osgi.service.component.annotations.Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        //To make sure this CPS is started after the framework
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(TranslationKeys.UMI_STD_STATUS_NAME).format();
    }

    @Override
    public Class<Device> getDomainClass() {
        return Device.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.DOMAIN_NAME_DEVICE).format();
    }

    @Override
    public PersistenceSupport<Device, UmiwanStdStatusDomainExtension> getPersistenceSupport() {
        return new UmiwanStdStatusPersistentSupport(this.thesaurus);
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return EnumSet.allOf(ViewPrivilege.class);
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.allOf(EditPrivilege.class);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> properties = new ArrayList<>();
        properties.add(this.propertySpecService.specForValuesOf(new InstantFactory()).named(Fields.NEXT_CALL.javaName(), TranslationKeys.UMI_NEXT_CALL)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.specForValuesOf(new InstantFactory()).named(Fields.LAST_TRY.javaName(), TranslationKeys.UMI_LAST_TRY)
                .fromThesaurus(this.thesaurus)
                .finish());
        properties.add(this.propertySpecService.specForValuesOf(new InstantFactory()).named(Fields.LAST_CALL.javaName(), TranslationKeys.UMI_LAST_CALL)
                .fromThesaurus(this.thesaurus)
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.LAST_DURATION.javaName(), TranslationKeys.UMI_LAST_DURATION)
                .fromThesaurus(this.thesaurus)
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.SHORT_RETRY_CTR.javaName(), TranslationKeys.UMI_SHORT_RETRY_CTR)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.LONG_RETRY_CTR.javaName(), TranslationKeys.UMI_LONG_RETRY_CTR)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.ALL_FAILURE_CTR.javaName(), TranslationKeys.UMI_ALL_FAILURE_CTR)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.ALL_RETRY_CTR.javaName(), TranslationKeys.UMI_ALL_RETRY_CTR)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.ALL_SUCCESS_CTR.javaName(), TranslationKeys.UMI_ALL_SUCCESS_CTR)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.RETRY_CTR_1.javaName(), TranslationKeys.UMI_RETRY_CTR_1)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.RETRY_CTR_2.javaName(), TranslationKeys.UMI_RETRY_CTR_2)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.RETRY_CTR_3.javaName(), TranslationKeys.UMI_RETRY_CTR_3)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.RETRY_CTR_4.javaName(), TranslationKeys.UMI_RETRY_CTR_4)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.ERROR_CODE.javaName(), TranslationKeys.UMI_ERROR_CODE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.RETRY_TYPE.javaName(), TranslationKeys.UMI_RETRY_TYPE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.STATUS_FLAG.javaName(), TranslationKeys.UMI_STATUS_FLAG)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.UMI_EMC_READ_FAIL_CTR.javaName(), TranslationKeys.UMI_EMC_READ_FAIL_CTR)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.UMI_EMC_READ_PASS_CTR.javaName(), TranslationKeys.UMI_EMC_READ_PASS_CTR)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        return properties;
    }
}