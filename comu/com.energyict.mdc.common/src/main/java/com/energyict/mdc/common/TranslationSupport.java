package com.energyict.mdc.common;

import com.elster.jupiter.util.Checks;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-24 (08:42)
 */
public enum TranslationSupport {

    ;

    public static String translateEnum(Enum value, boolean classNameAsPrefix) {
        if (value != null) {
            return localizedName(value, classNameAsPrefix);
        }
        else {
            return null;
        }
    }

    private static String localizedName (Enum value, boolean classNameAsPrefix) {
        if (value instanceof LocalizableEnum) {
            LocalizableEnum localizableEnum = (LocalizableEnum) value;
            return localizableEnum.getLocalizedName();
        }
        else {
            return localizedValue(value, classNameAsPrefix);
        }
    }

    public static String localizedValue (Enum value, boolean classNameAsPrefix) {
        String translationKey = getTranslationKey(value, classNameAsPrefix);
        return UserEnvironment.getDefault().getTranslation(translationKey);
    }

    private static String getTranslationKey (Enum value, boolean classNameAsPrefix) {
        String translationKey = value.toString();
        if (classNameAsPrefix){
            String prefix = value.getClass().getSimpleName();
            if (Checks.is(prefix).emptyOrOnlyWhiteSpace()) { // is the case when the Enum class is an anonymous inner class
                prefix = value.getClass().getName();
                // eg. "com.energyict.mdc.tasks.ConnectionTaskFactoryImpl$ConnectionTaskDiscriminator$1"
                // determine the classname as the (last) part between two $ characters:
                prefix = prefix.substring(0, prefix.lastIndexOf('$'));
                prefix = prefix.substring( prefix.lastIndexOf('$')+1 );
            }
            String original = prefix+"."+translationKey;
            translationKey = original.substring(0,1).toLowerCase()+original.substring(1);
        }
        return translationKey;
    }

}