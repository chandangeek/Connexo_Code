/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.rest.impl;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Created by bvn on 5/22/17.
 */
public class CertificateInfoFactoryTest {
    @Test
    public void testRenderSubject() throws Exception {
        CertificateInfoFactory certificateInfoFactory = new CertificateInfoFactory(null, null);
        String name = certificateInfoFactory.x500FormattedName("CN=Matthieu Deroo, OU=Software solutions, L=Kortrijk, ST=West-Vlaanderen, C=Belgium, O=Honeywell");
        assertThat(name).isEqualTo("CN=Matthieu Deroo, OU=Software solutions, O=Honeywell, L=Kortrijk, ST=West-Vlaanderen, C=Belgium");
    }
}


