package com.elster.jupiter.systemadmin.rest.response;

import com.elster.jupiter.license.License;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class LicenseListInfo {
    List<LicenseShortInfo> data;
    private long total;

    public LicenseListInfo() {
        data = new ArrayList<>();
    }

    public LicenseListInfo(List<License> lics) {
        this();
        for (License lic : lics) {
            LicenseShortInfo info = new LicenseShortInfo(lic);
            data.add(info);
        }
        total = lics.size();

    }

    public long getTotal() {
        return total;
    }

    public List<LicenseShortInfo> getData() {
        return data;
    }

}
