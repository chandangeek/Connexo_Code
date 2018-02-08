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
        bulk.updateCounts();
    }

    public static class Bulk {
        //do math on backend
        public Long total;
        public Long valid;
        public Long invalid;

        //all ids
        public List<Long> certificatesIds;
        //ids with usages, that can't be revoked
        public List<Long> certificatesIdsWithUsages;

        private void updateCounts() {
            if (total == null) {
                total = (long) certificatesIds.size();
            }
            invalid = (long) certificatesIdsWithUsages.size();
            valid = total - invalid;
        }
    }
}
