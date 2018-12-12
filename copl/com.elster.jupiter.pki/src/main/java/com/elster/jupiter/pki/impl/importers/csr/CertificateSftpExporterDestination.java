package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.ftpclient.FtpClientService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;

public class CertificateSftpExporterDestination implements CertificateExportDestination {
    private final FtpClientService ftpClientService;
    private final Clock clock;
    private final String host, username, password, filename, directory, extension;
    private final int port;
    private String lastFileName;

    public CertificateSftpExporterDestination(FtpClientService ftpClientService, Clock clock, Map<String, Object> properties) {
        this.ftpClientService = ftpClientService;
        this.clock = clock;
        host = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_HOSTNAME.getPropertyKey());
        port = ((Long) properties.get(CSRImporterTranslatedProperty.EXPORT_PORT.getPropertyKey())).intValue();
        username = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_USER.getPropertyKey());
        password = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_PASSWORD.getPropertyKey());
        filename = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_FILE_NAME.getPropertyKey());
        directory = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_FILE_LOCATION.getPropertyKey());
        extension = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_FILE_EXTENSION.getPropertyKey());
    }

    @Override
    public void export(byte[] bytes, byte[] signature) throws IOException {
        String path = new CertificateExportTagReplacer(clock).replaceTags(directory.replaceAll("/$", "") + "/" + filename + '.' + extension);
        ftpClientService.getSftpFactory(host, port, username, password).runInSession(fileSystem -> {
            Path file = fileSystem.getPath(path);
            Files.write(file, bytes);
            Files.write(file, signature, StandardOpenOption.APPEND);
            lastFileName = file.getFileName().toString();
        });
    }

    @Override
    public Optional<String> getLastExportedFileName() {
        return Optional.ofNullable(lastFileName);
    }

    public String getProtocol() {
        return "sftp";
    }

    @Override
    public String getUrl() {
        return getProtocol() + "://" + username + '@' + host + ':' + port;
    }
}
