package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImport;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.orm.DataMapper;

import java.sql.Connection;
import java.sql.SQLException;

interface OrmClient {

    DataMapper<ImportSchedule> getImportScheduleFactory();

    DataMapper<FileImport> getFileImportFactory();

    Connection getConnection() throws SQLException;

    void install();
}
