package com.energyict.mdc.upl.nls;


/**
 * Supports the translation of a {@link TranslationKey}
 * with the injection of parameter values.
 * Like with {@link java.text.MessageFormat}, the translation
 * of the key is expected to contain "{n}" where a parameter
 * is expected to be injected.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-21 (08:55)
 */
public interface NlsMessageFormat {

    /**
     * Formats the default translation, replacing
     * each "{n}" with the corresponding parameter.
     *
     * @param parameters The parameters
     * @return The complete translation
     */
    String format(Object... parameters);

}