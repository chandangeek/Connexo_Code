package com.elster.jupiter.export.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.FtpDataExportDestination;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.ftpclient.FtpClientService;
import com.elster.jupiter.ftpclient.FtpSessionFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;

public abstract class AbstractFtpDataExportDestination extends AbstractDataExportDestination implements FtpDataExportDestination {
    private static final String NON_PATH_INVALID = "\":*?<>|";
    private static final String PATH_INVALID = "\"*?<>|";

    private class Sender {
        private final TagReplacerFactory tagReplacerFactory;
        private final FileSystem remoteFileSystem;

        private Sender(TagReplacerFactory tagReplacerFactory, FileSystem remoteFileSystem) {
            this.tagReplacerFactory = tagReplacerFactory;
            this.remoteFileSystem = remoteFileSystem;
        }

        private void send(Map<StructureMarker, Path> files) {
            files.forEach(this::copyFile);
        }

        private void copyFile(StructureMarker structureMarker, Path path) {
            doCopy(path, determineTargetFile(structureMarker, remoteFileSystem));
        }

        private Path determineTargetFile(StructureMarker structureMarker, FileSystem remoteFileSystem) {
            TagReplacer tagReplacer = tagReplacerFactory.forMarker(structureMarker);
            Path targetDirectory = getTargetDirectory(tagReplacer.replaceTags(fileLocation));
            if (!Files.exists(targetDirectory)) {
                try {
                    targetDirectory = Files.createDirectories(targetDirectory);
                } catch (IOException e) {
                    throw new FileIOException(getThesaurus(), targetDirectory, e);
                }
            }
            return targetDirectory.resolve(tagReplacer.replaceTags(fileName) + '.' + fileExtension);
        }

        private void doCopy(Path source, Path target) {
            try {
                Files.copy(source, target);
            } catch (IOException e) {
                throw new FileIOException(getThesaurus(), target, e);
            }
        }

        private Path getTargetDirectory(String fileLocation) {
            return remoteFileSystem.getPath("/").resolve(fileLocation);
        }

    }

    private String server;
    private int port = 21;
    private String user;
    private String password;
    @ValidFileName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALIDCHARS_EXCEPTION + "}")
    private String fileName;
    @ValidFileName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALIDCHARS_EXCEPTION + "}")
    private String fileExtension;
    @ValidFileLocation(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALIDCHARS_EXCEPTION + "}")
    private String fileLocation;

    private final DataVaultService dataVaultService;
    private final FtpClientService ftpClientService;

    abstract FtpSessionFactory getFtpSessionFactory();

    void fill(IExportTask task, String server, int port, String user, String password, String fileLocation, String fileName, String fileExtension) {
        initTask(task);
        setServer(server);
        setPort(port);
        setUser(user);
        setPassword(password);
        setFileName(fileName);
        setFileExtension(fileExtension);
        setFileLocation(fileLocation);
    }


    @Inject
    AbstractFtpDataExportDestination(DataModel dataModel, Clock clock, Thesaurus thesaurus, DataExportService dataExportService, FileSystem fileSystem, DataVaultService dataVaultService, FtpClientService ftpClientService) {
        super(dataModel, clock, thesaurus, dataExportService, fileSystem);
        this.dataVaultService = dataVaultService;
        this.ftpClientService = ftpClientService;
    }

    FtpClientService getFtpClientService() {
        return this.ftpClientService;
    }

    @Override
    public void send(Map<StructureMarker, Path> files, TagReplacerFactory tagReplacerFactory) {
        try {
            getFtpSessionFactory().runInSession(
                remoteFileSystem -> {
                    new AbstractFtpDataExportDestination.Sender(tagReplacerFactory, remoteFileSystem).send(files);
                }
            );
        } catch (IOException e) {
            throw new FtpIOException(getThesaurus(), getServer(), getPort(), e);
        }
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String getFileLocation() {
        return fileLocation;
    }

    @Override
    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    @Override
    public String getServer() {
        return server;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return new String(dataVaultService.decrypt(password));
    }

    @Override
    public void setServer(String server) {
        this.server = server;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public void setPassword(String password) {
        this.password = dataVaultService.encrypt(password.getBytes());
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }
}
