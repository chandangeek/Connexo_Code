/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.energyict.mdc.common.device.data.SecurityAccessor;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface SecurityAccessorDAO {

    Optional<SecurityAccessor> findBy(SymmetricKeyWrapper key);

    Optional<SecurityAccessor> findBy(CertificateWrapper certificateWrapper);
}
