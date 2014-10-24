package com.elster.jupiter.export;

public interface DataExportStrategy {

    boolean isExportUpdate();

    boolean isExportContinuousData();

    ValidatedDataOption getValidatedDataOption();

}
