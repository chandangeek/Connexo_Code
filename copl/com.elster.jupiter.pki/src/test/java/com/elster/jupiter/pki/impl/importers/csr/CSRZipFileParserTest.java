/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.fileimport.FileImportOccurrence;
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
    @Mock
    FileImportOccurrence fileImportOccurrence;
    @Mock
    FileImportOccurrence fileImportOccurrenceFlat;

    @Before
    public void setUp() throws Exception {
        when(nlsService.getThesaurus(CaService.COMPONENTNAME, Layer.DOMAIN)).thenReturn(thesaurus);
    }

    @Test
    public void testParserSuccessful() throws Exception {
        int expectedSize = 2;
        String expectedPath1 = "670-001122-1636";
        String expectedPath2 = "670-001123-1637";

        String zipFileName = getClass().getClassLoader().getResource("testCSR.zip").getPath();

        when(fileImportOccurrence.getPath()).thenReturn(zipFileName);

        CSRZipFileParser csrZipFileParser = new CSRZipFileParser(fileImportOccurrence, thesaurus);
        Map<String, Map<String, PKCS10CertificationRequest>> csrMap = csrZipFileParser.parse();

//                new CSRZipFileParser(thesaurus)
//                .parseInputStream(ReusableInputStream.from(new FileInputStream(getClass().getClassLoader().getResource("testCSR.zip").getPath())).stream());

        assertThat(csrMap.size()).isEqualTo(expectedSize);
        assertThat(csrMap.get(expectedPath1)).isNotNull();
        assertThat(csrMap.get(expectedPath2)).isNotNull();
        assertThat(csrMap.get(expectedPath1).size()).isEqualTo(expectedSize);
        assertThat(csrMap.get(expectedPath2).size()).isEqualTo(expectedSize);
    }

    @Test
    public void testParserSuccessfulFlatFile() throws Exception {
        String expectedPath1 = "454C536301234568";
        String expectedPath2 = "454C536301234567";

        String zipFileName = getClass().getClassLoader().getResource("testCSRflat.zip").getPath();

        when(fileImportOccurrenceFlat.getPath()).thenReturn(zipFileName);

        CSRZipFileParser csrZipFileParser = new CSRZipFileParser(fileImportOccurrenceFlat, thesaurus);
        Map<String, Map<String, PKCS10CertificationRequest>> csrMap =  csrZipFileParser.parse();

//                new CSRZipFileParser(thesaurus)
//                .parseInputStream(ReusableInputStream.from(new FileInputStream(getClass().getClassLoader().getResource("testCSRflat.zip").getPath())).stream());

        assertThat(csrMap.size()).isEqualTo(2);
        assertThat(csrMap.get(expectedPath1)).isNotNull();
        assertThat(csrMap.get(expectedPath2)).isNotNull();
        assertThat(csrMap.get(expectedPath1).size()).isEqualTo(2);
        assertThat(csrMap.get(expectedPath2).size()).isEqualTo(2);
    }
}