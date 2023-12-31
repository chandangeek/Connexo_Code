package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.protocol.DeviceMessageFile;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.LocalTimeFactory;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.NumberLookup;
import com.energyict.mdc.upl.properties.PropertySelectionMode;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.StringLookup;

import com.energyict.obis.ObisCode;
import com.google.inject.Inject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PropertySpecService universal protocol property spec service}
 * delegating as much as possible to the {@link PropertySpecService multisense} property spec service.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (09:43)
 */
@Component(name = "com.energyict.mdc.protocol.pluggable.upl.propertyspecservice", service = {PropertySpecService.class}, immediate = true)
@SuppressWarnings("unused")
public class UPLPropertySpecServiceImpl implements PropertySpecService {

    private volatile com.energyict.mdc.dynamic.PropertySpecService actual;
    private volatile NlsService nlsService;

    private Thesaurus protocolThesaurus;

    // For OSGi framework
    public UPLPropertySpecServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public UPLPropertySpecServiceImpl(com.energyict.mdc.dynamic.PropertySpecService propertySpecService, NlsService nlsService) {
        this();
        this.setActualPropertySpecService(propertySpecService);
        this.setNlsService(nlsService);
    }

    @Reference
    public void setActualPropertySpecService(com.energyict.mdc.dynamic.PropertySpecService actual) {
        this.actual = actual;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Activate
    public void activate() {
        Services.propertySpecService(this);
    }

    @Deactivate
    public void deactivate() {
        Services.propertySpecService(null);
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> stringSpec() {
        return new NlsOptionsAdapter<>(this.actual.stringSpec(), getProtocolThesaurus());
    }

    private Thesaurus getProtocolThesaurus() {
        if (protocolThesaurus == null) {
            protocolThesaurus = this.nlsService.getThesaurus(DeviceProtocolService.COMPONENT_NAME, Layer.DOMAIN);
        }
        return protocolThesaurus;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> stringSpecOfExactLength(int length) {
        return new NlsOptionsAdapter<>(this.actual.specForValuesOf(new ExactLengthStringFactory(length)), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> stringSpecOfMaximumLength(int length) {
        return new NlsOptionsAdapter<>(this.actual.specForValuesOf(new MaximumLengthStringFactory(length)), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<HexString> hexStringSpec() {
        return new NlsOptionsAdapter<>(this.actual.hexStringSpec(), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<HexString> hexStringSpecOfExactLength(int length) {
        return new NlsOptionsAdapter<>(this.actual.hexStringSpecOfExactLength(length), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> textareaStringSpec() {
        return new NlsOptionsAdapter<>(this.actual.textareaStringSpec(), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> encryptedStringSpec() {
        return new NlsOptionsAdapter<>(this.actual.encryptedStringSpec(), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Integer> integerSpec() {
        return new NlsOptionsAdapter<>(this.actual.specForValuesOf(new IntegerFactory()), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Long> longSpec() {
        return new NlsOptionsAdapter<>(this.actual.longSpec(), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> bigDecimalSpec() {
        return new NlsOptionsAdapter<>(this.actual.bigDecimalSpec(), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> boundedBigDecimalSpec(BigDecimal lowerLimit, BigDecimal upperLimit) {
        return new NlsOptionsAdapter<>(this.actual.boundedBigDecimalSpec(lowerLimit, upperLimit), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> positiveBigDecimalSpec() {
        return new NlsOptionsAdapter<>(this.actual.positiveBigDecimalSpec(), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Boolean> booleanSpec() {
        return new NlsOptionsAdapter<>(this.actual.booleanSpec(), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<TimeZone> timeZoneSpec() {
        return new NlsOptionsAdapter<>(this.actual.timezoneSpec(), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Duration> durationSpec() {
        return new DurationNlsOptionsAdapter(this.actual.durationSpec(), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<TemporalAmount> temporalAmountSpec() {
        return new TemporalAmountNlsOptionsAdapter(this.actual.temporalAmountSpec(), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Date> dateTimeSpec() {
        return new NlsOptionsAdapter<>(this.actual.specForValuesOf(new DateAndTimeFactory()), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Date> dateSpec() {
        return new NlsOptionsAdapter<>(this.actual.specForValuesOf(new DateFactory()), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<LocalTime> timeSpec() {
        return new NlsOptionsAdapter<>(this.actual.specForValuesOf(new LocalTimeFactory()), getProtocolThesaurus());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<ObisCode> obisCodeSpec() {
        return new NlsOptionsAdapter<>(this.actual.obisCodeSpec(), getProtocolThesaurus());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> PropertySpecBuilderWizard.NlsOptions<T> referenceSpec(String apiClassName) {
        return new NlsOptionsAdapter<>(
                this.actual.referenceSpec(SupportedReferenceClass.fromClassName(apiClassName).connexoClass()),
                getProtocolThesaurus());
    }

    private enum SupportedReferenceClass {
        FIRMWARE_VERSION("com.energyict.mdc.upl.properties.FirmwareVersion", BaseFirmwareVersion.class),
        DEVICE_MESSAGE_FILE("com.energyict.mdc.upl.properties.DeviceMessageFile", DeviceMessageFile.class),
        TARIFF_CALENDAR("com.energyict.mdc.upl.properties.TariffCalendar", Calendar.class),

        LOAD_PROFILE("com.energyict.mdc.upl.meterdata.LoadProfile", LoadProfile.class),
        DEVICE_GROUP("com.energyict.mdc.upl.properties.DeviceGroup", EndDeviceGroup.class),

        NUMBER_LOOKUP("com.energyict.mdc.upl.properties.NumberLookup", NumberLookup.class),
        STRING_LOOKUP("com.energyict.mdc.upl.properties.StringLookup", StringLookup.class),

        KEY_ACCESSOR_TYPE("com.energyict.mdc.upl.security.KeyAccessorType", SecurityAccessorType.class);

        private final String uplClassName;
        private final Class connexoClass;

        SupportedReferenceClass(String uplClassName, Class connexoClass) {
            this.uplClassName = uplClassName;
            this.connexoClass = connexoClass;
        }

        static SupportedReferenceClass fromClassName(String uplClassName) {
            return Stream
                    .of(values())
                    .filter(each -> each.uplClassName.equals(uplClassName))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("The class " + uplClassName + " is not supported by " + UPLPropertySpecServiceImpl.class.getSimpleName() + " yet"));
        }

        Class connexoClass() {
            return this.connexoClass;
        }
    }

    private static class NlsOptionsAdapter<T> implements PropertySpecBuilderWizard.NlsOptions<T> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.NlsOptions<T> actual;
        private final Thesaurus thesaurus;

        private NlsOptionsAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.NlsOptions<T> actual, Thesaurus thesaurus) {
            this.actual = actual;
            this.thesaurus = thesaurus;
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<T> named(TranslationKey nameTranslationKey) {
            return new ThesaurusBasedAdapter<>(this.actual.named(ConnexoTranslationKeyAdapter.adaptTo(nameTranslationKey)), this.thesaurus);
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<T> named(String name, TranslationKey displayNameTranslationKey) {
            return new ThesaurusBasedAdapter<>(this.actual.named(name, ConnexoTranslationKeyAdapter.adaptTo(displayNameTranslationKey)), this.thesaurus);
        }

        @Override
        public PropertySpecBuilderWizard.HardCoded<T> named(String name, String displayName) {
            return new HardCodedAdapter<>(this.actual.named(name, displayName));
        }
    }

    private static class ThesaurusBasedAdapter<T> implements PropertySpecBuilderWizard.ThesaurusBased<T> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.ThesaurusBased<T> actual;
        private final com.elster.jupiter.nls.Thesaurus thesaurus;

        private ThesaurusBasedAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.ThesaurusBased<T> actual, com.elster.jupiter.nls.Thesaurus thesaurus) {
            this.actual = actual;
            this.thesaurus = thesaurus;
        }

        @Override
        public PropertySpecBuilder<T> describedAs(TranslationKey descriptionTranslationKey) {
            this.actual.describedAs(ConnexoTranslationKeyAdapter.adaptTo(descriptionTranslationKey));
            return new PropertySpecBuilderAdapter<>(this.actual.fromThesaurus(this.thesaurus));
        }

    }

    private static class HardCodedAdapter<T> implements PropertySpecBuilderWizard.HardCoded<T> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.HardCoded<T> actual;

        private HardCodedAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.HardCoded<T> actual) {
            this.actual = actual;
        }

        @Override
        public PropertySpecBuilder<T> describedAs(String description) {
            return new PropertySpecBuilderAdapter<>(this.actual.describedAs(description));
        }
    }

    private static class DurationNlsOptionsAdapter implements PropertySpecBuilderWizard.NlsOptions<Duration> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.NlsOptions<TimeDuration> actual;
        private final com.elster.jupiter.nls.Thesaurus thesaurus;

        private DurationNlsOptionsAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.NlsOptions<TimeDuration> actual, com.elster.jupiter.nls.Thesaurus thesaurus) {
            this.actual = actual;
            this.thesaurus = thesaurus;
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<Duration> named(TranslationKey nameTranslationKey) {
            return new DurationThesaurusBasedAdapter(this.actual.named(ConnexoTranslationKeyAdapter.adaptTo(nameTranslationKey)), this.thesaurus);
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<Duration> named(String name, TranslationKey displayNameTranslationKey) {
            return new DurationThesaurusBasedAdapter(this.actual.named(name, ConnexoTranslationKeyAdapter.adaptTo(displayNameTranslationKey)), thesaurus);
        }

        @Override
        public PropertySpecBuilderWizard.HardCoded<Duration> named(String name, String displayName) {
            return new DurationHardCodedAdapter(this.actual.named(name, displayName));
        }
    }

    private static class DurationThesaurusBasedAdapter implements PropertySpecBuilderWizard.ThesaurusBased<Duration> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.ThesaurusBased<TimeDuration> actual;
        private final com.elster.jupiter.nls.Thesaurus thesaurus;

        private DurationThesaurusBasedAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.ThesaurusBased<TimeDuration> actual, com.elster.jupiter.nls.Thesaurus thesaurus) {
            this.actual = actual;
            this.thesaurus = thesaurus;
        }

        @Override
        public PropertySpecBuilder<Duration> describedAs(TranslationKey descriptionTranslationKey) {
            this.actual.describedAs(ConnexoTranslationKeyAdapter.adaptTo(descriptionTranslationKey));
            return new DurationPropertySpecBuilderAdapter(this.actual.fromThesaurus(this.thesaurus));
        }
    }

    private static class DurationHardCodedAdapter implements PropertySpecBuilderWizard.HardCoded<Duration> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.HardCoded<TimeDuration> actual;

        private DurationHardCodedAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.HardCoded<TimeDuration> actual) {
            this.actual = actual;
        }

        @Override
        public PropertySpecBuilder<Duration> describedAs(String description) {
            return new DurationPropertySpecBuilderAdapter(this.actual.describedAs(description));
        }
    }

    private static class TemporalAmountNlsOptionsAdapter implements PropertySpecBuilderWizard.NlsOptions<TemporalAmount> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.NlsOptions<TimeDuration> actual;
        private final com.elster.jupiter.nls.Thesaurus thesaurus;

        private TemporalAmountNlsOptionsAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.NlsOptions<TimeDuration> actual, com.elster.jupiter.nls.Thesaurus thesaurus) {
            this.actual = actual;
            this.thesaurus = thesaurus;
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<TemporalAmount> named(TranslationKey nameTranslationKey) {
            return new TemporalAmountThesaurusBasedAdapter(this.actual.named(ConnexoTranslationKeyAdapter.adaptTo(nameTranslationKey)), this.thesaurus);
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<TemporalAmount> named(String name, TranslationKey displayNameTranslationKey) {
            return new TemporalAmountThesaurusBasedAdapter(this.actual.named(name, ConnexoTranslationKeyAdapter.adaptTo(displayNameTranslationKey)), thesaurus);
        }

        @Override
        public PropertySpecBuilderWizard.HardCoded<TemporalAmount> named(String name, String displayName) {
            return new TemporalAmountHardCodedAdapter(this.actual.named(name, displayName));
        }
    }

    private static class TemporalAmountThesaurusBasedAdapter implements PropertySpecBuilderWizard.ThesaurusBased<TemporalAmount> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.ThesaurusBased<TimeDuration> actual;
        private final com.elster.jupiter.nls.Thesaurus thesaurus;

        private TemporalAmountThesaurusBasedAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.ThesaurusBased<TimeDuration> actual, com.elster.jupiter.nls.Thesaurus thesaurus) {
            this.actual = actual;
            this.thesaurus = thesaurus;
        }

        @Override
        public PropertySpecBuilder<TemporalAmount> describedAs(TranslationKey descriptionTranslationKey) {
            this.actual.describedAs(ConnexoTranslationKeyAdapter.adaptTo(descriptionTranslationKey));
            return new TemporalAmountPropertySpecBuilderAdapter(this.actual.fromThesaurus(this.thesaurus));
        }
    }

    private static class TemporalAmountHardCodedAdapter implements PropertySpecBuilderWizard.HardCoded<TemporalAmount> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.HardCoded<TimeDuration> actual;

        private TemporalAmountHardCodedAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.HardCoded<TimeDuration> actual) {
            this.actual = actual;
        }

        @Override
        public PropertySpecBuilder<TemporalAmount> describedAs(String description) {
            return new TemporalAmountPropertySpecBuilderAdapter(this.actual.describedAs(description));
        }
    }

    private static class PropertySpecBuilderAdapter<T> implements PropertySpecBuilder<T> {
        private final com.elster.jupiter.properties.PropertySpecBuilder<T> actual;

        private PropertySpecBuilderAdapter(com.elster.jupiter.properties.PropertySpecBuilder<T> actual) {
            this.actual = actual;
        }

        @Override
        public PropertySpecBuilder<T> setDefaultValue(T defaultValue) {
            this.actual.setDefaultValue(defaultValue);
            return this;
        }

        @Override
        public PropertySpecBuilder<T> markExhaustive() {
            this.actual.markExhaustive();
            return this;
        }

        @Override
        public PropertySpecBuilder<T> markExhaustive(PropertySelectionMode selectionMode) {
            this.actual.markExhaustive(PropertySelectionModeConverter.fromUpl(selectionMode));
            return this;
        }

        @Override
        public PropertySpecBuilder<T> markEditable() {
            this.actual.markEditable();
            return this;
        }

        @Override
        public PropertySpecBuilder<T> markMultiValued() {
            this.actual.markMultiValued();
            return this;
        }

        @Override
        public PropertySpecBuilder<T> markMultiValued(String separator) {
            this.actual.markMultiValued(separator);
            return this;
        }

        @Override
        public PropertySpecBuilder<T> markRequired() {
            this.actual.markRequired();
            return this;
        }

        @Override
        public PropertySpecBuilder<T> addValues(T... values) {
            this.actual.addValues(values);
            return this;
        }

        @Override
        public PropertySpecBuilder<T> addValues(List<T> values) {
            this.actual.addValues(values);
            return this;
        }

        @Override
        public PropertySpec finish() {
            return ConnexoToUPLPropertSpecAdapter.adaptTo(this.actual.finish());
        }
    }
}