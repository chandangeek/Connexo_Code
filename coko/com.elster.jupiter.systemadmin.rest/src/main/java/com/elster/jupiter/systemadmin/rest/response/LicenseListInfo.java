package com.elster.jupiter.systemadmin.rest.response;

import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.NlsService;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class LicenseListInfo {
    private List<LicenseShortInfo> data = new ArrayList<>();
    private long total;

    public LicenseListInfo() {
    }

    public LicenseListInfo(NlsService nlsService, List<License> licenses) {
        this();
        for (License license : licenses) {
            LicenseShortInfo info = new LicenseShortInfo(nlsService, license);
            data.add(info);
        }
        total = licenses.size();

    }

    public long getTotal() {
        return total;
    }

    public List<LicenseShortInfo> getData() {
        return data;
    }

}
