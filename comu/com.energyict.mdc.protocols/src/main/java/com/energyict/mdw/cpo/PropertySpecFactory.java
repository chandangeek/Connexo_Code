package com.energyict.mdw.cpo;

import com.energyict.mdw.dynamicattributes.BigDecimalFactory;
import com.energyict.mdw.dynamicattributes.BooleanFactory;
import com.energyict.mdw.dynamicattributes.DateAndTimeFactory;
import com.energyict.mdw.dynamicattributes.DateFactory;
import com.energyict.mdw.dynamicattributes.Ean13Factory;
import com.energyict.mdw.dynamicattributes.Ean18Factory;
import com.energyict.mdw.dynamicattributes.HexStringFactory;
import com.energyict.mdw.dynamicattributes.LargeStringFactory;
import com.energyict.mdw.dynamicattributes.ObisCodeValueFactory;
import com.energyict.mdw.dynamicattributes.PasswordFactory;
import com.energyict.mdw.dynamicattributes.StringFactory;
import com.energyict.mdw.dynamicattributes.ThreeStateFactory;
import com.energyict.mdw.dynamicattributes.TimeDurationValueFactory;
import com.energyict.mdw.dynamicattributes.TimeOfDayFactory;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TimeOfDay;
import com.energyict.mdc.common.ean.Ean13;
import com.energyict.mdc.common.ean.Ean18;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.device.Channel;
import com.energyict.mdc.protocol.api.device.LoadProfile;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeValueSelectionMode;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory;
import com.energyict.mdc.protocol.api.lookups.Lookup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides factory services for {@link PropertySpec}s.
 * <p/>
 * User: jbr
 * Date: 7/05/12
 * Time: 10:56
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-02 (14:19)
 */
public class PropertySpecFactory {

    /**
     * Creates a PropertySpec for a String value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<String> stringPropertySpec(String name) {
        return simplePropertySpec(name, String.class, new StringFactory());
    }

    /**
     * Creates a PropertySpec for a String value, taking into account its default value.
     *
     * @param name The name of the PropertySpec
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public static PropertySpec<String> stringPropertySpec(String name, String defaultValue) {
        return PropertySpecBuilder.
                forClass(String.class, new StringFactory()).
                name(name).
                setDefaultValue(defaultValue).
                finish();
    }

    /**
     * Creates a PropertySpec for a String value which only allows the given values
     *
     * @param name   The name of the PropertySpec
     * @param values the allowed values for this StringPropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<String> stringPropertySpecWithValues(String name, String... values) {
        return PropertySpecBuilder.
                forClass(String.class, new StringFactory()).
                name(name).addValues(values).
                markExhaustive().
                finish();
    }

    /**
     * Creates a PropertySpec for a String value which only allows the given values
     *
     * @param name   The name of the PropertySpec
     * @param values the allowed values for this StringPropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<String> stringPropertySpecWithValuesAndDefaultValue(String name, String defaultValue, String... values) {
        return PropertySpecBuilder.
                forClass(String.class, new StringFactory()).
                name(name).addValues(values).
                markExhaustive().
                setDefaultValue(defaultValue).
                finish();
    }

    /**
     * Creates a PropertySpec for a "large" String value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<String> largeStringPropertySpec(String name) {
        return simplePropertySpec(name, String.class, new LargeStringFactory());
    }

    /**
     * Creates a PropertySpec for a String value having a fixed length.
     *
     * @param name The name of the PropertySpec
     * @param length The desired length of the String
     * @return The PropertySpec
     */
    public static PropertySpec<String> fixedLengthStringPropertySpec(String name, int length) {
        return new FixedLengthStringPropertySpec(name, length);
    }

    /**
     * Creates a PropertySpec for a HexString value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<HexString> hexStringPropertySpec(String name) {
        return simplePropertySpec(name, HexString.class, new HexStringFactory());
    }

    /**
     * Creates a PropertySpec for a HexString value having a fixed length.
     *
     * @param name The name of the PropertySpec
     * @param length The desired length of the HexString, specified in nr of bytes (e.g. length 16 corresponds to 16 bytes or 32 Hex chars)
     * @return The PropertySpec
     */
    public static PropertySpec<HexString> fixedLengthHexStringPropertySpec(String name, int length) {
        return new FixedLengthHexStringPropertySpec(name, length);
    }

    /**
     * Creates a PropertySpec for a {@link Password} values.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<Password> passwordPropertySpec(String name) {
        return simplePropertySpec(name, Password.class, new PasswordFactory());
    }

    /**
     * Creates a PropertySpec for a BigDecimal value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<BigDecimal> bigDecimalPropertySpec(String name) {
        return simplePropertySpec(name, BigDecimal.class, new BigDecimalFactory());
    }

    /**
     * Creates a PropertySpec for a BigDecimal value, taking into account its default value.
     *
     * @param name The name of the PropertySpec
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public static PropertySpec<BigDecimal> bigDecimalPropertySpec(String name, BigDecimal defaultValue) {
        return PropertySpecBuilder.
                forClass(BigDecimal.class, new BigDecimalFactory()).
                name(name).
                setDefaultValue(defaultValue).
                finish();
    }

    /**
     * Creates a BigDecimal PropertySpec which only allows the given values
     *
     * @param name   the name of the PropertySpec
     * @param values the allowed values for this BigDecimalPropertySpec
     * @return the PropertySpec
     */
    public static PropertySpec<BigDecimal> bigDecimalPropertySpecWithValues(String name, BigDecimal... values) {
        return PropertySpecBuilder.
                forClass(BigDecimal.class, new BigDecimalFactory()).
                name(name).addValues(values).
                markExhaustive().
                finish();
    }

    /**
     * Creates a BigDecimal PropertySpec which only allows to values between the lowerLimit and the upperLimit (inclusive)
     *
     * @param name   the name of the PropertySpec
     * @param lowerLimit lowest value allowed
     * @param upperLimit largest value allowed
     * @return the PropertySpec
     */
    public static PropertySpec<BigDecimal> boundedDecimalPropertySpec(String name, BigDecimal lowerLimit, BigDecimal upperLimit) {
        return new BoundedBigDecimalPropertySpec(name, lowerLimit, upperLimit);
    }

    /**
     * Creates a BigDecimal PropertySpec which only allows positive values
     *
     * @param name   the name of the PropertySpec
     * @return the PropertySpec
     */
    public static PropertySpec<BigDecimal> positiveDecimalPropertySpec(String name) {
        return new BoundedBigDecimalPropertySpec(name, BigDecimal.ZERO, null);
    }

    /**
     * Creates a PropertySpec for a Boolean value.
     * This property can have <i>three</i> values:
     * <ul>
     * <li>False</li>
     * <li>True</li>
     * <li>Unknown</li>
     * </ul>
     * The UI will be able to represent the three values.
     * Choosing Unknown will result in an empty property.<br/>
     * If this is not your desired behavior, consider using {@link #notNullableBooleanPropertySpec(String)}
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<Boolean> booleanPropertySpec(String name) {
        return simplePropertySpec(name, Boolean.class, new ThreeStateFactory());
    }

    /**
     * Creates a PropertySpec for a Boolean value <b>which cannot be set to null</b>.
     * This means that this property will always be configured (by default false).<br/>
     * If you don't want this behavior for your property,
     * consider using {@link #booleanPropertySpec(String)}.
     * This one allows to set the value to "Unknown",
     * which will result in a <code>null</code> property value.
     *
     * @param name the name of the PropertySpec
     * @return the PropertySpec
     */
    public static PropertySpec<Boolean> notNullableBooleanPropertySpec (String name) {
        return simplePropertySpec(name, Boolean.class, new BooleanFactory());
    }

    /**
     * Creates a PropertySpec for a Date value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<Date> datePropertySpec(String name) {
        return simplePropertySpec(name, Date.class, new DateFactory());
    }

    /**
     * Creates a PropertySpec for a {@link TimeOfDay} value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<TimeOfDay> timeOfDayPropertySpec(String name) {
        return simplePropertySpec(name, TimeOfDay.class, new TimeOfDayFactory());
    }

    /**
     * Creates a PropertySpec for a Date value with time resolution.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<Date> dateTimePropertySpec(String name) {
        return simplePropertySpec(name, Date.class, new DateAndTimeFactory());
    }

    /**
     * Creates a PropertySpec for a {@link TimeDuration} value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<TimeDuration> timeDurationPropertySpec(String name) {
        return simplePropertySpec(name, TimeDuration.class, new TimeDurationValueFactory());
    }

    /**
     * Creates a PropertySpec for a {@link TimeDuration} value, taking into account its default value.
     *
     * @param name The name of the PropertySpec
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public static PropertySpec<TimeDuration> timeDurationPropertySpec(String name, TimeDuration defaultValue) {
        return PropertySpecBuilder.
                forClass(TimeDuration.class, new TimeDurationValueFactory()).
                name(name).
                setDefaultValue(defaultValue).
                finish();
    }

    /**
     * Creates a PropertySpec for a {@link TimeDuration} value which only allows the given values
     *
     * @param name   The name of the PropertySpec
     * @param values the allowed values for this TimeDurationPropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<TimeDuration> timeDurationPropertySpecWithValues(String name, TimeDuration... values) {
        return PropertySpecBuilder.
                forClass(TimeDuration.class, new TimeDurationValueFactory()).
                name(name).addValues(values).
                finish();
    }

    /**
     * Creates a PropertySpec for a {@link TimeDuration} value. Only the small units will be
     * displayed in the editor.
     * <ul>
     *     <li>{@link TimeDuration#MILLISECONDS}</li>
     *     <li>{@link TimeDuration#SECONDS}</li>
     *     <li>{@link TimeDuration#MINUTES}</li>
     *     <li>{@link TimeDuration#HOURS}</li>
     * </ul>
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<TimeDuration> timeDurationPropertySpecWithSmallUnits(String name){
        return simplePropertySpec(name, TimeDuration.class, new TimeDurationValueFactory(true));
    }

    /**
     * Creates a PropertySpec for a {@link TimeDuration} value, taking into account its default value.
     *  Only the small units will be
     * displayed in the editor.
     * <ul>
     *     <li>{@link TimeDuration#MILLISECONDS}</li>
     *     <li>{@link TimeDuration#SECONDS}</li>
     *     <li>{@link TimeDuration#MINUTES}</li>
     *     <li>{@link TimeDuration#HOURS}</li>
     * </ul>
     *
     * @param name The name of the PropertySpec
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public static PropertySpec<TimeDuration> timeDurationPropertySpecWithSmallUnitsAndDefaultValue(String name, TimeDuration defaultValue) {
        return PropertySpecBuilder.
                forClass(TimeDuration.class, new TimeDurationValueFactory(true)).
                name(name).
                setDefaultValue(defaultValue).
                finish();
    }

    /**
     * Creates a PropertySpec for a {@link TimeDuration} value which only allows the given values
     * Only the small units will be
     * displayed in the editor.
     * <ul>
     *     <li>{@link TimeDuration#MILLISECONDS}</li>
     *     <li>{@link TimeDuration#SECONDS}</li>
     *     <li>{@link TimeDuration#MINUTES}</li>
     *     <li>{@link TimeDuration#HOURS}</li>
     * </ul>
     *
     * @param name   The name of the PropertySpec
     * @param values the allowed values for this TimeDurationPropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<TimeDuration> timeDurationPropertySpecWithSmallUnitsWithSpecificValue(String name, TimeDuration... values) {
        return PropertySpecBuilder.
                forClass(TimeDuration.class, new TimeDurationValueFactory(true)).
                name(name).addValues(values).
                finish();
    }

    /**
     * Creates a PropertySpec for an {@link ObisCode} value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<ObisCode> obisCodePropertySpec(String name) {
        return simplePropertySpec(name, ObisCode.class, new ObisCodeValueFactory());
    }

    /**
     * Creates a PropertySpec for an {@link ObisCode} value which only allows the given values
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<ObisCode> obisCodePropertySpecWithValues(String name, ObisCode... values) {
        return PropertySpecBuilder.
                forClass(ObisCode.class, new ObisCodeValueFactory()).
                name(name).addValues(values).
                finish();
    }

    /**
     * Creates a PropertySpec for a {@link Code codeTable} value.
     *
     * @param name The name of the PropertySpec
     * @return the newly created PropertySpec
     */
    public static PropertySpec<Code> codeTableReferencePropertySpec(String name) {
        return PropertySpecBuilder.
                forReference(Code.class, new ValueDomain(FactoryIds.CODE.id(), 0), AttributeValueSelectionMode.SEARCH_AND_SELECT).
                name(name).finish();
    }

    /**
     * Creates a PropertySpec for a {@link UserFile userFile} value.
     *
     * @param name the name of the PropertySpec
     * @return the newly created PropertySpec
     */
    public static PropertySpec<UserFile> userFileReferencePropertySpec(String name) {
        return PropertySpecBuilder.
                forReference(UserFile.class, new ValueDomain(FactoryIds.USERFILE.id(), 0),
                        AttributeValueSelectionMode.SEARCH_AND_SELECT).
                name(name).finish();
    }

    /**
     * Creates a PropertySpec for a {@link Lookup lookUpTable} value.
     *
     * @param name the name for the PropertySpec
     * @return the newly created PropertySpec
     */
    public static PropertySpec<Lookup> lookupPropertySpec(final String name) {
        return PropertySpecBuilder.
                forReference(Lookup.class, new ValueDomain(FactoryIds.LOOKUP.id(), 0),
                        AttributeValueSelectionMode.COMBOBOX).
                name(name).finish();
    }

    /**
     * Creates a PropertySpec for a {@link LoadProfile} value.
     *
     * @param name the name of the PropertySpec
     * @return the newly created PropertySpec
     */
    public static PropertySpec<LoadProfile> loadProfilePropertySpec(final String name) {
        return PropertySpecBuilder.
                forReference(LoadProfile.class, new ValueDomain(FactoryIds.LOADPROFILE.id(), 0),
                        AttributeValueSelectionMode.SEARCH_AND_SELECT).
                name(name).finish();
    }

    /**
     * Creates a PropertySpec for a {@link Channel} value.
     *
     * @param name the name of the PropertySpec
     * @return the newly created PropertySpec
     */
    public static PropertySpec<Channel> channelPropertySpec(final String name) {
        return PropertySpecBuilder.
                forReference(Channel.class, new ValueDomain(FactoryIds.CHANNEL.id(), 0),
                        AttributeValueSelectionMode.SEARCH_AND_SELECT).
                name(name).finish();
    }

    /**
     * Creates a PropertySpec for a Ean13 value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<Ean13> ean13PropertySpec(String name) {
        return simplePropertySpec(name, Ean13.class, new Ean13Factory());
    }

    /**
     * Creates a PropertySpec for a Ean18 value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<Ean18> ean18PropertySpec(String name) {
        return simplePropertySpec(name, Ean18.class, new Ean18Factory());
    }

    private static <T> PropertySpec<T> simplePropertySpec(String name, Class<T> domainClass, ValueFactory<T> valueFactory) {
        return PropertySpecBuilder.
                forClass(domainClass, valueFactory).
                name(name).
                finish();
    }

    // for legacy conversion

    /**
     * Converts a list with string keys to the new List<PropertySpec> format.
     *
     * @param keys The list of keys
     * @return The list of PropertySpecs
     */
    public static List<PropertySpec> toPropertySpecs(List<String> keys) {
        List<PropertySpec> result = new ArrayList<PropertySpec>();
        for (String key : keys) {
            result.add(PropertySpecFactory.stringPropertySpec(key));
        }
        return result;
    }

    // Hide utility class constructor
    private PropertySpecFactory() {
    }

}