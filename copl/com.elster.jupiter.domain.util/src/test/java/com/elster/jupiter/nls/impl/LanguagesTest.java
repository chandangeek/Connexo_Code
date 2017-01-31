/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LanguagesTest {

    @Mock
    private NlsServiceImpl nlsService;
    @Mock
    private BundleContext bundleContext;
    private FileSystem fileSystem;
    @Mock
    private DataModel dataModel;

    @Before
    public void setUp() {
        when(nlsService.getBundleContext()).thenReturn(bundleContext);
        when(bundleContext.getProperty("com.elster.jupiter.nls.csv.separator")).thenReturn(";");
        when(bundleContext.getProperty("com.elster.jupiter.nls.install")).thenReturn("de");
        when(bundleContext.getProperty("com.elster.jupiter.nls.config.directory")).thenReturn("c:/languages");

        fileSystem = Jimfs.newFileSystem(Configuration.windows());

        when(nlsService.getFileSystem()).thenReturn(fileSystem);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testReadUtf8File() throws IOException {

        Path path = fileSystem.getPath("c:/languages/pulse_de.csv");
        Files.createDirectories(path.getParent());
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(path, StandardOpenOption.CREATE_NEW), Charset.forName("UTF-8"))) {
            writer.write("MTR;REST;readingtypesmanagment.addReadingType.addConfirmationXXX[1];{0} Ablesetyp hinzufügen?;\n"+
                    "MTR;REST;readingTypes.attribute.currency;Währung;\n");
        }

        Languages languages = Languages.withSettingsOf(nlsService);

        NlsKeyImpl nlsKey = new NlsKeyImpl(dataModel).init("MTR", Layer.REST, "readingtypesmanagment.addReadingType.addConfirmationXXX[1]");

        languages.addTranslationsTo(nlsKey);

        Optional<String> translation = nlsKey.translate(Locale.GERMAN);

        assertThat(translation).contains("{0} Ablesetyp hinzufügen?");
    }

}