package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImport;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

import java.sql.Connection;
import java.sql.SQLException;

class OrmClientImpl implements OrmClient {

    private final DataModel dataModel;

    public OrmClientImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public DataMapper<ImportSchedule> getImportScheduleFactory() {
        return dataModel.mapper(ImportSchedule.class);
    }

    @Override
    public DataMapper<FileImport> getFileImportFactory() {
        return dataModel.mapper(FileImport.class);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataModel.getConnection(false);
    }

    @Override
    public void install() {
        dataModel.install(true, true);
    }

}
