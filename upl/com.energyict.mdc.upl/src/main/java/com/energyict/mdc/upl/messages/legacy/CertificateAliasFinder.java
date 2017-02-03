/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.security.CertificateAlias;

public interface CertificateAliasFinder {

    CertificateAlias from(String alias);

    CertificateAlias newFrom(String alias, String encodedCertificate);

}