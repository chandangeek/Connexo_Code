package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.ftpclient.FtpClientService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.util.Map;

public class CertificateSftpExporterDestination implements CertificateExportDestination {

    private final FtpClientService ftpClientService;
    private final Clock clock;
    private final Map<String, Object> properties;

    public CertificateSftpExporterDestination(FtpClientService ftpClientService, Clock clock, Map<String, Object> properties) {
        this.ftpClientService = ftpClientService;
        this.clock = clock;
        this.properties = properties;
    }

    public void export(byte[] bytes, byte[] signature) throws IOException {
        String host = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_HOSTNAME.getPropertyKey());
        int port = ((Long) properties.get(CSRImporterTranslatedProperty.EXPORT_PORT.getPropertyKey())).intValue();
        String username = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_USER.getPropertyKey());
        String password = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_PASSWORD.getPropertyKey());
        String filename = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_FILE_NAME.getPropertyKey());
        String directory = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_FILE_LOCATION.getPropertyKey());
        String extension = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_FILE_EXTENSION.getPropertyKey());
        String path = new CertificateExportTagReplacer(clock).replaceTags(directory.replaceAll("/$", "") + "/" + filename + "." + extension);
        ftpClientService.getSftpFactory(host, port, username, password).runInSession(fileSystem -> {
            Path file = fileSystem.getPath(path);
            Files.write(file, bytes);
            Files.write(file, signature, StandardOpenOption.APPEND);
        });
    }
}
