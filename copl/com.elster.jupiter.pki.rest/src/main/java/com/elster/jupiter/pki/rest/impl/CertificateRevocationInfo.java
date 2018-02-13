package com.elster.jupiter.pki.rest.impl;

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

    public void addIdWithUsages(Long id) {
        if (bulk.certificatesIdsWithUsages == null) {
            bulk.certificatesIdsWithUsages = new ArrayList<>();
        }
        bulk.certificatesIdsWithUsages.add(id);
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

        //all ids
        public List<Long> certificatesIds = new ArrayList<>();
        //ids with usages, that can't be revoked
        public List<Long> certificatesIdsWithUsages = new ArrayList<>();

        private void updateCounters() {
            total = (long) certificatesIds.size();
            invalid = (long) certificatesIdsWithUsages.size();
            valid = total - invalid;
        }
    }
}
