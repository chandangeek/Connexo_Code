package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.DataModelImpl;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.ImmutableMap;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileDestinationTest {

    public static final String DATA1 = "line 1";
    public static final String DATA2 = "line 2";
    public static final String DATA3 = "line 3";
    public static final String DATA4 = "line 4";
    public static final String APPSERVER_PATH = "/appserver/export";
    public static final String FILENAME = "filename";
    public static final String EXTENSION = "txt";
    public static final String ABSOLUTE_DIR = "/export";
    public static final String RELATIVE_DIR = "datadir";

    private Clock clock = Clock.systemDefaultZone();
    private TagReplacerFactory tagReplacerFactory = new TagReplacerFactory() {
        @Override
        public TagReplacer forMarker(StructureMarker structureMarker) {
            return TagReplacerImpl.asTagReplacer(clock, structureMarker, 17);
        }
    };

    @Mock
    private AppService appService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private AppServer appServer;
    @Mock
    private DataExportService dataExportService;
    @Mock
    DataModel dataModel;
    @Mock
    Logger logger;
    @Mock
    private TransactionService transactionService;
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();


    private FileSystem fileSystem;
    private Path file1, file2;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
        when(dataExportService.getExportDirectory(appServer)).thenReturn(Optional.of(fileSystem.getPath(APPSERVER_PATH)));

        file1 = fileSystem.getPath("/a.tmp");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file1, StandardOpenOption.CREATE_NEW)))) {
            writer.write(DATA1);
            writer.write(DATA2);
        }
        file2 = fileSystem.getPath("/b.tmp");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file2, StandardOpenOption.CREATE_NEW)))) {
            writer.write(DATA3);
            writer.write(DATA4);
        }
        MessageInterpolator messageInterpolator = getMessageInterpolator();
        ValidatorFactory validatorFactory = Validation.byDefaultProvider()
                .configure()
                .constraintValidatorFactory(getConstraintValidatorFactory())
                .messageInterpolator(messageInterpolator)
                .buildValidatorFactory();

        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
    }

    @Test
    public void testExportToCsvWithAbsolutePath() {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService,fileSystem, transactionService);
        fileDestination.init(null, ABSOLUTE_DIR, FILENAME, EXTENSION);
        fileDestination.send(ImmutableMap.of(DefaultStructureMarker.createRoot(clock, "root"), file1), tagReplacerFactory, logger, thesaurus);
        Path file = fileSystem.getPath(ABSOLUTE_DIR, FILENAME + "." + EXTENSION);
        assertThat(Files.exists(file)).isTrue();
        assertThat(getContent(file)).isEqualTo(DATA1 + DATA2);
    }

    @Test
    public void testExportToCsvWithRelativePath() {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, transactionService);
        fileDestination.init(null, RELATIVE_DIR, FILENAME, EXTENSION);
        fileDestination.send(ImmutableMap.of(DefaultStructureMarker.createRoot(clock, "root"), file1), tagReplacerFactory, logger, thesaurus);
        Path file = fileSystem.getPath(APPSERVER_PATH, RELATIVE_DIR, FILENAME + "." + EXTENSION);
        assertThat(Files.exists(file)).isTrue();
        assertThat(getContent(file)).isEqualTo(DATA1 + DATA2);
    }

    private String getContent(Path file) {
        try {
            StringBuffer content = new StringBuffer();
            List<String> lines = Files.readAllLines(file);
            for (String line : lines) {
                content.append(line);
            }
            return content.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreateDestinationWithValidPath() throws Exception {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, transactionService);
        fileDestination.init(null, "A/C", FILENAME, EXTENSION);
        fileDestination.save();
    }

    @Test
    public void testCreateDestinationWithValidFileExtension() throws Exception {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, transactionService);
        fileDestination.init(null, RELATIVE_DIR, FILENAME, "EXE");
        fileDestination.save();
    }



    @Test
    @ExpectedConstraintViolation(property = "fileLocation", messageId="{InvalidChars}")
    public void testCreateDestinationWithInvalidPath() throws Exception {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, transactionService);
        fileDestination.init(null, "A < C", FILENAME, EXTENSION);
        fileDestination.save();
    }

    @Test
    @ExpectedConstraintViolation(property = "fileName", messageId="{InvalidChars}")
    public void testCreateDestinationWithInvalidFileNameQuote() throws Exception {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, transactionService);
        fileDestination.init(null, RELATIVE_DIR, "A\"A", EXTENSION);
        fileDestination.save();
    }

    @Test
    @ExpectedConstraintViolation(property = "fileExtension", messageId="{InvalidChars}", strict=false)
    public void testCreateDestinationWithInvalidFileExtensionAstrix() throws Exception {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, transactionService);
        fileDestination.init(null, RELATIVE_DIR, FILENAME, "EX*E");
        fileDestination.save();
    }

    @Test
    @ExpectedConstraintViolation(property = "fileExtension", messageId="{InvalidChars}",strict=false)
    public void testCreateDestinationWithInvalidFileExtensionQuotedAstrix() throws Exception {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, transactionService);
        fileDestination.init(null, RELATIVE_DIR, FILENAME, "EX\\*E");
        fileDestination.save();
    }

    @Test
    @ExpectedConstraintViolation(property = "fileExtension", messageId="{InvalidChars}")
    public void testCreateDestinationWithInvalidFileExtensionQuote() throws Exception {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, transactionService);
        fileDestination.init(null, RELATIVE_DIR, FILENAME, "E\"XE");
        fileDestination.save();
    }

    @Test
    @ExpectedConstraintViolation(property = "fileExtension", messageId="{InvalidChars}")
    public void testCreateDestinationWithInvalidFileExtensionColon() throws Exception {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem,transactionService);
        fileDestination.init(null, RELATIVE_DIR, FILENAME, "E:XE");
        fileDestination.save();
    }

    @Test
    @ExpectedConstraintViolation(property = "fileExtension", messageId="{InvalidChars}")
    public void testCreateDestinationWithInvalidFileExtensionSmaller() throws Exception {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, transactionService);
        fileDestination.init(null, RELATIVE_DIR, FILENAME, "E<XE");
        fileDestination.save();
    }

    @Test
    @ExpectedConstraintViolation(property = "fileExtension", messageId="{InvalidChars}")
    public void testCreateDestinationWithInvalidFileExtensionLarger() throws Exception {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, transactionService);
        fileDestination.init(null, RELATIVE_DIR, FILENAME, "E>XE");
        fileDestination.save();
    }

    @Test
    @ExpectedConstraintViolation(property = "fileExtension", messageId="{FieldSizeBetweenMinAndMax}")
    public void testCreateDestinationWithInvalidFileExtensionTooLong() throws Exception {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, transactionService);
        fileDestination.init(null, RELATIVE_DIR, FILENAME, "EXE98765432109876543210987654321098765432109876543210987654321098765432109876543210");
        fileDestination.save();
    }

    @Test
    public void testMultiple() {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, transactionService);
        fileDestination.setFileName("export<identifier>");
        fileDestination.setFileExtension("txt");
        fileDestination.setFileLocation("a/b");

        StructureMarker marker1 = DefaultStructureMarker.createRoot(clock, "file1");
        StructureMarker marker2 = DefaultStructureMarker.createRoot(clock, "file2");

        fileDestination.send(ImmutableMap.of(marker1, file1, marker2, file2), tagReplacerFactory, logger, thesaurus);

        assertThat(fileSystem.getPath("/appserver/export/a/b/exportfile1.txt")).exists();
    }

    private ConstraintValidatorFactory getConstraintValidatorFactory() {
        return new ConstraintValidatorFactory() {

            @Override
            public void releaseInstance(ConstraintValidator<?, ?> arg0) {
            }

            @Override
            public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> arg0) {
                try {
                    return arg0.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            };
        };
    }

    private MessageInterpolator getMessageInterpolator() {
        return new MessageInterpolator() {
            @Override
            public String interpolate(String messageTemplate, Context context) {
                return messageTemplate;
            }

            @Override
            public String interpolate(String messageTemplate, Context context, Locale locale) {
                return messageTemplate;
            }
        };
    }

}

