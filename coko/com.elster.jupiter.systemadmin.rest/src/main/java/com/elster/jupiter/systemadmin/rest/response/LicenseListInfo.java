package com.elster.jupiter.systemadmin.rest.response;

import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.NlsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        Collections.sort(data, new LicenseExpirationDateComparator());
        total = licenses.size();

    }

    public long getTotal() {
        return total;
    }

    public List<LicenseShortInfo> getData() {
        return data;
    }

}

class LicenseExpirationDateComparator implements Comparator<LicenseShortInfo> {

    @Override
    public int compare(LicenseShortInfo l1, LicenseShortInfo l2) {

        return l1.getExpires().compareTo(l2.getExpires());
    }
}
