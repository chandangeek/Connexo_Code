package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.util.ArrayList;
import java.util.List;

public class CertificateRevocationInfo {
    public boolean isOnline;
    public Long timeout;
    public Bulk bulk;


    public CertificateRevocationInfo() {
        this.bulk = new Bulk();
    }

    public CertificateRevocationInfo(boolean isOnline) {
        this.isOnline = isOnline;
        this.bulk = new Bulk();
    }

    public void addWithUsages(CertificateWrapper cert) {
        if (bulk.certificatesWithUsages == null) {
            bulk.certificatesWithUsages = new ArrayList<>();
        }
        bulk.certificatesWithUsages.add(new IdWithNameInfo(cert.getId(), cert.getAlias()));
    }

    /**
     * Do math on backend
     */
    public void updateCounters() {
        bulk.updateCounters();
    }


    public static class Bulk {
        public Long total = 0L;
        public Long valid = 0L;
        public Long invalid = 0L;

        public List<Long> certificatesIds = new ArrayList<>();
        public List<IdWithNameInfo> certificatesWithUsages = new ArrayList<>();

        private void updateCounters() {
            total = (long) certificatesIds.size();
            invalid = (long) certificatesWithUsages.size();
            valid = total - invalid;
        }
    }
}
