package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.nio.file.FileSystem;
import java.util.Map;


/**
 * Created by igh on 22/05/2015.
 */
public abstract class AbstractDataExportDestination implements DataExportDestination {

    static final Map<String, Class<? extends DataExportDestination>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends DataExportDestination>>of(FileDestination.TYPE_IDENTIFIER, FileDestinationImpl.class, EmailDestination.TYPE_IDENTIFIER, EmailDestinationImpl.class);

    private long id;
    private Reference<ExportTask> task = ValueReference.absent();
    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final DataExportService dataExportService;
    private final AppService appService;
    private final FileSystem fileSystem;

    @Inject
    AbstractDataExportDestination(DataModel dataModel, Thesaurus thesaurus, DataExportService dataExportService, AppService appService, FileSystem fileSystem) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.dataExportService = dataExportService;
        this.appService = appService;
        this.fileSystem = fileSystem;
    }


    public ExportTask getTask() {
        return this.task.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return id == ((AbstractDataExportDestination) o).id;
    }

    @Override
    public long getId() {
        return id;
    }

    public void save() {
        if (getId() == 0) {
            doPersist();
            return;
        }
        doUpdate();
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
    }

    public void delete() {
        doUpdate();
    }

    protected Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    protected AppService getAppService() {
        return appService;
    }

    protected DataExportService getDataExportService() {
        return dataExportService;
    }

    protected FileSystem getFileSystem() {
        return fileSystem;
    }
}
