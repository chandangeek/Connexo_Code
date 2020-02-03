package com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata;

import com.elster.jupiter.export.webservicecall.DataExportSCCustomInfo;

public class ProfileIdCustomInfo implements DataExportSCCustomInfo {
    private String info;

    public ProfileIdCustomInfo(String customInfo) {
        info = customInfo;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void fromString(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return info;
    }
}
