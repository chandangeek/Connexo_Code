/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.util.streams.ReusableInputStream;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.FileInputStream;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CSRZipFileParserTest {

    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() throws Exception {
        when(nlsService.getThesaurus(CaService.COMPONENTNAME, Layer.DOMAIN)).thenReturn(thesaurus);
    }

    @Test
    public void testParserSuccessful() throws Exception {
        ReusableInputStream reusableInputStream = ReusableInputStream.from(new FileInputStream(getClass().getClassLoader().getResource("testCSR.zip").getPath()));

        Map<String, Map<String, PKCS10CertificationRequest>> csrMap = new CSRZipFileParser(thesaurus)
                .parseInputStream(reusableInputStream.stream());
    }
}