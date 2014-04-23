package com.elster.jupiter.systemadmin.rest.response;

import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class LicenseShortInfo {
    protected String applicationkey;
    protected String applicationname;
    protected String status;
    protected Long expires;

    public LicenseShortInfo() {}

    public LicenseShortInfo(NlsService nlsService, License license) {
        this.applicationkey = license.getApplicationKey();
        this.applicationname = nlsService.getThesaurus(this.applicationkey, Layer.REST).getString(this.applicationkey, this.applicationkey);
        this.status = license.getStatus().name().toLowerCase();
        this.expires = license.getExpiration().getTime();
    }

    public String getStatus() {
        return status;
    }

    public String getApplicationkey() {
        return applicationkey;
    }

    public String getApplicationname() {
        return applicationname;
    }

    public Long getExpires() {
        return expires;
    }
}
