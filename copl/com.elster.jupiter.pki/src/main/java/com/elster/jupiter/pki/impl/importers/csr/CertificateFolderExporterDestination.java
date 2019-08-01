package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.ftpclient.FtpClientService;

import java.io.IOException;
import java.nio.file.*;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;

public class CertificateFolderExporterDestination implements CertificateExportDestination {
    private final Clock clock;
    private final String filename, directory, extension;
    private String lastFileName;

    public CertificateFolderExporterDestination(Clock clock, Map<String, Object> properties) {
        this.clock = clock;
        filename = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_FILE_NAME.getPropertyKey());
        directory = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_FILE_LOCATION.getPropertyKey());
        extension = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_FILE_EXTENSION.getPropertyKey());
    }

    @Override
    public void export(byte[] bytes, byte[] signature) throws IOException {
        String path = new CertificateExportTagReplacer(clock).replaceTags(directory.replaceAll("/$", "") + "/" + filename + '.' + extension);

        FileSystem fileSystem = FileSystems.getDefault();
        Path file = fileSystem.getPath(path);

        Path directory = file.getParent();
        if (Files.notExists(directory)){
            Files.createDirectories(directory);
        }
        Path newFile = Files.createFile(file);
        Files.write(newFile, bytes);
        Files.write(newFile, signature, StandardOpenOption.APPEND);
        lastFileName = newFile.getFileName().toString();
    }

    @Override
    public Optional<String> getLastExportedFileName() {
        return Optional.ofNullable(lastFileName);
    }

    @Override
    public String getUrl() {
        return  lastFileName;
    }
}
