package com.energyict.mdc.pluggable.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.impl.EncryptedStringFactory;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySelectionMode;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecPossibleValues;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PropertySpecService universal protocol property spec service}
 * delegating as much as possible to the {@link PropertySpecService multisense} property spec service.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (09:43)
 */
@Component(name = "com.energyict.mdc.protocol.pluggable.upl.propertyspecservice", service = {UPLPropertySpecServiceImpl.class})
@SuppressWarnings("unused")
public class UPLPropertySpecServiceImpl implements PropertySpecService {
    private volatile com.energyict.mdc.dynamic.PropertySpecService actual;
    private DataVaultService dataVaultService;

    private static com.elster.jupiter.properties.PropertySelectionMode fromUpl(PropertySelectionMode selectionMode) {
        switch (selectionMode) {
            case NONE: {
                return com.elster.jupiter.properties.PropertySelectionMode.UNSPECIFIED;
            }
            case LIST: {
                return com.elster.jupiter.properties.PropertySelectionMode.LIST;
            }
            case COMBOBOX: {
                return com.elster.jupiter.properties.PropertySelectionMode.COMBOBOX;
            }
            case SEARCH_AND_SELECT: {
                return com.elster.jupiter.properties.PropertySelectionMode.SEARCH_AND_SELECT;
            }
            default: {
                throw new IllegalArgumentException("Unexpected upl property selection mode: " + selectionMode.name());
            }
        }
    }

    // For OSGi framework
    public UPLPropertySpecServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public UPLPropertySpecServiceImpl(PropertySpecServiceImpl propertySpecService, DataVaultService dataVaultService) {
        this();
        this.setActualPropertySpecService(propertySpecService);
        this.setDataVaultService(dataVaultService);
    }

    @Reference
    public void setActualPropertySpecService(com.energyict.mdc.dynamic.PropertySpecService actual) {
        this.actual = actual;
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Activate
    public void activate() {
        Services.propertySpecService(this);
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> stringSpec() {
        return new NlsOptionsAdapter<>(this.actual.stringSpec());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> stringSpecOfExactLength(int length) {
        return new NlsOptionsAdapter<>(this.actual.specForValuesOf(new ExactLengthStringFactory(length)));
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> stringSpecOfMaximumLength(int length) {
        return new NlsOptionsAdapter<>(this.actual.specForValuesOf(new MaximumLengthStringFactory(length)));
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<HexString> hexStringSpec() {
        return new NlsOptionsAdapter<>(this.actual.hexStringSpec());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<HexString> hexStringSpecOfExactLength(int length) {
        return new NlsOptionsAdapter<>(this.actual.hexStringSpecOfExactLength(length));
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> encryptedStringSpec() {
        return new NlsOptionsAdapter<>(this.actual.specForValuesOf(new EncryptedStringFactory(this.dataVaultService)));
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Password> passwordSpec() {
        com.elster.jupiter.properties.PropertySpecBuilderWizard.NlsOptions<com.energyict.mdc.common.Password> spec = this.actual.passwordSpec();
        return new PasswordNlsOptionsAdapter(spec);
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Integer> integerSpec() {
        return new NlsOptionsAdapter<>(this.actual.specForValuesOf(new IntegerFactory()));
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Long> longSpec() {
        return new NlsOptionsAdapter<>(this.actual.longSpec());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> bigDecimalSpec() {
        return new NlsOptionsAdapter<>(this.actual.bigDecimalSpec());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> boundedBigDecimalSpec(BigDecimal lowerLimit, BigDecimal upperLimit) {
        return new NlsOptionsAdapter<>(this.actual.boundedBigDecimalSpec(lowerLimit, upperLimit));
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> positiveBigDecimalSpec() {
        return new NlsOptionsAdapter<>(this.actual.positiveBigDecimalSpec());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Boolean> booleanSpec() {
        return new NlsOptionsAdapter<>(this.actual.booleanSpec());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<TimeZone> timeZoneSpec() {
        return new NlsOptionsAdapter<>(this.actual.timezoneSpec());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Duration> durationSpec() {
        return new DurationNlsOptionsAdapter(this.actual.durationSpec());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<TemporalAmount> temporalAmountSpec() {
        return new DurationNlsOptionsAdapter(this.actual.temporalAmountSpec());
    }

    @Override
    public <T> PropertySpecBuilderWizard.NlsOptions<T> referenceSpec(Class<T> apiClass) {
        return new NlsOptionsAdapter<>(this.actual.referenceSpec(apiClass));
    }

    private static class NlsOptionsAdapter<T> implements PropertySpecBuilderWizard.NlsOptions<T> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.NlsOptions<T> actual;

        private NlsOptionsAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.NlsOptions<T> actual) {
            this.actual = actual;
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<T> named(TranslationKey nameTranslationKey) {
            return new ThesaurusBasedAdapter<>(this.actual.named(new ConnexoTranslationKeyAdapter(nameTranslationKey)));
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<T> named(String name, TranslationKey displayNameTranslationKey) {
            return new ThesaurusBasedAdapter<>(this.actual.named(name, new ConnexoTranslationKeyAdapter(displayNameTranslationKey)));
        }

        @Override
        public PropertySpecBuilderWizard.HardCoded<T> named(String name, String displayName) {
            return new HardCodedAdapter<>(this.actual.named(name, displayName));
        }
    }

    private static class ThesaurusBasedAdapter<T> implements PropertySpecBuilderWizard.ThesaurusBased<T> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.ThesaurusBased<T> actual;

        private ThesaurusBasedAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.ThesaurusBased<T> actual) {
            this.actual = actual;
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<T> describedAs(TranslationKey descriptionTranslationKey) {
            this.actual.describedAs(new ConnexoTranslationKeyAdapter(descriptionTranslationKey));
            return this;
        }

        @Override
        public PropertySpecBuilder<T> fromThesaurus(Thesaurus thesaurus) {
            return new PropertySpecBuilderAdapter<>(this.actual.fromThesaurus(new ConnexoThesaurusAdapter(thesaurus)));
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

    private static class PasswordNlsOptionsAdapter implements PropertySpecBuilderWizard.NlsOptions<Password> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.NlsOptions<com.energyict.mdc.common.Password> actual;

        private PasswordNlsOptionsAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.NlsOptions<com.energyict.mdc.common.Password> actual) {
            this.actual = actual;
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<Password> named(TranslationKey nameTranslationKey) {
            return new PasswordThesaurusBasedAdapter(this.actual.named(new ConnexoTranslationKeyAdapter(nameTranslationKey)));
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<Password> named(String name, TranslationKey displayNameTranslationKey) {
            return new PasswordThesaurusBasedAdapter(this.actual.named(name, new ConnexoTranslationKeyAdapter(displayNameTranslationKey)));
        }

        @Override
        public PropertySpecBuilderWizard.HardCoded<Password> named(String name, String displayName) {
            return new PasswordHardCodedAdapter(this.actual.named(name, displayName));
        }
    }

    private static class PasswordThesaurusBasedAdapter implements PropertySpecBuilderWizard.ThesaurusBased<Password> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.ThesaurusBased<com.energyict.mdc.common.Password> actual;

        private PasswordThesaurusBasedAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.ThesaurusBased<com.energyict.mdc.common.Password> actual) {
            this.actual = actual;
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<Password> describedAs(TranslationKey descriptionTranslationKey) {
            this.actual.describedAs(new ConnexoTranslationKeyAdapter(descriptionTranslationKey));
            return this;
        }

        @Override
        public PropertySpecBuilder<Password> fromThesaurus(Thesaurus thesaurus) {
            return new PasswordPropertySpecBuilderAdapter(this.actual.fromThesaurus(new ConnexoThesaurusAdapter(thesaurus)));
        }
    }

    private static class PasswordHardCodedAdapter implements PropertySpecBuilderWizard.HardCoded<Password> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.HardCoded<com.energyict.mdc.common.Password> actual;

        private PasswordHardCodedAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.HardCoded<com.energyict.mdc.common.Password> actual) {
            this.actual = actual;
        }

        @Override
        public PropertySpecBuilder<Password> describedAs(String description) {
            return new PasswordPropertySpecBuilderAdapter(this.actual.describedAs(description));
        }
    }

    private static class DurationNlsOptionsAdapter implements PropertySpecBuilderWizard.NlsOptions<Duration> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.NlsOptions<TimeDuration> actual;

        private DurationNlsOptionsAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.NlsOptions<TimeDuration> actual) {
            this.actual = actual;
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<Duration> named(TranslationKey nameTranslationKey) {
            return new DurationThesaurusBasedAdapter(this.actual.named(new ConnexoTranslationKeyAdapter(nameTranslationKey)));
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<Duration> named(String name, TranslationKey displayNameTranslationKey) {
            return new DurationThesaurusBasedAdapter(this.actual.named(name, new ConnexoTranslationKeyAdapter(displayNameTranslationKey)));
        }

        @Override
        public PropertySpecBuilderWizard.HardCoded<Duration> named(String name, String displayName) {
            return new DurationHardCodedAdapter(this.actual.named(name, displayName));
        }
    }

    private static class DurationThesaurusBasedAdapter implements PropertySpecBuilderWizard.ThesaurusBased<Duration> {
        private final com.elster.jupiter.properties.PropertySpecBuilderWizard.ThesaurusBased<TimeDuration> actual;

        private DurationThesaurusBasedAdapter(com.elster.jupiter.properties.PropertySpecBuilderWizard.ThesaurusBased<TimeDuration> actual) {
            this.actual = actual;
        }

        @Override
        public PropertySpecBuilder<Duration> describedAs(TranslationKey descriptionTranslationKey) {
            this.actual.describedAs(new ConnexoTranslationKeyAdapter(descriptionTranslationKey));
            return new DurationPropertySpecBuilderAdapter(this.actual.fromThesaurus(new ConnexoThesaurusAdapter(thesaurus)));
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
            this.actual.markExhaustive(fromUpl(selectionMode));
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
            return new PropertSpecAdapter(this.actual.finish());
        }
    }

    private static class PasswordPropertySpecBuilderAdapter implements PropertySpecBuilder<Password> {
        private final com.elster.jupiter.properties.PropertySpecBuilder<com.energyict.mdc.common.Password> actual;

        private PasswordPropertySpecBuilderAdapter(com.elster.jupiter.properties.PropertySpecBuilder<com.energyict.mdc.common.Password> actual) {
            this.actual = actual;
        }

        @Override
        public PropertySpecBuilder<Password> setDefaultValue(Password defaultValue) {
            this.actual.setDefaultValue(this.fromUpl(defaultValue));
            return this;
        }

        @Override
        public PropertySpecBuilder<Password> markExhaustive() {
            this.actual.markExhaustive();
            return this;
        }

        @Override
        public PropertySpecBuilder<Password> markExhaustive(PropertySelectionMode selectionMode) {
            this.actual.markExhaustive(UPLPropertySpecServiceImpl.fromUpl(selectionMode));
            return this;
        }

        @Override
        public PropertySpecBuilder<Password> markEditable() {
            this.actual.markEditable();
            return this;
        }

        @Override
        public PropertySpecBuilder<Password> markMultiValued() {
            this.actual.markMultiValued();
            return this;
        }

        @Override
        public PropertySpecBuilder<Password> markMultiValued(String separator) {
            this.actual.markMultiValued(separator);
            return this;
        }

        @Override
        public PropertySpecBuilder<Password> markRequired() {
            this.actual.markRequired();
            return this;
        }

        @Override
        public PropertySpecBuilder<Password> addValues(Password... values) {
            this.actual.addValues(Stream.of(values).map(this::fromUpl).collect(Collectors.toList()));
            return this;
        }

        @Override
        public PropertySpecBuilder<Password> addValues(List<Password> values) {
            this.actual.addValues(values.stream().map(this::fromUpl).collect(Collectors.toList()));
            return this;
        }

        private com.energyict.mdc.common.Password fromUpl(Password password) {
            return new com.energyict.mdc.common.Password(password.getValue());
        }

        @Override
        public PropertySpec finish() {
            return new PropertSpecAdapter(this.actual.finish());
        }
    }

    private static class DurationPropertySpecBuilderAdapter implements PropertySpecBuilder<Duration> {
        private final com.elster.jupiter.properties.PropertySpecBuilder<TimeDuration> actual;

        private DurationPropertySpecBuilderAdapter(com.elster.jupiter.properties.PropertySpecBuilder<TimeDuration> actual) {
            this.actual = actual;
        }

        @Override
        public PropertySpecBuilder<Duration> setDefaultValue(Duration defaultValue) {
            this.actual.setDefaultValue(this.fromUpl(defaultValue));
            return this;
        }

        @Override
        public PropertySpecBuilder<Duration> markExhaustive() {
            this.actual.markExhaustive();
            return this;
        }

        @Override
        public PropertySpecBuilder<Duration> markExhaustive(PropertySelectionMode selectionMode) {
            this.actual.markExhaustive(UPLPropertySpecServiceImpl.fromUpl(selectionMode));
            return this;
        }

        @Override
        public PropertySpecBuilder<Duration> markEditable() {
            this.actual.markEditable();
            return this;
        }

        @Override
        public PropertySpecBuilder<Duration> markMultiValued() {
            this.actual.markMultiValued();
            return this;
        }

        @Override
        public PropertySpecBuilder<Duration> markMultiValued(String separator) {
            this.actual.markMultiValued(separator);
            return this;
        }

        @Override
        public PropertySpecBuilder<Duration> markRequired() {
            this.actual.markRequired();
            return this;
        }

        @Override
        public PropertySpecBuilder<Duration> addValues(Duration... values) {
            this.actual.addValues(Stream.of(values).map(this::fromUpl).collect(Collectors.toList()));
            return this;
        }

        @Override
        public PropertySpecBuilder<Duration> addValues(List<Duration> values) {
            this.actual.addValues(values.stream().map(this::fromUpl).collect(Collectors.toList()));
            return this;
        }

        private TimeDuration fromUpl(Duration duration) {
            long remainingMillis = duration.toMillis() % 1000;
            if (remainingMillis == 0) {
                return TimeDuration.seconds((int) duration.getSeconds());
            } else {
                return TimeDuration.millis((int) duration.toMillis());
            }
        }

        @Override
        public PropertySpec finish() {
            return new PropertSpecAdapter(this.actual.finish());
        }
    }

    private static class PropertSpecAdapter implements PropertySpec {
        private final com.elster.jupiter.properties.PropertySpec actual;

        private PropertSpecAdapter(com.elster.jupiter.properties.PropertySpec actual) {
            this.actual = actual;
        }

        @Override
        public String getName() {
            return this.actual.getName();
        }

        @Override
        public String getDisplayName() {
            return this.actual.getDisplayName();
        }

        @Override
        public String getDescription() {
            return this.actual.getDescription();
        }

        @Override
        public boolean isRequired() {
            return this.actual.isRequired();
        }

        @Override
        public boolean validateValue(Object value) throws PropertyValidationException {
            try {
                return this.actual.validateValue(value);
            } catch (InvalidValueException e) {
                throw new InvalidPropertyException(e, e.getMessage());
            }
        }

        @Override
        public Optional<?> getDefaultValue() {
            com.elster.jupiter.properties.PropertySpecPossibleValues possibleValues = this.actual.getPossibleValues();
            if (possibleValues == null) {
                return Optional.empty();
            } else {
                return Optional.ofNullable(possibleValues.getDefault());
            }
        }

        @Override
        public PropertySpecPossibleValues getPossibleValues() {
            return new PossibleValuesAdapter(this.actual.getPossibleValues());
        }

        @Override
        public boolean supportsMultiValues() {
            return this.actual.supportsMultiValues();
        }
    }

    private static class PossibleValuesAdapter implements PropertySpecPossibleValues {
        private final com.elster.jupiter.properties.PropertySpecPossibleValues actual;

        private PossibleValuesAdapter(com.elster.jupiter.properties.PropertySpecPossibleValues actual) {
            this.actual = actual;
        }

        @Override
        public PropertySelectionMode getSelectionMode() {
            return this.toUpl(this.actual.getSelectionMode());
        }

        @Override
        public List<?> getAllValues() {
            return this.actual.getAllValues();
        }

        @Override
        public boolean isExhaustive() {
            return this.actual.isExhaustive();
        }

        @Override
        public boolean isEditable() {
            return this.actual.isEditable();
        }

        @Override
        public Object getDefault() {
            return this.actual.getDefault();
        }

        private PropertySelectionMode toUpl(com.elster.jupiter.properties.PropertySelectionMode selectionMode) {
            switch (selectionMode) {
                case UNSPECIFIED: {
                    return PropertySelectionMode.NONE;
                }
                case LIST: {
                    return PropertySelectionMode.LIST;
                }
                case COMBOBOX: {
                    return PropertySelectionMode.COMBOBOX;
                }
                case SEARCH_AND_SELECT: {
                    return PropertySelectionMode.SEARCH_AND_SELECT;
                }
                default: {
                    throw new IllegalArgumentException("Unexpected property selection mode: " + selectionMode.name());
                }
            }
        }

    }

    private static final class ExactLengthStringFactory extends StringFactory {
        private final int length;

        private ExactLengthStringFactory(int length) {
            this.length = length;
        }

        @Override
        public boolean isValid(java.lang.String value) {
            return value.length() == this.length;
        }

    }

    private static final class MaximumLengthStringFactory extends StringFactory {
        private final int maxLength;

        private MaximumLengthStringFactory(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public boolean isValid(java.lang.String value) {
            return value.length() <= this.maxLength;
        }

    }

}