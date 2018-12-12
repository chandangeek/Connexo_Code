/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnexoThesaurusAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnexoTranslationKeyAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLNlsMessageFormatAdapter;
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

    public static Thesaurus adaptTo(com.elster.jupiter.nls.Thesaurus actual) {
        if (actual instanceof ConnexoThesaurusAdapter) {
            return ((ConnexoThesaurusAdapter) actual).getUplThesaurus();
        } else {
            return new UPLThesaurusAdapter(actual);
        }
    }

    private UPLThesaurusAdapter(com.elster.jupiter.nls.Thesaurus actual) {
        this.actual = actual;
    }

    public com.elster.jupiter.nls.Thesaurus getConnexoThesaurus() {
        return actual;
    }

    @Override
    public NlsMessageFormat getFormat(TranslationKey key) {
        return UPLNlsMessageFormatAdapter.adaptTo(this.actual.getFormat(ConnexoTranslationKeyAdapter.adaptTo(key)));
    }

    @Override
    public int hashCode() {
        return actual != null ? actual.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UPLThesaurusAdapter) {
            return actual.equals(((UPLThesaurusAdapter) o).actual);
        } else {
            return actual.equals(o);
        }
    }
}