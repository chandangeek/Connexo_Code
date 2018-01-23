package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateWrapper;

import java.util.List;

public class VetoCertificateMarkObsoleteException extends LocalizedException {

    public VetoCertificateMarkObsoleteException(Thesaurus thesaurus, CertificateWrapper certificateWrapper, List<String> deviceNames) {
        super(thesaurus, MessageSeeds.VETO_CERTIFICATE_MARK_OBSOLETE, certificateWrapper.getAlias(), deviceNames);
    }

}
