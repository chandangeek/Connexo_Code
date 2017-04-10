/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateWrapper;

/**
 * Created by bvn on 3/30/17.
 */
public class VetoDeleteCertificateException extends LocalizedException {
    public VetoDeleteCertificateException(Thesaurus thesaurus, CertificateWrapper certificateWrapper) {
        super(thesaurus, MessageSeeds.VETO_CERTIFICATE_DELETION, certificateWrapper.getAlias());
    }
}
