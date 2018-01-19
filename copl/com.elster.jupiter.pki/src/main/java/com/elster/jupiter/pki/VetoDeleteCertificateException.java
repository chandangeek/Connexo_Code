/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.impl.MessageSeeds;

import aQute.bnd.annotation.ProviderType;

/**
 * Created by bvn on 3/30/17.
 */
@ProviderType
public class VetoDeleteCertificateException extends LocalizedException {
    public VetoDeleteCertificateException(Thesaurus thesaurus, CertificateWrapper certificateWrapper) {
        super(thesaurus, MessageSeeds.VETO_CERTIFICATE_DELETION, certificateWrapper.getAlias());
    }
}
