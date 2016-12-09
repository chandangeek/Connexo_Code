package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.upl.nls.NlsMessageFormat;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.nls.TranslationKey;

/**
 * Adapter between the Connexo {@link com.elster.jupiter.nls.Thesaurus}
 * and the {@link Thesaurus} from the universal protocol layer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (13:11)
 */
public class UPLThesaurusAdapter implements Thesaurus {
    private final com.elster.jupiter.nls.Thesaurus actual;

    public UPLThesaurusAdapter(com.elster.jupiter.nls.Thesaurus actual) {
        this.actual = actual;
    }

    @Override
    public NlsMessageFormat getFormat(TranslationKey key) {
        return new UPLNlsMessageFormatAdapter(this.actual.getFormat(new ConnexoTranslationKeyAdapter(key)));
    }

    private static class UPLNlsMessageFormatAdapter implements NlsMessageFormat {
        private final com.elster.jupiter.nls.NlsMessageFormat actual;

        private UPLNlsMessageFormatAdapter(com.elster.jupiter.nls.NlsMessageFormat actual) {
            this.actual = actual;
        }

        @Override
        public String format(Object... parameters) {
            return this.actual.format(parameters);
        }
    }

}