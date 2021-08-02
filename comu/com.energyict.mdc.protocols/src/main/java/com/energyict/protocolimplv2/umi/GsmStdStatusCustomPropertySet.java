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
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.protocolimplv2.umi.GsmStdStatusCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class GsmStdStatusCustomPropertySet implements CustomPropertySet<Device, GsmStdStatusCustomPropertySet.GsmStdStatusDomainExtension> {

    public static final String PREFIX = "UW2";
    public static final String TABLE = PREFIX + "_GSM_STD_STATUS";

    private enum Fields {
        DOMAIN("device"),
        SUBSCRIBER_ID("subscriberId"),
        MODEM_MODEL("modemModel"),
        MODEM_REVISION("modemRevision"),
        MODEM_FIRMWARE("modemFirmware"),
        MODEM_SERIAL("modemSerial"),
        PROVIDER("provider"),
        LOCAL_IP_ADDR("localIpAddr"),
        LAST_ERROR_TIME("lastErrorTime"),
        LAST_ERROR_MSG("lastErrorMsg"),
        RSSI("rssi"),
        BER("ber"),
        BATTERY_VOLTAGE("batteryVoltage"),
        STATUS_FLAGS("statusFlags"),
        LAST_ERROR_CODE("lastErrorCode"),
        CME_ERROR("cmeError"),
        LAST_STATE("lastState"),
        ICCID("iccid");

        Fields(String javaName) {
            this.javaName = javaName;
        }

        private final String javaName;

        public String javaName() {
            return javaName;
        }
    }

    public static class GsmStdStatusDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<Device> {
        private String subscriberId;
        private String modemModel;
        private String modemRevision;
        private String modemFirmware;
        private String modemSerial;
        private String provider;
        private String localIpAddr;
        private Instant lastErrorTime;
        private String lastErrorMsg;
        private int rssi;
        private int ber;
        private int batteryVoltage;
        private int statusFlags;
        private int lastErrorCode;
        private int cmeError;
        private int lastState;
        private String iccid;

        private Reference<Device> device = ValueReference.absent();

        @Override
        public void copyFrom(Device domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
            this.device.set(domainInstance);
            this.subscriberId = (String) propertyValues.getProperty(Fields.SUBSCRIBER_ID.javaName());
            this.modemModel = (String) propertyValues.getProperty(Fields.MODEM_MODEL.javaName());
            this.modemRevision = (String) propertyValues.getProperty(Fields.MODEM_REVISION.javaName());
            this.modemFirmware = (String) propertyValues.getProperty(Fields.MODEM_FIRMWARE.javaName());
            this.modemSerial = (String) propertyValues.getProperty(Fields.MODEM_SERIAL.javaName());
            this.provider = (String) propertyValues.getProperty(Fields.PROVIDER.javaName());
            this.localIpAddr = (String) propertyValues.getProperty(Fields.LOCAL_IP_ADDR.javaName());
            this.lastErrorTime = (Instant) propertyValues.getProperty(Fields.LAST_ERROR_TIME.javaName());
            this.lastErrorMsg = (String) propertyValues.getProperty(Fields.LAST_ERROR_MSG.javaName());
            this.rssi = ((Number) propertyValues.getProperty(Fields.RSSI.javaName())).intValue();
            this.ber = ((Number) propertyValues.getProperty(Fields.BER.javaName())).intValue();
            this.batteryVoltage = ((Number) propertyValues.getProperty(Fields.BATTERY_VOLTAGE.javaName())).intValue();
            this.statusFlags = ((Number) propertyValues.getProperty(Fields.STATUS_FLAGS.javaName())).intValue();
            this.lastErrorCode = ((Number) propertyValues.getProperty(Fields.LAST_ERROR_CODE.javaName())).intValue();
            this.cmeError = ((Number) propertyValues.getProperty(Fields.CME_ERROR.javaName())).intValue();
            this.lastState = ((Number)propertyValues.getProperty(Fields.LAST_STATE.javaName())).intValue();
            this.iccid = (String) propertyValues.getProperty(Fields.ICCID.javaName());
        }

        @Override
        public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
            propertySetValues.setProperty(Fields.SUBSCRIBER_ID.javaName(), this.subscriberId);
            propertySetValues.setProperty(Fields.MODEM_MODEL.javaName(), this.modemModel);
            propertySetValues.setProperty(Fields.MODEM_REVISION.javaName(), this.modemRevision);
            propertySetValues.setProperty(Fields.MODEM_FIRMWARE.javaName(), this.modemFirmware);
            propertySetValues.setProperty(Fields.MODEM_SERIAL.javaName(), this.modemSerial);
            propertySetValues.setProperty(Fields.PROVIDER.javaName(), this.provider);
            propertySetValues.setProperty(Fields.LOCAL_IP_ADDR.javaName(), this.localIpAddr);
            propertySetValues.setProperty(Fields.LAST_ERROR_TIME.javaName(), this.lastErrorTime);
            propertySetValues.setProperty(Fields.LAST_ERROR_MSG.javaName(), this.lastErrorMsg);
            propertySetValues.setProperty(Fields.RSSI.javaName(), this.rssi);
            propertySetValues.setProperty(Fields.BER.javaName(), this.ber);
            propertySetValues.setProperty(Fields.BATTERY_VOLTAGE.javaName(), this.batteryVoltage);
            propertySetValues.setProperty(Fields.STATUS_FLAGS.javaName(), this.statusFlags);
            propertySetValues.setProperty(Fields.LAST_ERROR_CODE.javaName(), this.lastErrorCode);
            propertySetValues.setProperty(Fields.CME_ERROR.javaName(), this.cmeError);
            propertySetValues.setProperty(Fields.LAST_STATE.javaName(), this.lastState);
            propertySetValues.setProperty(Fields.ICCID.javaName(), this.iccid);
        }

        @Override
        public void validateDelete() {
            // allow delete
        }
    }

    public static class GsmStdStatusPersistentSupport implements PersistenceSupport<Device, GsmStdStatusDomainExtension> {
        private final Thesaurus thesaurus;

        public GsmStdStatusPersistentSupport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public String componentName() {
            return PREFIX;
        }

        @Activate
        public void activate() {
            //activate
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
        public Class<GsmStdStatusDomainExtension> persistenceClass() {
            return GsmStdStatusDomainExtension.class;
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
            table.column(Fields.SUBSCRIBER_ID.name()).map(Fields.SUBSCRIBER_ID.javaName()).varChar(Table.NAME_LENGTH).add();
            table.column(Fields.MODEM_MODEL.name()).map(Fields.MODEM_MODEL.javaName()).varChar(Table.NAME_LENGTH).add();
            table.column(Fields.MODEM_REVISION.name()).map(Fields.MODEM_REVISION.javaName()).varChar(Table.NAME_LENGTH).add();
            table.column(Fields.MODEM_FIRMWARE.name()).map(Fields.MODEM_FIRMWARE.javaName()).varChar(Table.NAME_LENGTH).add();
            table.column(Fields.MODEM_SERIAL.name()).map(Fields.MODEM_SERIAL.javaName()).varChar(Table.NAME_LENGTH).add();
            table.column(Fields.PROVIDER.name()).map(Fields.PROVIDER.javaName()).varChar(Table.NAME_LENGTH).add();
            table.column(Fields.LOCAL_IP_ADDR.name()).map(Fields.LOCAL_IP_ADDR.javaName()).varChar(Table.NAME_LENGTH).add();
            table.column(Fields.LAST_ERROR_TIME.name()).map(Fields.LAST_ERROR_TIME.javaName()).number().conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column(Fields.LAST_ERROR_MSG.name()).map(Fields.LAST_ERROR_MSG.javaName()).varChar(Table.NAME_LENGTH).add();
            table.column(Fields.RSSI.name()).map(Fields.RSSI.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.BER.name()).map(Fields.BER.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.BATTERY_VOLTAGE.name()).map(Fields.BATTERY_VOLTAGE.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.STATUS_FLAGS.name()).map(Fields.STATUS_FLAGS.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.LAST_ERROR_CODE.name()).map(Fields.LAST_ERROR_CODE.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.CME_ERROR.name()).map(Fields.CME_ERROR.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.LAST_STATE.name()).map(Fields.LAST_STATE.javaName()).number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column(Fields.ICCID.name()).map(Fields.ICCID.javaName()).varChar(Table.NAME_LENGTH).add();
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
    public GsmStdStatusCustomPropertySet() {
    }

    @SuppressWarnings("unused") // TESTS
    @Inject
    public GsmStdStatusCustomPropertySet(NlsService nlsService, PropertySpecService propertySpecService, DeviceService deviceService) {
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
    public void setCustomPropertySetService (CustomPropertySetService customPropertySetService) {
        //To make sure this CPS is started after the framework
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(TranslationKeys.UMI_GSM_STD_STATUS_NAME).format();
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
    public PersistenceSupport<Device, GsmStdStatusDomainExtension> getPersistenceSupport() {
        return new GsmStdStatusPersistentSupport(this.thesaurus);
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
        properties.add(this.propertySpecService.stringSpec().named(Fields.SUBSCRIBER_ID.javaName(), TranslationKeys.UMI_SUBSCRIBER_ID)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.stringSpec().named(Fields.MODEM_MODEL.javaName(), TranslationKeys.UMI_MODEM_MODEL)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.stringSpec().named(Fields.MODEM_REVISION.javaName(), TranslationKeys.UMI_MODEM_REVISION)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.stringSpec().named(Fields.MODEM_FIRMWARE.javaName(), TranslationKeys.UMI_MODEM_FIRMWARE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.stringSpec().named(Fields.MODEM_SERIAL.javaName(), TranslationKeys.UMI_MODEM_SERIAL)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.stringSpec().named(Fields.PROVIDER.javaName(), TranslationKeys.UMI_PROVIDER)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.stringSpec().named(Fields.LOCAL_IP_ADDR.javaName(), TranslationKeys.UMI_LOCAL_IP_ADDR)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.specForValuesOf(new InstantFactory()).named(Fields.LAST_ERROR_TIME.javaName(), TranslationKeys.UMI_LAST_ERROR_TIME)
                .fromThesaurus(this.thesaurus)
                .finish());
        properties.add(this.propertySpecService.stringSpec().named(Fields.LAST_ERROR_MSG.javaName(), TranslationKeys.UMI_LAST_ERROR_MSG)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.RSSI.javaName(), TranslationKeys.UMI_RSSI)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.BER.javaName(), TranslationKeys.UMI_BER)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.BATTERY_VOLTAGE.javaName(), TranslationKeys.UMI_BATTERY_VOLTAGE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.STATUS_FLAGS.javaName(), TranslationKeys.UMI_STATUS_FLAGS)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.LAST_ERROR_CODE.javaName(), TranslationKeys.UMI_LAST_ERROR_CODE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.CME_ERROR.javaName(), TranslationKeys.UMI_CME_ERROR)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.LAST_STATE.javaName(), TranslationKeys.UMI_LAST_STATE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.stringSpec().named(Fields.ICCID.javaName(), TranslationKeys.UMI_ICCID)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        return properties;
    }
}