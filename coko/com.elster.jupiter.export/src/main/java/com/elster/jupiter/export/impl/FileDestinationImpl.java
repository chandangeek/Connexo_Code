package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;
import java.util.logging.Logger;

@Sandboxed(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PARENT_BREAKING_PATH + "}")
class FileDestinationImpl extends AbstractDataExportDestination implements FileDestination {
    private static final String NON_PATH_INVALID = "\":*?<>|";
    private static final String PATH_INVALID = "\"*?<>|";

    private class Sender {
        private final TagReplacerFactory tagReplacerFactory;
        private final Logger logger;
        private final Thesaurus thesaurus;

        private Sender(TagReplacerFactory tagReplacerFactory, Logger logger, Thesaurus thesaurus) {
            this.tagReplacerFactory = tagReplacerFactory;
            this.logger = logger;
            this.thesaurus = thesaurus;
        }

        private void send(Map<StructureMarker, Path> files) {
            files.forEach(this::copyFile);
        }

        private void copyFile(StructureMarker structureMarker, Path path) {
            doCopy(path, determineTargetFile(structureMarker));
        }

        private Path determineTargetFile(StructureMarker structureMarker) {
            TagReplacer tagReplacer = tagReplacerFactory.forMarker(structureMarker);
            String fileLocationWithTags = tagReplacer.replaceTags(fileLocation);
            Path targetDirectory = getTargetDirectory(fileLocationWithTags);
            String fileNameWithTags = tagReplacer.replaceTags(fileName) + '.' + fileExtension;
            if (!Files.exists(targetDirectory)) {
                try {
                    targetDirectory = Files.createDirectories(targetDirectory);
                } catch (IOException e) {
                    throw new DestinationFailedException(
                            thesaurus, MessageSeeds.FILE_DESTINATION_FAILED, e, fileLocationWithTags + "/" + fileNameWithTags, e.getMessage());
                }
            }
            return targetDirectory.resolve(fileNameWithTags);
        }

        private void doCopy(Path source, Path target) {
            try {
                Files.copy(source, target);
            } catch (Exception e) {
                throw new DestinationFailedException(
                        thesaurus, MessageSeeds.FILE_DESTINATION_FAILED, e, target.toAbsolutePath().toString(), e.toString() + " " + e.getMessage());
            }
            try (TransactionContext context = getTransactionService().getContext()) {
                    MessageSeeds.DATA_EXPORTED_TO.log(logger, thesaurus, target.toAbsolutePath().toString());
                context.commit();
            }
        }

        private Path getTargetDirectory(String fileLocation) {
            return getDefaultExportDir().resolve(fileLocation);
        }

        private Path getDefaultExportDir() {
            AppServer appServer = appService.getAppServer().orElseThrow(IllegalStateException::new);
            return getDataExportService().getExportDirectory(appServer).orElseGet(() -> getFileSystem().getPath("").toAbsolutePath());
        }

    }

    private final AppService appService;

    @ValidFileName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALIDCHARS_EXCEPTION + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_MIN_AND_MAX + "}")
    private String fileName;
    @ValidFileName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALIDCHARS_EXCEPTION + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_MIN_AND_MAX + "}")
    private String fileExtension;
    @ValidFileLocation(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALIDCHARS_EXCEPTION + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_MIN_AND_MAX + "}")
    private String fileLocation;

    @Inject
    FileDestinationImpl(DataModel dataModel, Clock clock, Thesaurus thesaurus, DataExportService dataExportService, AppService appService, FileSystem fileSystem, TransactionService transactionService) {
        super(dataModel, clock, thesaurus, dataExportService, fileSystem, transactionService);
        this.appService = appService;
    }

    static FileDestinationImpl from(IExportTask task, DataModel dataModel, String fileLocation, String fileName, String fileExtension) {
        return dataModel.getInstance(FileDestinationImpl.class).init(task, fileLocation, fileName, fileExtension);
    }

    @Override
    public void send(Map<StructureMarker, Path> files, TagReplacerFactory tagReplacerFactory, Logger logger, Thesaurus thesaurus) {
        new Sender(tagReplacerFactory, logger, thesaurus).send(files);
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

    FileDestinationImpl init(IExportTask task, String fileLocation, String fileName, String fileExtension) {
        initTask(task);
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileLocation = fileLocation;
        return this;
    }

}
