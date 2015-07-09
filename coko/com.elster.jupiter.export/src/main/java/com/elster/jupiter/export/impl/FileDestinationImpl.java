package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.streams.FancyJoiner;

import javax.inject.Inject;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;
import java.util.regex.Pattern;

class FileDestinationImpl extends AbstractDataExportDestination implements FileDestination {
    private static final String NON_PATH_INVALID = "\":*?<>|";
    private static final String PATH_INVALID = "\"*?<>|";

    private class Sender {
        private final TagReplacerFactory tagReplacerFactory;

        private Sender(TagReplacerFactory tagReplacerFactory) {
            this.tagReplacerFactory = tagReplacerFactory;
        }

        private void send(Map<StructureMarker, Path> files) {
            files.forEach(this::copyFile);
        }
        
        private void copyFile(StructureMarker structureMarker, Path path) {
            doCopy(path, determineTargetFile(structureMarker));
        }

        private Path determineTargetFile(StructureMarker structureMarker) {
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
            return getDefaultExportDir().resolve(fileLocation);
        }

        private Path getDefaultExportDir() {
            AppServer appServer = getAppService().getAppServer().orElseThrow(IllegalStateException::new);
            return getDataExportService().getExportDirectory(appServer).orElseGet(() -> getFileSystem().getPath("").toAbsolutePath());
        }

    }

    private String fileName;
    private String fileExtension;
    private String fileLocation;


    @Inject
    FileDestinationImpl(DataModel dataModel, Clock clock, Thesaurus thesaurus, DataExportService dataExportService, AppService appService, FileSystem fileSystem) {
        super(dataModel, clock, thesaurus, dataExportService, appService, fileSystem);
    }

    static FileDestinationImpl from(IExportTask task, DataModel dataModel, String fileLocation, String fileName, String fileExtension) {
        return dataModel.getInstance(FileDestinationImpl.class).init(task, fileLocation, fileName, fileExtension);
    }

    @Override
    public void send(Map<StructureMarker, Path> files, TagReplacerFactory tagReplacerFactory) {
        new Sender(tagReplacerFactory).send(files);
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

    // TODO add validations
    private void verifySandboxBreaking(String path, String prefix, String extension, String property) {
        String basedir = File.separatorChar + "xyz" + File.separatorChar; // bogus path for validation purposes, not real security
        FilePermission sandbox = new FilePermission(basedir+"-", "write");
        FilePermission request = new FilePermission(basedir + path + File.separatorChar + prefix + "A." + extension, "write");
        if (!sandbox.implies(request)) {
            throw new LocalizedFieldValidationException(MessageSeeds.PARENT_BREAKING_PATH_NOT_ALLOWED, "properties."+ property);
        }
    }

    protected void checkInvalidChars(String value, String fieldName, String invalidCharacters) {
        for (int i = 0; i < invalidCharacters.length(); i++) {
            char invalidChar = invalidCharacters.charAt(i);
            if (value.indexOf(invalidChar) != -1) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALIDCHARS_EXCEPTION, "properties." + fieldName, asString(invalidCharacters));
            }
        }
    }

    FileDestinationImpl init(IExportTask task, String fileLocation, String fileName, String fileExtension) {
        initTask(task);
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileLocation = fileLocation;
        return this;
    }

    private String asString(String invalidCharacters) {
        String and = ' ' + Translations.Labels.AND.translate(getThesaurus()) + ' ';
        return Pattern.compile("").splitAsStream(invalidCharacters).collect(FancyJoiner.joining(", ", and));
    }

}
