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
import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
        int expectedSize = 2;
        String expectedPath1 = "670-001122-1636";
        String expectedPath2 = "670-001123-1637";

        Map<String, Map<String, PKCS10CertificationRequest>> csrMap = new CSRZipFileParser(thesaurus)
                .parseInputStream(ReusableInputStream.from(new FileInputStream(new URI(getClass().getClassLoader().getResource("testCSR.zip").getFile()).getPath())).stream());

        assertThat(csrMap.size()).isEqualTo(expectedSize);
        assertThat(csrMap.get(expectedPath1)).isNotNull();
        assertThat(csrMap.get(expectedPath2)).isNotNull();
        assertThat(csrMap.get(expectedPath1).size()).isEqualTo(expectedSize);
        assertThat(csrMap.get(expectedPath2).size()).isEqualTo(expectedSize);
    }
}