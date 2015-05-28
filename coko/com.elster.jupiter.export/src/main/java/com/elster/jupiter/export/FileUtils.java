package com.elster.jupiter.export;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.fileimport.FileIOException;
import com.elster.jupiter.nls.Thesaurus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Created by igh on 28/05/2015.
 */
public class FileUtils {

    private final Thesaurus thesaurus;
    private final DataExportService dataExportService;
    private final AppService appService;

    private final FileSystem fileSystem;

    public FileUtils(FileSystem fileSystem, Thesaurus thesaurus, DataExportService dataExportService, AppService appService) {
        this.fileSystem = fileSystem;
        this.thesaurus = thesaurus;
        this.dataExportService = dataExportService;
        this.appService = appService;
    }

    public Path createTemporaryFile(List<FormattedExportData> data, String fileName, String fileExtension) {
        try {
            String tempFileName = new StringBuilder(fileName).append('.').append(fileExtension).toString();
            Path tempFile = Files.createTempFile(getTempDir(), tempFileName, fileExtension);
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
                for (FormattedExportData dataPart : data) {
                    writer.write(dataPart.getAppendablePayload());
                }
            }
            return tempFile;
        } catch (IOException ex) {
            throw new FatalDataExportException(new FileIOException(ex, thesaurus));
        }
    }

    public Path createTemporaryFile(List<FormattedExportData> data) {
        return createTemporaryFile(data, "tempfile", "tmp");
    }

    public Path createFile(List<FormattedExportData> data, String fileName, String fileExtension, String fileLocation) {
        try {
            Path tempFile = createTemporaryFile(data);
            Path file = ensuringDirectoryExists(createFile(fileName, fileExtension, fileLocation));
            Files.copy(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
            return file;
        } catch (IOException ex) {
            throw new FatalDataExportException(new FileIOException(ex, thesaurus));
        }
    }

    private Path createFile(String fileName, String fileExtension, String fileLocation) {
        fileName = new StringBuilder(fileName).append('.').append(fileExtension).toString();
        Path path = this.fileSystem.getPath(fileLocation);
        if (path.isAbsolute()) {
            return path.resolve(fileName);
        }
        return getDefaultExportDir().resolve(path).resolve(fileName);
    }

    private Path ensuringDirectoryExists(Path path) {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new FatalDataExportException(new FileIOException(e, thesaurus));
        }
        return path;
    }


    private Path getTempDir() {
        return FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"));
    }

    private Path getDefaultExportDir() {
        AppServer appServer = appService.getAppServer().orElseThrow(IllegalStateException::new);
        return dataExportService.getExportDirectory(appServer).orElseGet(() -> this.fileSystem.getPath("").toAbsolutePath());
    }


}
