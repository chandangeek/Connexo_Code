package com.elster.jupiter.systemadmin.rest.response;

import com.elster.jupiter.systemadmin.Properties;

import java.util.ArrayList;
import java.util.List;

public class LicenseListInfo {
    List<LicenseShortInfo> data;
    private long total;

    public LicenseListInfo() {
        data = new ArrayList<>();
    }

    public LicenseListInfo(List<Properties> props) {
        this();
        for (Properties prop : props) {
            LicenseShortInfo info = new LicenseShortInfo(prop);
            data.add(info);
        }
        total = props.size();

    }

    public long getTotal() {
        return total;
    }

    public List<LicenseShortInfo> getData() {
        return data;
    }

}
