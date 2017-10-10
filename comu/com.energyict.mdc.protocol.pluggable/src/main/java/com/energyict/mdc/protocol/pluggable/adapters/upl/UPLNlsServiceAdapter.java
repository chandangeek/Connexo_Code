/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.elster.jupiter.nls.Layer;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;

/**
 * Adapter between the Connexo {@link com.elster.jupiter.nls.NlsService}
 * and the {@link NlsService} from the universal protocol layer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (13:11)
 */
public class UPLNlsServiceAdapter implements NlsService {

    private final com.elster.jupiter.nls.NlsService actual;

    public static NlsService adaptTo(com.elster.jupiter.nls.NlsService actual) {
        return new UPLNlsServiceAdapter(actual);
    }

    private UPLNlsServiceAdapter(com.elster.jupiter.nls.NlsService actual) {
        this.actual = actual;
    }

    @Override
    public Thesaurus getThesaurus(String id) {
        return UPLThesaurusAdapter.adaptTo(this.actual.getThesaurus(id, Layer.DOMAIN));
    }

    @Override
    public int hashCode() {
        return actual != null ? actual.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UPLNlsServiceAdapter) {
            return actual.equals(((UPLNlsServiceAdapter) o).actual);
        } else {
            return actual.equals(o);
        }
    }
}