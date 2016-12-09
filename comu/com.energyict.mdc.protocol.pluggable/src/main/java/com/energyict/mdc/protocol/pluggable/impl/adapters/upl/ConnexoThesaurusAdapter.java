package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.upl.nls.Thesaurus;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter between the {@link Thesaurus} from the universal protocol layer
 * to the Connexo {@link com.elster.jupiter.nls.Thesaurus}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (11:02)
 */
class ConnexoThesaurusAdapter implements com.elster.jupiter.nls.Thesaurus {
    private final Thesaurus actual;

    ConnexoThesaurusAdapter(Thesaurus actual) {
        this.actual = actual;
    }

    @Override
    public NlsMessageFormat getFormat(com.elster.jupiter.nls.TranslationKey key) {
        return new ConnexoNlsMessageFormatAdapter(this.actual.getFormat(new UPLTranslationKeyAdapter(key)));
    }

    @Override
    public String getString(String key, String defaultMessage) {
        throw new UnsupportedOperationException("Adapter between universal protocol thesaurus and Connexo thesaurus does not support getString(String, String) because protocols are not aware of this method and should therefore not be capable of calling it.");
    }

    @Override
    public String getString(Locale locale, String key, String defaultMessage) {
        throw new UnsupportedOperationException("Adapter between universal protocol thesaurus and Connexo thesaurus does not support getString(String, String) because protocols are not aware of this method and should therefore not be capable of calling it.");
    }

    @Override
    public NlsMessageFormat getFormat(MessageSeed seed) {
        throw new UnsupportedOperationException("Adapter between universal protocol thesaurus and Connexo thesaurus does not support getFormat(MessageSeed) because protocols are not aware of this method and should therefore not be capable of calling it.");
    }

    @Override
    public Map<String, String> getTranslationsForCurrentLocale() {
        throw new UnsupportedOperationException("Adapter between universal protocol thesaurus and Connexo thesaurus does not support getTranslationsForCurrentLocale() because protocols are not aware of this method and should therefore not be capable of calling it.");
    }

    @Override
    public boolean hasKey(String key) {
        throw new UnsupportedOperationException("Adapter between universal protocol thesaurus and Connexo thesaurus does not support hasKey(String) because protocols are not aware of this method and should therefore not be capable of calling it.");
    }

    @Override
    public com.elster.jupiter.nls.Thesaurus join(com.elster.jupiter.nls.Thesaurus thesaurus) {
        throw new UnsupportedOperationException("Adapter between universal protocol thesaurus and Connexo thesaurus does not support join(Thesaurus) because protocols are not aware of this method and should therefore not be capable of calling it.");
    }

    @Override
    public DateTimeFormatter forLocale(DateTimeFormatter dateTimeFormatter) {
        throw new UnsupportedOperationException("Adapter between universal protocol thesaurus and Connexo thesaurus does not support forLocale(DateTimeFormatter) because protocols are not aware of this method and should therefore not be capable of calling it.");
    }

    @Override
    public String interpolate(String s, Context context) {
        throw new UnsupportedOperationException("Adapter between universal protocol thesaurus and Connexo thesaurus does not support interpolate(String, Context) because protocols are not aware of this method and should therefore not be capable of calling it.");
    }

    @Override
    public String interpolate(String s, Context context, Locale locale) {
        throw new UnsupportedOperationException("Adapter between universal protocol thesaurus and Connexo thesaurus does not support interpolate(String, Context, Locale) because protocols are not aware of this method and should therefore not be capable of calling it.");
    }
}